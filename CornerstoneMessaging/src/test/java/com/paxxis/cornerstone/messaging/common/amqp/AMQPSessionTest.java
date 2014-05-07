package com.paxxis.cornerstone.messaging.common.amqp;

import com.paxxis.cornerstone.base.MessagingConstants;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AMQPSessionTest {

    @Test
    public void testSelectorParsing() throws Exception {
        AMQPSession session = new AMQPSession();
        Map<String, Object> args = session.createBindingArgs(new StringBuilder()
       	       .append(MessagingConstants.HeaderConstant.GroupId.name())
    		   .append(" = ")
    		   .append(1000)
    		   .append(" AND ")
    		   .append(MessagingConstants.HeaderConstant.GroupVersion.name())
    		   .append(" = ")
    		   .append(2000)
    		   .append(" AND routingKey = test")
    		   .toString());

    	assertEquals("all", args.get("x-match"));
    	assertEquals(1000, args.get(MessagingConstants.HeaderConstant.GroupId.name()));
        assertEquals(2000, args.get(MessagingConstants.HeaderConstant.GroupVersion.name()));
        assertEquals("test", args.get("routingKey"));

        args = session.createBindingArgs(new StringBuilder()
                    .append(MessagingConstants.HeaderConstant.GroupId.name())
                    .append(" = ")
                    .append(3000)
                    .toString());
        assertEquals(3000, args.get(MessagingConstants.HeaderConstant.GroupId.name()));

        args = session.createBindingArgs(null);
        assertNotNull(args);
        assertEquals(0, args.size());
    }
}