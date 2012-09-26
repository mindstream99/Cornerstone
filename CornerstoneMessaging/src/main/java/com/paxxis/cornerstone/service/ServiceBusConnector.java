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

package com.paxxis.cornerstone.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.paxxis.cornerstone.common.CornerstoneConfigurable;
import com.paxxis.cornerstone.common.ScheduledExecutionPool;

/**
 * ServiceBusConnector manages connections to the service bus.
 *  
 * 
 * @author Robert Englander
 */
public class ServiceBusConnector extends CornerstoneConfigurable
{
    private static Logger _logger = Logger.getLogger(ServiceBusConnector.class.getName());

	private static final String ACK_AUTO = "auto";
	private static final String ACK_CLIENT = "client";
	private static final String ACK_DUPS = "dups";
	
	private static final int SILENCE_FREQUENCY = 300000;
	private static final int SILENCE_DURATION = 10000;
	
	private int silenceFrequency = SILENCE_FREQUENCY;
	private int silenceDuration = SILENCE_DURATION;
	private ScheduledExecutionPool executionPool = null;
	private boolean periodicSilence = false;
	
	private class SilenceStarter implements Runnable {
		private boolean startSilence;
		
		public SilenceStarter(boolean starter) {
			this.startSilence = starter;
		}
		
		@Override
		public void run() {
			if (_connection != null) {
				try {
					if (startSilence) {
						_logger.info("Stopping connection");
						stopConnection();
					} else {
						_logger.info("Starting connection");
						startConnection();
					}
				} catch (Exception e) {
					_logger.error(e);
				}

				int freq = startSilence ? silenceDuration : silenceFrequency;
				executionPool.schedule(new SilenceStarter(!startSilence), freq, TimeUnit.MILLISECONDS);
			}
		}
	}
	
    // JMS objects
    private Connection _connection = null;
    private Session _session = null;
    
    // the initial context factory
    private JndiInitialContextFactory _contextFactory = null;
    
    // the connection factory name
    private String _connectionFactoryName = "";
    
    // the JMS message acknowledge mode
    private int ackMode = Session.AUTO_ACKNOWLEDGE;
    
    // indicates whether or not the connection should be established at startup
    private boolean _connectOnStartup = false;
    
    // indicates how often the connector should attempt to reconnect
    // after a dropped connection (in milliseconds).  -1 means that
    // it will not attempt to reconnect.
    private long _reconnectInterval = 30000L;
    
    private boolean _isAutoReconnectPending = false;
    
    // the associated management controller
    private IServiceBusController _mgmtController = null;
    
    // the registered connector clients
    private ArrayList<IServiceBusConnectorClient> _connectorClients = new ArrayList<IServiceBusConnectorClient>();

    private ArrayList<ServiceBusConnectionListener> _listeners = new ArrayList<ServiceBusConnectionListener>();
    
    public static int _reconnectorCounter = 0;
    class Reconnector extends Thread
    {
        public Reconnector()
        {
            setName("Cornerstone Service Bus Reconnector - " + _reconnectorCounter++);
        }
        
        public void run()
        {
            while (true)
            {
                try
                {
                    sleep(_reconnectInterval);
                }
                catch (InterruptedException e)
                {}
                
                if (isConnected())
                {
                    // no reason to be here
                    break;
                }
                
                try
                {
                    connect();
                    break;
                }
                catch (Exception ee)
                {
                    _logger.error("Unexpected error in JMS reconnector", ee);
                }
            }
        }
    }
    
    /**
     * Constructor
     */
    public ServiceBusConnector()
    {
    }
    
    public void setExecutionPool(ScheduledExecutionPool pool) {
    	executionPool = pool;
    }
    
    public void setPeriodicSilence(boolean periodicSilence) {
    	this.periodicSilence = periodicSilence;
    }
    
    public void setSilenceFrequency(int freq) {
    	if (freq < 60000) {
    		throw new RuntimeException("SilenceFrequency must be greater or equal to 60000");
    	}
    	
    	silenceFrequency = freq;
    }
    
    public void setSilenceDuration(int time) {
    	if (time < 5000) {
    		throw new RuntimeException("SilencePauseTime must be greater or equal to 5000");
    	}
    	
    	silenceDuration = time;
    }
    
    public void initialize() {
    	super.initialize();
    	if (periodicSilence) {
        	if (executionPool != null) {
        		// the initial silence cycle begins randomly within the next 10 minutes
    			Random rand = new Random();
    			long wait = 60000 * (1 + rand.nextInt(10));
    			executionPool.schedule(new SilenceStarter(true), wait, TimeUnit.MILLISECONDS);
        	} else {
        		throw new RuntimeException("ExecutionPool can't be null when PeriodicSilence is set to true");
        	}
    	}
    }
    
    public void addServiceBusConnectionListener(ServiceBusConnectionListener listener)
    {
        synchronized (_listeners)
        {
            if (!_listeners.contains(listener))
            {
                _listeners.add(listener);
            }
        }
    }
    
    public void removedServiceBusConnectionListener(ServiceBusConnectionListener listener)
    {
        synchronized (_listeners)
        {
            if (_listeners.contains(listener))
            {
                _listeners.remove(listener);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void notifyConnectionChange(boolean connected)
    {
        List<ServiceBusConnectionListener> listeners = null;
        synchronized (_listeners)
        {
            listeners = (List<ServiceBusConnectionListener>)_listeners.clone();
        }
        
        for (ServiceBusConnectionListener listener : listeners)
        {
            listener.onConnectionChange(connected);
        }
    }
    
    /**
     * Add a list of service bus connector clients
     *
     * @param clients the collection of clients
     */
    public void setServiceBusConnectorClients(ArrayList<IServiceBusConnectorClient> clients)
    {
        _connectorClients = new ArrayList<IServiceBusConnectorClient>(clients);
        for (IServiceBusConnectorClient client : clients)
        {
            client.setServiceBusConnector(this);
        }
    }
    
    /**
     * Add a service bus connector client.
     *
     * @param client the client to add
     */
    public void addServiceBusConnectorClient(IServiceBusConnectorClient client)
    {
        synchronized (_connectorClients)
        {
            _connectorClients.add(client);
            client.setServiceBusConnector(this);
        }
    }
    
    /**
     * Get the JMS session
     *
     * @return the session instance
     */
    protected Session getSession()
    {
        return _session;
    }
   
    /**
     * Create a message consumer for a specified destination.
     *
     * @param dest the destination for which a consumer is created
     * @param selector the message selector (or null if no selector is used)
     *
     * @return a message consumer
     */
    protected MessageConsumer createConsumer(Destination dest, String selector)
    {
        try
        {
            return _session.createConsumer(dest, selector);
        }
        catch (JMSException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Create a temporary queue on the current session.
     *
     * @return a temporary queue
     */
    protected TemporaryQueue createTemporaryQueue()
    {
        try
        {
            return _session.createTemporaryQueue();
        }
        catch (JMSException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    protected MessageProducer createMessageProducer(String destinationName) {
        try {
        	Destination dest = null;
        	if (destinationName != null) {
        		dest = (Destination)getInitialContextFactory().createInitialContext().lookup(destinationName);
            }
	        return createMessageProducer(dest);
        } catch (NamingException ne) {
            _logger.error(ne);
            throw new RuntimeException(ne);
        }
    }
    
    /**
     * Create a message sender for a destination
     *
     * @param dest the destination
     *
     * @return a message producer
     */
    protected MessageProducer createMessageProducer(Destination dest)
    {
        try
        {
            return _session.createProducer(dest);
        }
        catch (JMSException e)
        {
            _logger.error(e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Start the connection.
     */
    protected void startConnection() throws Exception {
        try {
            _connection.start();
        } catch (JMSException e) {
            _logger.error(e);
            throw new Exception(e);
        }
    }

    protected void stopConnection() throws Exception {
    	try {
			_connection.stop();
		} catch (JMSException e) {
            _logger.error(e);
            throw new Exception(e);
		}
    }
    
    public boolean isConnectOnStartup()
    {
        return _connectOnStartup;
    }
    
    class Disconnector implements ShutdownListener
    {
        private CountDownLatch latch;
        public Disconnector(int count)
        {
            this.latch = new CountDownLatch(count);
        }
        
        public void onShutdownComplete()
        {
            this.latch.countDown(); 
        }
        
        public void await() {
            try {
	            this.latch.await();
            } catch (InterruptedException ie) {
                //doing nothing is the correct behavior
            }
        }
    }
    
    /**
     * Disconnect from the service bus.
     */
    public void disconnect()
    {
        // ask all of the connector clients to tear down and block
        // until they all complete their shutdown
        Disconnector disc = new Disconnector(_connectorClients.size());
        
        for (IServiceBusConnectorClient client : _connectorClients)
        {
            client.tearDown(disc);
        }
        
        notifyConnectionStatusChange();

        disc.await();
        
        // now that the clients are torn down we can close
        try
        {
            closeSession();
            closeConnection();
        }
        catch (JMSException e)
        {
            throw new RuntimeException(e);
        }
        
        notifyConnectionStatusChange();
    }

    /**
     * Close the JMS session
     */
    protected void closeSession() throws JMSException
    {
        if (_session != null) {
            _session.close();
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
    
    /**
     * Determine if we're connected to the bus.
     *
     * @return true if connected (including disconnect pending), false otherwise.
     */
    public boolean isConnected()
    {
        return (_connection != null);
    }
    
    /**
     * Is a disconnect from the service bus pending?
     *
     * @return true if a disconnect has been initiated and is not
     * yet complete, false otherwise.
     */
    protected boolean isDisconnectPending()
    {
        for (IServiceBusConnectorClient client : _connectorClients)
        {
            if (client.isTeardownPending())
            {
                return true;
            }
        }

        return false;
    }
    
    protected boolean isAutoReconnectPending()
    {
        return _isAutoReconnectPending;
    }
    
    /**
     * Connect to the service bus.  
     *
     * @return true if a new connection is established or if the connector
     * is already connected to the service bus. false otherwise
     */
    public boolean connect()
    {
        boolean gotConnection = true;
        
        if (!isConnected())
        {
            gotConnection = false;
            
            try
            {
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
                
                // create a session
                _session = _connection.createSession(false, ackMode);
                
                if (_isAutoReconnectPending)
                {
                    _isAutoReconnectPending = false;
                    _connection.start();
                
                    for (IServiceBusConnectorClient client : _connectorClients)
                    {
                        client.setup();
                    }
                }
                else
                {
                    // ask all the connector clients to perform their setup steps
                    for (IServiceBusConnectorClient client : _connectorClients)
                    {
                        client.setup();
                    }
                    
                    try {
                        _connection.start();
                    } catch (Exception e) {
                    	throw new RuntimeException(e);
                    }
                }

                gotConnection = true;
                //_logger.info("Established service bus connection through factory: " + _connectionFactoryName);
            }
            catch(Throwable t)
            {
                onConnectionFailed(t);
            }
        }
        
        notifyConnectionStatusChange();
        
        return gotConnection;
    }
    
    protected void onConnectionFailed(Throwable t)
    {
        _logger.warn("JMS connection failure", t);

        // ask all the connector clients to perform their setup steps
        for (IServiceBusConnectorClient client : _connectorClients)
        {
            client.halt();
        }
        
        try
        {
            closeSession();
        }
        catch (Exception e)
        {}
        
        try
        {
            closeConnection();
        }
        catch (Exception ee)
        {}
        
        _connection = null;
        _session = null;
        
        _isAutoReconnectPending = true;
        notifyConnectionDropped();
        
        if (_reconnectInterval > 0)
        {
            Reconnector r = new Reconnector();
            r.start();
        }
    }
    
    /**
     * Notifies the associated management bean of a dropped connection.
     */
    protected void notifyConnectionDropped()
    {
        IServiceBusController mbean = getManagementBean();
        if (mbean != null)
        {
            mbean.notifyConnectionFailed();
        }
    }

    /**
     * Notifies the associated management bean of a connection status change.
     */
    protected void notifyConnectionStatusChange()
    {
        IServiceBusController mbean = getManagementBean();
        if (mbean != null)
        {
            mbean.notifyConnectionStatusChange();
        }
        
        notifyConnectionChange(isConnected());
    }
    
    /**
     * Set the associated management controller bean.
     *
     * @param controller the management controller
     */
    public void setManagementBean(IServiceBusController controller)
    {
        _mgmtController = controller;
    }
    
    /**
     * Get the associated management controller bean.
     *
     * @return the management controller
     */
    protected IServiceBusController getManagementBean()
    {
        return _mgmtController;
    }
    
    /**
     * Set the initial jndi context factory
     *
     * @param factory the context factory
     */
    public void setInitialContextFactory(JndiInitialContextFactory factory)
    {
        _contextFactory = factory;
    }
    
    /**
     * Get the initial jndi context factory
     *
     * @return the initial context factory
     */
    protected JndiInitialContextFactory getInitialContextFactory()
    {
        return _contextFactory;
    }
    
    public void setAckMode(String mode) {
    	if (ACK_CLIENT.equalsIgnoreCase(mode)) {
    		ackMode = Session.CLIENT_ACKNOWLEDGE;
    	} else if (ACK_DUPS.equalsIgnoreCase(mode)) {
    		ackMode = Session.DUPS_OK_ACKNOWLEDGE;
    	} else {
    		ackMode = Session.AUTO_ACKNOWLEDGE;
    	}
    }
    
    public String getAckMode() {
    	String result = "???";
    	if (ackMode == Session.AUTO_ACKNOWLEDGE) {
    		result = ACK_AUTO;
    	} else if (ackMode == Session.CLIENT_ACKNOWLEDGE) {
    		result = ACK_CLIENT;
    	} else if (ackMode == Session.DUPS_OK_ACKNOWLEDGE) {
    		result = ACK_DUPS;
    	}
    	
    	return result;
    }
    
    public int getAcknowledgeMode() {
    	return ackMode;
    }
    
    /**
     * Set the name of the connection factory
     *
     * @param name the connection factory name
     */
    public void setConnectionFactoryName(String name)
    {
        _connectionFactoryName = name;
    }
    
    /**
     * Get the name of the connection factory
     *
     * @return the connection factory name
     */
    protected String getConnectionFactoryName()
    {
        return _connectionFactoryName;
    }
    
    /**
     * Set the connect on startup behavior.
     *
     * @param connect true if the connector should connect to the
     * service bus at startup, false otherwise.
     */
    public void setConnectOnStartup(boolean connect)
    {
        _connectOnStartup = connect;
    }

    /**
     * Get the value of the connect on startup property.
     *
     * @return connect on startup value.
     */
    public boolean getConnectOnStartup()
    {
        return _connectOnStartup;
    }
    
    /**
     * Sets the reconnect interval.
     *
     * NOTE:  not implemented yet
     *
     * @param interval the interval, in milliseconds, for retry attempts
     * to connect to the service bus.  -1 disables the reconnect behavior.
     *
     */
    public void setReconnectInterval(int interval)
    {
        _reconnectInterval = interval;
    }
}