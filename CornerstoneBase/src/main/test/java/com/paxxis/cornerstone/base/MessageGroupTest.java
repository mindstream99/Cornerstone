package com.paxxis.cornerstone.base;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class MessageGroupTest {

    @Test
    public void testMessageSelector() throws Exception {
        assertEquals("GroupId = 1000 AND GroupVersion = 2000", new TestMessageGroupV2000().getMessageSelector());
    }

    @Test
    public void testValidMessageGroupClass() throws Exception {
        TestMessageGroupV2000 grp = new TestMessageGroupV2000();
        grp.initialize();
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidMessageGroupClass() throws Exception {
        new MessageGroup() {
            @Override
            public int getId() {
                return 100;
            }

            @Override
            public int getVersion() {
                return 200;
            }
        }.initialize();
    }
}

