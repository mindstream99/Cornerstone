package com.paxxis.cornerstone.messaging.common.amqp;

import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.messaging.TestMessage;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class AMQPJavaObjectPayloadTest {

    @Test
    public void testCreateMessage() throws Exception {
        AMQPJavaObjectPayload jop = new AMQPJavaObjectPayload(null);
        AMQPMessage msg = jop.createMessage();
        assertNull("Body should be null", msg.getBody());
        assertNotNull("Message should have props", msg.getProps());
        assertNotNull("Message should have headers", msg.getProps().getHeaders());
        assertEquals(
                "Message payload type should be JavaObject",
                MessagingConstants.PayloadType.JavaObjectPayload.getValue(),
                msg.getProps().getHeaders().get(MessagingConstants.HeaderConstant.PayloadType.name()));
        assertEquals(
                "Message payload content type should be application/json",
                MessagingConstants.PayloadType.JavaObjectPayload.getContentType(),
                msg.getProps().getContentType());
    }

    @Test
    public void testCreateMessageWithObject() throws Exception {
        AMQPJavaObjectPayload jop = new AMQPJavaObjectPayload(null);
        TestMessage data = new TestMessage("world", 1000, 2000);

        AMQPMessage msg = jop.createMessage(data);
        Map<String, Object> headers = msg.getProps().getHeaders();
        assertNotNull("Body should not be null", msg.getBody());
        assertEquals(
                "Message type header is wrong",
                data.getMessageType(),
                headers.get(MessagingConstants.HeaderConstant.MessageType.name()));
        assertEquals(
                "Message version header is wrong",
                data.getMessageVersion(),
                headers.get(MessagingConstants.HeaderConstant.MessageVersion.name()));

        TestMessage data2 = (TestMessage) jop.getPayload(msg);
        assertTrue("Messages should be distinct objects", data != data2);
        assertEquals("Message types should equal", data.getMessageType(), data2.getMessageType());
        assertEquals("Message version should equal", data.getMessageVersion(), data2.getMessageVersion());
        assertEquals("Message data should equal", data.getData(), data2.getData());
    }
}