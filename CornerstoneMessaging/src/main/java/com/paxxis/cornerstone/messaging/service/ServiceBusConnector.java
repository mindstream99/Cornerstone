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

package com.paxxis.cornerstone.messaging.service;

import com.paxxis.cornerstone.common.CornerstoneConfigurable;
import com.paxxis.cornerstone.common.ScheduledExecutionPool;
import com.paxxis.cornerstone.messaging.common.Session;
import com.paxxis.cornerstone.service.ShutdownListener;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ServiceBusConnector manages connections to the service bus.
 *  
 * 
 * @author Robert Englander
 */
public abstract class ServiceBusConnector extends CornerstoneConfigurable
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
		    if (isConnected()) {
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
    	if (isConnectOnStartup()) {
            connect();
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
     * Return the primary session associated to the connection.  Every connection should have exactly one session
     * that it creates on connect associated to it...
     *
     * @return the primary session
     */
    public abstract Session getSession();

    /**
     * Create a secondary session.  Most clients will prefer to use the connection's primary session.
     *
     * @return a secondary session
     */
    public abstract Session createSession();


    /**
     * Start the connection.
     */
    protected abstract void startConnection();

    /**
     * Stop the connection
     */
    protected abstract void stopConnection();

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

    protected abstract void close();

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

        close();

        notifyConnectionStatusChange();
    }


    /**
     * Determine if we're connected to the bus.
     *
     * @return true if connected (including disconnect pending), false otherwise.
     */
    public abstract boolean isConnected();

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

    protected abstract void initConnection();

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
                initConnection();

                if (_isAutoReconnectPending)
                {
                    _isAutoReconnectPending = false;
                    startConnection();

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
                        startConnection();
                    } catch (Exception e) {
                    	throw new RuntimeException(e);
                    }
                }

                gotConnection = true;
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

        close();

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
    public JndiInitialContextFactory getInitialContextFactory()
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
