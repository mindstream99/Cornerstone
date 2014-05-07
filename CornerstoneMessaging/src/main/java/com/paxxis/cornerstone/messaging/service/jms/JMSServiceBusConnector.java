/*
 * Copyright 2010 the original author or authors.
 * Copyright 2009 Paxxis Technology LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paxxis.cornerstone.messaging.service.jms;

import com.paxxis.cornerstone.messaging.common.jms.JMSSession;
import com.paxxis.cornerstone.messaging.service.ServiceBusConnector;
import org.apache.log4j.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.naming.Context;

/**
 * ServiceBusConnector manages connections to the service bus.
 *  
 * 
 * @author Matthew Pflueger
 */
public class JMSServiceBusConnector extends ServiceBusConnector {
    private static Logger _logger = Logger.getLogger(JMSServiceBusConnector.class.getName());

    // JMS objects
    private Connection _connection = null;
    private JMSSession _session = null;
    

    public JMSServiceBusConnector() {}


    @Override
    public com.paxxis.cornerstone.messaging.common.Session createSession() {
        try {
            return new JMSSession(_connection.createSession(false, getAcknowledgeMode()), getInitialContextFactory());
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public com.paxxis.cornerstone.messaging.common.Session getSession() {
        return _session;
    }

    /**
     * Start the connection.
     */
    @Override
    protected void startConnection() {
        try {
            _connection.start();
        } catch (JMSException e) {
            _logger.error(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void stopConnection() {
    	try {
			_connection.stop();
		} catch (JMSException e) {
            _logger.error(e);
            throw new RuntimeException(e);
		}
    }



    /**
     * Determine if we're connected to the bus.
     *
     * @return true if connected (including disconnect pending), false otherwise.
     */
    public boolean isConnected() {
        return (_connection != null);
    }

    protected void initConnection() {
        try {
            // get an initial jndi context
            Context ctx = getInitialContextFactory().createInitialContext();

            // lookup the connection factory
            ConnectionFactory factory = (ConnectionFactory)
                    ctx.lookup(getConnectionFactoryName());

            // create a connection
            _connection = factory.createConnection();
            _connection.setExceptionListener(
                    new ExceptionListener() {
                        public void onException(JMSException ex) {
                            _logger.warn(ex);

                            // TODO these methods are part of the Cornerstone reconnection mechanism, which
                            // is incompatible with ActiveMQ's failover protocol (and also doesn't work correctly).
                            // So for now logging the exception will have to do.
                            //onConnectionFailed(ex);
                            //notifyConnectionStatusChange();
                        }
                    }
            );

            _session = (JMSSession) createSession();
        } catch (Exception e) {
            _logger.error(e);
            throw new RuntimeException(e);
        }
    }


    /**
     * Close the JMS session
     */
    protected void closeSession() throws JMSException
    {
        if (_session != null) {
            _session.getSession().close();
            _session = null;
        }
    }

    /**
     * Close the JMS connection
     */
    protected void closeConnection() throws JMSException
    {
        if (_connection != null) {
            _connection.close();
            _connection = null;
        }
    }

    protected void close() {
        try {
            closeSession();
            closeConnection();
        } catch (JMSException e) {
            _logger.error(e);
        }
    }


}
