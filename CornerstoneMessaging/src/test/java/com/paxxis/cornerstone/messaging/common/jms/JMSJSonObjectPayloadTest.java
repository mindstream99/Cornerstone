package com.paxxis.cornerstone.messaging.common.jms;

import com.paxxis.cornerstone.base.MessagingConstants;
import com.paxxis.cornerstone.messaging.TestMessage;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.jms.Session;
import javax.jms.TextMessage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class JMSJSonObjectPayloadTest {

    TextMessage msg;
    Session session;
    JMSJSonObjectPayload jop;

    @Before
    public void before() throws Exception {
        msg = mock(TextMessage.class);
        session = mock(Session.class);
        when(session.createTextMessage()).thenReturn(msg);
        jop = new JMSJSonObjectPayload(session, TestMessage.class);
    }

    @Test
    public void testCreateMessage() throws Exception {
        JMSMessage message = jop.createMessage();
        verify(msg).setIntProperty(MessagingConstants.HeaderConstant.PayloadType.name(), jop.getType().getValue());
        assertSame("Incorrect TextMessage", msg, message.message);
    }

    @Test
    public void testCreateMessageWithObject() throws Exception {
        TestMessage data = new TestMessage("world", 1000, 2000);
        JMSMessage message = jop.createMessage(data);
        verify(msg).setIntProperty(MessagingConstants.HeaderConstant.PayloadType.name(), jop.getType().getValue());
        verify(msg).setIntProperty(MessagingConstants.HeaderConstant.MessageType.name(), data.getMessageType());
        verify(msg).setIntProperty(MessagingConstants.HeaderConstant.MessageVersion.name(), data.getMessageVersion());
        verify(msg).setText(new ObjectMapper().writeValueAsString(data));
        assertSame("Incorrect TextMessage", msg, message.message);
    }
}