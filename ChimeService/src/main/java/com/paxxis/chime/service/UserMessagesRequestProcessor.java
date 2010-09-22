/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.paxxis.chime.service;

import com.mysql.jdbc.CommunicationsException;
import com.paxxis.chime.client.common.Cursor;
import com.paxxis.chime.client.common.ErrorMessage;
import com.paxxis.chime.client.common.Message;
import com.paxxis.chime.client.common.User;
import com.paxxis.chime.client.common.UserMessagesBundle;
import com.paxxis.chime.client.common.UserMessagesRequest;
import com.paxxis.chime.client.common.UserMessagesResponse;
import com.paxxis.chime.common.MessagePayload;
import com.paxxis.chime.data.UserMessageUtils;
import com.paxxis.chime.database.DatabaseConnection;
import com.paxxis.chime.database.DatabaseConnectionPool;
import org.apache.log4j.Logger;

/**
 *
 * @author rob
 */
public class UserMessagesRequestProcessor extends MessageProcessor {
    private static final Logger _logger = Logger.getLogger(UserMessagesRequestProcessor.class);

    // the database connection pool
    DatabaseConnectionPool _pool;

    /**
     * Constructor
     *
     */
    public UserMessagesRequestProcessor(MessagePayload payloadType, DatabaseConnectionPool pool)
    {
        super(payloadType);
        _pool = pool;
    }

    protected Message process(boolean ignorePreviousChanges)
    {
        Object payload = getPayload();
        UserMessagesRequest requestMessage = (UserMessagesRequest)new UserMessagesRequest().createInstance(payload);
        User user = requestMessage.getUser();

        // build up a response
        Message response = null;
        DatabaseConnection database = _pool.borrowInstance(this);

        boolean tryAgain = true;
        boolean retried = false;

        while (tryAgain)
        {
            tryAgain = false;
            UserMessagesResponse dtr = new UserMessagesResponse();
            dtr.setRequest(requestMessage);
            response = dtr;

            try
            {
                User userInstance = requestMessage.getDataInstance();
                if (requestMessage.getType() == UserMessagesRequest.Type.Query) {
                    Cursor cursor = requestMessage.getCursor();
                    long start = System.currentTimeMillis();

                    UserMessagesBundle msgBundle = UserMessageUtils.getMessages(userInstance, cursor, database);
                    userInstance.setUserMessagesBundle(msgBundle);

                    long end = System.currentTimeMillis();
                    _logger.info(msgBundle.getMessages().size() + " of " + msgBundle.getCursor().getTotal() + " User Message(s) retrieved in " + (end - start) + " msecs");
                } else {
                    long start = System.currentTimeMillis();

                    userInstance = UserMessageUtils.deleteMessages(requestMessage.getUser(), userInstance, requestMessage.getDeleteList(), database);

                    long end = System.currentTimeMillis();
                    _logger.info("User Message(s) deleted in " + (end - start) + " msecs");
                }

                dtr.setUser(userInstance);
            }
            catch (Exception e)
            {
                if (e.getCause() instanceof CommunicationsException)
                {
                    if (!retried && !database.isConnected())
                    {
                        retried = true;
                        tryAgain = true;

                        _pool.connect(database);
                    }
                }
                else
                {
                    ErrorMessage em = new ErrorMessage();
                    em.setMessage(e.getMessage());
                    response = em;
                }
            }
        }

        _pool.returnInstance(database, this);

        return response;
    }

}
