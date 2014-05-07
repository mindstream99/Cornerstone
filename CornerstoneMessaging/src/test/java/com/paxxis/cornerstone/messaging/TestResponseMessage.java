package com.paxxis.cornerstone.messaging;

import com.paxxis.cornerstone.base.ResponseMessage;

public class TestResponseMessage extends ResponseMessage<TestRequestMessage> {

    private String data;
    private int type;
    private int version;

    public TestResponseMessage() {
        this(null, 5001, 6000);
    }

    public TestResponseMessage(String data) {
        this(data, 5001, 6000);
    }

    public TestResponseMessage(String data, int type, int version) {
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

    public void setData(String data) {
        this.data = data;
    }

    private void setMessageType(int type) {
        this.type = type;
    }

    private void setMessageVersion(int version) {
        this.version = version;
    }
}
