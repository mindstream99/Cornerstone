package com.paxxis.cornerstone.service;

import java.io.Serializable;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.paxxis.cornerstone.base.ErrorMessage;
import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.base.RequestMessage;
import com.paxxis.cornerstone.common.JavaObjectPayload;
import com.paxxis.cornerstone.common.MessagePayload;

public class DestinationSender extends CornerstoneConfigurable implements IServiceBusConnectorClient {
    private static final Logger logger = Logger.getLogger(RequestQueueSender.class);

    // the destination
    private Destination destination = null;

    // the message sender
    private MessageProducer messageSender = null;

    protected MessageProducer getMessageSender() {
		return messageSender;
	}

	// the name of the destination
    private String destinationName = null;

    // the service bus connector
    private ServiceBusConnector connector = null;

    protected ServiceBusConnector getConnector() {
		return connector;
	}

	// teardown pending flag
    private boolean teardownPending = false;

    public DestinationSender() {
    }

    public void setDestinationName(String name) {
        destinationName = name;
    }

    protected String getDestinationName() {
        return destinationName;
    }

    public void setServiceBusConnector(ServiceBusConnector connector) {
        this.connector = connector;
    }

    /**
     * Is a teardown pending?
     *
     * @return true if a disconnect has been initiated and is not
     * yet complete, false otherwise.
     */
    public boolean isTeardownPending() {
        return teardownPending;
    }

    /**
     * Close the JMS session objects
     */
    protected void closeDown() throws JMSException {
        
        messageSender.close();
        messageSender = null;
        destination = null;
    }

    public void halt() {
    }

    /**
     * Setup the JMS specific objects.<br><br>
     * This method is called by the ServiceBusConnector as part of its
     * connection process.  It should not be called directly by users...
     *
     * @throws RuntimeException if the setup could not be completed
     */
    public void setup() {

        try {
            // lookup the destination
            destination = (Destination)connector.getInitialContextFactory().createInitialContext().lookup(destinationName);

            messageSender = connector.createMessageProducer(destination);
            messageSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch(Throwable t) {
            try {
                closeDown();
            } catch (JMSException e) {
                // is there any need to include this in the runtime exception below?
            }

            throw new RuntimeException(t);
        }
   }

    /**
     * Tear down the sender.
     */
    public void tearDown(final ShutdownListener listener) {
        // don't do this if we're already in the process of tearing down
        if (!isTeardownPending()) {
            teardownPending = true;

            try {
                closeDown();
                teardownPending = false;
                listener.onShutdownComplete();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * publish a message to the destination with no response.
     *
     * @param clazz the response class
     * @param msg the message 
     * @param payloadType the message payload type
     *
     */
    public synchronized <REQ extends RequestMessage> void publish(
            REQ msg,
			MessagePayload payloadType) {

    	try {
            Message message = prepareMessage(msg, payloadType);
            messageSender.send(message);
        } catch (JMSException je) {
            ErrorMessage errorMsg = new ErrorMessage();
            errorMsg.setMessage("Unable to publish message. " + je.getMessage());
            throw new SendException(errorMsg);
        }
    }

    /**
     * Prepare a message for sending.
     *
     * @param requester the service requester
     */
    protected Message prepareMessage(
            com.paxxis.cornerstone.base.Message msg,
            MessagePayload payloadType) 
            throws JMSException {

        Message message = payloadType.createMessage(connector.getSession());

        message.setIntProperty(MessagingConstants.HeaderConstant.MessageType.name(), msg.getMessageType());
        message.setIntProperty(MessagingConstants.HeaderConstant.MessageVersion.name(), msg.getMessageVersion());
        message.setIntProperty(MessagingConstants.HeaderConstant.PayloadType.name(), payloadType.getType().getValue());

        Object payload = msg.getAsPayload(payloadType.getType());
        if (payloadType instanceof JavaObjectPayload) {
            ((ObjectMessage)message).setObject((Serializable)payload);
        }

        return message;
    }
}
