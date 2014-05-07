package com.paxxis.cornerstone.messaging;

import com.paxxis.cornerstone.base.Message;
import com.paxxis.cornerstone.base.MessageGroup;

import java.util.ArrayList;
import java.util.List;

public class TestMessageGroupV2000 extends MessageGroup {


    @Override
    public int getId() {
        return 1000;
    }

    @Override
    public int getVersion() {
        return 2000;
    }

    @Override
    public void register(Message msg) {
        super.register(msg);
    }

    public void register(String name) {
        List<String> names = getMessageProcessorNames();
        if (names == null) {
            names = new ArrayList<String>();
        }
        names.add(name);
        setMessageProcessorNames(names);
    }
}
