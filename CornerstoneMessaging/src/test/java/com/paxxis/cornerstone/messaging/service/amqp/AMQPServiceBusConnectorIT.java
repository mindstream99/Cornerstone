package com.paxxis.cornerstone.messaging.service.amqp;

import com.paxxis.cornerstone.base.MessageGroup;
import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.base.ResponseMessage;
import com.paxxis.cornerstone.messaging.TestMessageGroupV2000;
import com.paxxis.cornerstone.messaging.TestRequestMessage;
import com.paxxis.cornerstone.messaging.TestResponseMessage;
import com.paxxis.cornerstone.messaging.common.Destination;
import com.paxxis.cornerstone.messaging.common.Message;
import com.paxxis.cornerstone.messaging.common.MessageListener;
import com.paxxis.cornerstone.messaging.common.MessageProducer;
import com.paxxis.cornerstone.messaging.common.ResponsePromise;
import com.paxxis.cornerstone.messaging.common.Session;
import com.paxxis.cornerstone.messaging.common.amqp.AMQPDestination;
import com.paxxis.cornerstone.messaging.common.amqp.AMQPMessage;
import com.paxxis.cornerstone.messaging.common.amqp.AMQPSession;
import com.paxxis.cornerstone.messaging.service.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class AMQPServiceBusConnectorIT {


    @Test
    public void testSimpleSendReceive() throws Exception {

        AMQPServiceBusConnector connector = new AMQPServiceBusConnector();
        connector.initConnection();
        connector.startConnection();

        Session session = connector.getSession();
        assertNotNull(session);
        assertEquals(AMQPSession.class, session.getClass());

        AMQPSession s = (AMQPSession) session;

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("durable", false);
        args.put("exclusive", true);

        final AtomicReference<Message> amsg = new AtomicReference<Message>(null);
        session.createConsumer(
                new AMQPDestination("testA", s.getChannel()),
                "GroupId = 1000 AND GroupVersion = 2000",
                new MessageListener() {
                    @Override
                    public void onMessage(Message msg) {
                        amsg.compareAndSet(null, msg);
                    }
                },
                args);

        final AtomicReference<Message> bmsg = new AtomicReference<Message>(null);
        session.createConsumer(
                new AMQPDestination("testB", s.getChannel()),
                "GroupId = 1000 AND GroupVersion = 2000",
                new MessageListener() {
                    @Override
                    public void onMessage(Message msg) {
                        bmsg.compareAndSet(null, msg);
                    }
                },
                args);

        MessageProducer pA = session.createProducer("testA");
        AMQPMessage m = new AMQPMessage("test".getBytes());
        m.setProperty("GroupId", 1000);
        m.setProperty("GroupVersion", 2000);
        pA.send(m);

        MessageProducer pB = session.createProducer("testB");
        pB.send(m);

        await("testA publish and consume").atMost(2, SECONDS).untilAtomic(amsg, notNullValue());
        await("testB publish and consume").atMost(2, SECONDS).untilAtomic(bmsg, notNullValue());
        assertEquals("Wrong message class", AMQPMessage.class, amsg.get().getClass());
        assertEquals("Wrong message class", AMQPMessage.class, bmsg.get().getClass());

        m = (AMQPMessage) amsg.get();
        assertEquals("Invalid message body", "test", new String(m.getBody()));
        assertEquals("testA", m.getProperty("routingKey").toString());
        assertEquals(1000, m.getProperty("GroupId"));
        assertEquals(2000, m.getProperty("GroupVersion"));

        m = (AMQPMessage) bmsg.get();
        assertEquals("Invalid message body", "test", new String(m.getBody()));
        assertEquals("testB", m.getProperty("routingKey").toString());
        assertEquals(1000, m.getProperty("GroupId"));
        assertEquals(2000, m.getProperty("GroupVersion"));
    }


    @Test
    public void testComplexSendReceive() throws Exception {

        final TestMessageGroupV2000 grp = new TestMessageGroupV2000();
        grp.register("testRequestProcessor");
        grp.register(new TestRequestMessage());
        grp.initialize();


        final RequestQueueSenderPool sender = new RequestQueueSenderPool() {
            @Override
            protected ServiceBusConnector createConnector() {
                AMQPServiceBusConnector connector = new AMQPServiceBusConnector();
                //do not set connect on startup here as that causes the
                //connector to initialize clients before we've set ours
                connector.initialize();
                return connector;
            }

            @Override
            protected RequestQueueSender createSender() {
                RequestQueueSender sender = new RequestQueueSender();
                sender.setPayloadType(MessagingConstants.PayloadType.JavaObjectPayload);
                sender.setMessageGroup(grp);
                sender.setDestinationName("testRequest");
                sender.initialize();
                return sender;
            }
        };

        sender.initialize();
        assertFalse(sender.isEmpty());


        MessageProcessorFactory processorFactory = new MessageProcessorFactory() {
            @Override
            public MessageProcessor<?, ?> getMessageProcessor(String name) {
                //we ignore the name - it is testRequestProcessor
                return new BaseMessageProcessor<TestRequestMessage, TestResponseMessage>() {
                    @Override
                    public MessagingConstants.PayloadType getPayloadType() {
                        return MessagingConstants.PayloadType.JavaObjectPayload;
                    }

                    @Override
                    protected boolean process(TestRequestMessage requestMessage, TestResponseMessage responseMessage) throws Exception {
                        responseMessage.setData("ECHO " + requestMessage.getData());
                        return true;
                    }

                    @Override
                    public Class<TestRequestMessage> getRequestMessageClass() {
                        return TestRequestMessage.class;
                    }

                    @Override
                    public Class<TestResponseMessage> getResponseMessageClass() {
                        return TestResponseMessage.class;
                    }
                };
            }
        };


        ServiceBusMessageRouter handler = new ServiceBusMessageRouter();
        //we reuse the same group as the sender however normally this would not be the case - you would have two
        //groups in your config...
        handler.setMessageGroup(grp);
        handler.setMessageProcessorFactory(processorFactory);
        handler.initialize();


        AMQPServiceBusMessageReceiver receiver = new AMQPServiceBusMessageReceiver();
        receiver.setDestinationName("testRequest");
        receiver.setMessageHandler(handler);
        receiver.initialize();


        AMQPServiceBusConnector connector = new AMQPServiceBusConnector() {
            @Override
            protected void initConnection() {
                super.initConnection();
                configureSession(this);
            }
        };

        connector.addServiceBusConnectorClient(receiver);
        connector.setConnectOnStartup(true);
        connector.initialize();

        TestResponseMessage resp = sender.send(
                new TestRequestMessage("test"),
                TestResponseMessage.class).getResponse(2000);

        assertEquals("ECHO test", resp.getData());
    }


    private AMQPSession configureSession(AMQPServiceBusConnector connector) {
        Session session = connector.getSession();
        assertNotNull(session);
        assertEquals(AMQPSession.class, session.getClass());

        //for testing we flip the usual defaults to clean up the broker...
        AMQPSession s = (AMQPSession) session;
        s.setDurableQueue(false);
        s.setExclusiveQueue(true);
        s.setAutoDeleteQueue(true);

        return s;
    }

}