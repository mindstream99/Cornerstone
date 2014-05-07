package com.paxxis.cornerstone.messaging;

public class TestMessage extends com.paxxis.cornerstone.base.Message {

    private String data = null;
    private int type = 0;
    private int version = 0;

    private TestMessage() {
        //for JSON serialization
    }

    public TestMessage(String data, int type, int version) {
        this.data = data;
        this.type = type;
        this.version = version;
    }

    @Override
    public int getMessageType() {
        return type;
    }

    @Override
    public int getMessageVersion() {
        return version;
    }

    public String getData() {
        return data;
    }

    private void setData(String data) {
        this.data = data;
    }

    private void setMessageType(int type) {
        this.type = type;
    }

    private void setMessageVersion(int version) {
        this.version = version;
    }
}
