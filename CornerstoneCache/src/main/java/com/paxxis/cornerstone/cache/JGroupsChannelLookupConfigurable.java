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
package com.paxxis.cornerstone.cache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.infinispan.remoting.transport.jgroups.JGroupsChannelLookup;
import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;

import com.paxxis.cornerstone.common.BeanNameAwareConfigurable;

/**
 * Facilitates the creation of a JChannel from properties found in the configuration database.  Currently
 * this class only supports one Channel per classloader...
 * 
 * @author Matthew Pflueger
 */
public class JGroupsChannelLookupConfigurable extends BeanNameAwareConfigurable implements JGroupsChannelLookup {

    private static final Logger logger = Logger.getLogger(JGroupsChannelLookupConfigurable.class);
    
    private String configuration;
    private boolean startAndConnect = true;
    private boolean stopAndDisconnect = false;
    
    private static JChannel channel;
    
    public JGroupsChannelLookupConfigurable() {
        //we must have a public no-args constructor for Infinispan to instantiate this class dynamically...
    }

    @Override
    public void initialize() {
        super.initialize();
        
        /*
         * We are synchronizing on the class as the channel we construct is static and we want to
         * prevent constructing more than one per classloader at the moment.  This class name is
         * passed to Infinispan as part of its transport properties - Infinispan then dynamically
         * instantiates this class and asks for the channel and we give back the one we constructed
         * earlier when Spring called initialize...
         */
        synchronized (this.getClass()) {
            if (channel != null) {
                logger.error("JGroups channel already initialized! Currently only one channel per classloader is supported!");
                return;
            }
                
            if (configuration == null) {
                //we need to construct a configuration from properties in the db
                configuration = initConfiguration();
            }
        
            try {
                logger.debug("Initializing JChannel with configuration: " + configuration);
                channel = new JChannel(configuration);
            } catch (ChannelException e) {
                throw new RuntimeException("Error creating JGroups channel", e);
            }
        }
    }

    protected String initConfiguration() {
        Map<String, Object> items = getPrefixedConfigurationValues();
        
        //for JGroupsChannel each config item should be like <prefix>.<ordinal>.<protocol>.<property>
        //because order of the protocols is important for the channel
        //the bottom most protocol needs to be first so for example to define a channel with three
        //protocols one would add the following in the db
        //myChannelBeanName.1.TCP.loopback                          true
        //myChannelBeanName.2.FRAG.frag_size                        60000
        //myChannelBeanName.3.pbcast.STREAMING_STATE_TRANSFER 
        //NOTE: the last one above indicates a protocol with no properties which is valid
        //NOTE: the myChannelBeanName was stripped off in getPrefixedConfigurationValues()
        //NOTE: the ordinals do not need to be in strict numerical order (you can leave gaps)

        
        String[] protocolNames = new String[20];
        @SuppressWarnings("unchecked")
        Map<String, String>[] protocolProperties = new Map[20];
        
        for (Map.Entry<String, Object> item : items.entrySet()) {
            String message = 
                        "Not a JGroups channel config item " + 
                        item.getKey() + 
                        " all items must be in the format <prefix>.<ordinal>.<protocol>.<property>";

            String[] parts = item.getKey().split("\\.");
            if (parts.length < 2) {
                //must have at least an ordinal and a protocol name
                logger.debug(message);
                continue;
            }
            
            int protocolPosition = 0;
            try {
                protocolPosition = Integer.parseInt(parts[0]);
            } catch (NumberFormatException nfe) {
                logger.debug(message, nfe);
                continue;
            }
            
            
            String protocol = "";
            int endProtocolIndex = 1;
            for (; endProtocolIndex < parts.length; endProtocolIndex++) {
                protocol = protocol + parts[endProtocolIndex];
                if (Character.isUpperCase(parts[endProtocolIndex].charAt(0))) {
                    //we found the protocol as it is a Java class and class names are supposed to
                    //start with an uppercase letter...
                    break;
                }
                //lowercase letter means we have a package, lets add a period and keep going
                protocol = protocol + ".";
            }
            
            int propertyIndex = endProtocolIndex + 1;

            //ensure we don't hit an ArrayIndexOutOfBounds later... 
            protocolNames = ensureCapacity(protocolPosition, protocolNames);
            protocolProperties = ensureCapacity(protocolPosition, protocolProperties);
            
            String existingProtocolName = protocolNames[protocolPosition];
            if (existingProtocolName != null && !existingProtocolName.equals(protocol)) {
                //sanity check against a bad configuration in the db - somebody assigned two different
                //protocols to the same position
                throw new RuntimeException(
                        "Error in configuration - protocol " + 
                        existingProtocolName +
                        " and " +
                        protocol +
                        " both assigned to position " +
                        protocolPosition);
            }
            
            protocolNames[protocolPosition] = protocol;            
            if (propertyIndex >= parts.length) {
                //we have a protocol with no properties which is valid...
                continue;
            }
            
            //the rest is property + value - if we have property we must have a value...
            if (item.getValue() == null) {
                throw new IllegalArgumentException(message + " - no value for property specified");
            }
            if (!(item.getValue() instanceof String)) {
                throw new IllegalArgumentException(
                        message + 
                        " - value " + 
                        item.getValue() + 
                        " is not a string");
            }
            
            String propertyKey = parts[propertyIndex];
            for (++propertyIndex; propertyIndex < parts.length; propertyIndex++) {
                propertyKey = propertyKey + "." + parts[propertyIndex];
            }
            
            Map<String, String> props = protocolProperties[protocolPosition];
            if (props == null) {
                props = new HashMap<String, String>();
            }
            props.put(propertyKey, (String) item.getValue());
            protocolProperties[protocolPosition] = props;
            
        }
        
        //now lets create the old-style config string
        return createConfiguration(protocolNames, protocolProperties);
    }

    private <T> T[] ensureCapacity(int minCapacity, T[] array) {
        //ripped from ArrayList
        int oldCapacity = array.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3)/2 + 1;
    	    if (newCapacity < minCapacity) {
    	        newCapacity = minCapacity;
    	    }
            // minCapacity is usually close to size, so this is a win:
            return Arrays.copyOf(array, newCapacity);
        }
        return array;
    }
    
    private String createConfiguration(
            String[] protocolNames, 
            Map<String, String>[] protocolProperties) {

        StringBuilder builder = new StringBuilder();
        int index = -1;
        for (String protocolName : protocolNames) {
            index++;
            if (protocolName == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(":");
            }
            builder.append(protocolName);
            
            Map<String, String> props = protocolProperties[index];
            if (props == null) {
                continue;
            }
            
            builder.append("(");
            int numProps = 0;
            for (Map.Entry<String,String> prop : props.entrySet()) {
                if (numProps > 0) {
                    builder.append(";");
                }
                builder.append(prop.getKey())
                    .append("=")
                    .append(prop.getValue());
                numProps++;
            }
            builder.append(")");
        }
        
        return builder.toString();
    }
    
    /** 
     * Configuration can be the following:
     * <p>
     * an an old style property string like
     * <pre>UDP(in_port=5555;out_port=4445):FRAG(frag_size=1024)</pre>
     * </p>
     * 
     * <p>
     * a string representing a system resource containing a JGroups XML configuration (found on
     * classpath)
     * </p>
     * 
     * <p>
     * a string representing a URL pointing to a JGroups XML configuration
     * </p>
     * 
     * <p>
     * or a string representing a file name that contains a JGroups XML configuration
     * </p>
     * 
     * <p>
     * or nothing and it will be initialized from the database
     * </p>
     */
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public Channel getJGroupsChannel(Properties p) {
        //NOTE: we ignore the Properties at the moment because we are assuming one Channel per classloader
        //we are also ignoring the fact that the bean may have different configurations set on
        //on it as, again, we are assuming one Channel per classloader
        return channel;
    }

    @Override
    public boolean shouldStartAndConnect() {
        return isStartAndConnect();
    }

    @Override
    public boolean shouldStopAndDisconnect() {
        return isStopAndDisconnect();
    }

    public boolean isStartAndConnect() {
        return startAndConnect;
    }

    public void setStartAndConnect(boolean startAndConnect) {
        this.startAndConnect = startAndConnect;
    }

    public boolean isStopAndDisconnect() {
        return stopAndDisconnect;
    }

    public void setStopAndDisconnect(boolean stopAndDisconnect) {
        this.stopAndDisconnect = stopAndDisconnect;
    }
}
