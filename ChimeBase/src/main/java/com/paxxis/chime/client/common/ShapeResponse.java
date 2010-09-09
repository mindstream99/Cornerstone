
package com.paxxis.chime.client.common;


/**
 *
 * @author Robert Englander
 */
public class ShapeResponse extends ResponseMessage<ShapeRequest> {
    private final static int VERSION = 1;

    @Override
    public MessageConstants.MessageType getMessageType() {
        return messageType();
    }

    public static MessageConstants.MessageType messageType() {
        return MessageConstants.MessageType.ShapeResponse;
    }

    @Override
    public int getMessageVersion() {
        return messageVersion();
    }

    public static int messageVersion() {
        return VERSION;
    }
    
    private Shape shape = null;
    
    public Shape getShape()
    {
        return shape;
    }
    
    public void setShape(Shape s)
    {
        shape = s;
    }
}

