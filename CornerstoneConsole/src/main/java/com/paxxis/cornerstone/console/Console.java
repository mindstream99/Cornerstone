package com.paxxis.cornerstone.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.paxxis.cornerstone.base.management.ConfigurationChange;
import com.paxxis.cornerstone.base.management.ConfigurationChangeEvent;
import com.paxxis.cornerstone.database.DatabaseConnection;
import com.paxxis.cornerstone.database.DatabaseConnectionPool;
import com.paxxis.cornerstone.database.IDataSet;
import com.paxxis.cornerstone.messaging.service.DestinationSender;

public class Console {
    private static final Logger logger = Logger.getLogger(Console.class);

    private DestinationSender sender;
    private DatabaseConnectionPool dbPool;
    
    public static void main(String[] args) throws Exception {
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	String propName = null;
    	String propValue = null;
    	
    	try {
        	System.out.print("Enter the property name: ");
            propName = br.readLine();

            System.out.print("Enter the property value: ");
            propValue = br.readLine();
        } catch (IOException ioe) {
            System.exit(1);
        }
    	Console console = new Console();
    	console.updateValue(propName, propValue);
    	System.exit(0);
    }
    
    @SuppressWarnings("unchecked")
	public Console() {
        FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext("CornerstoneConsoleFactory.xml");
        ctx.registerShutdownHook();
        ctx.getBeanFactory().preInstantiateSingletons();
        sender = (DestinationSender) ctx.getBean("publisherPool");
        dbPool = (DatabaseConnectionPool)ctx.getBean("configurationPool");
    }

    private void updateValue(String name, Serializable value) {
    	try {
    		DatabaseConnection connection = dbPool.borrowInstance(this);
    		
    		String query = "select value from Configuration where name = '" + name + "'";
    		IDataSet dataSet = connection.getDataSet(query, true);
    		String sql;
    		if (dataSet.next()) {
    			String oldValue = dataSet.getFieldValue("value").asString();
    			System.out.println("Previous value for " +name+ " is " + oldValue + ".");

    			sql = "update Configuration set value = '" + value + "' where name = '" +name+ "'";
    		} else {
    			sql = "insert into Configuration values ('" + name + "','" + value + "','')";
    		}

    		connection.startTransaction();
    		connection.executeStatement(sql);
    		connection.commitTransaction();

    		dataSet.close();
    		dbPool.returnInstance(connection, this);
    		System.out.println("New value for " +name+ " is " + value + ".");
    		
            ConfigurationChangeEvent event = new ConfigurationChangeEvent();
            ConfigurationChange change = new ConfigurationChange();
            change.setName(name);
            change.setNewValue(value);
            event.addConfigurationChange(change);
            sender.publish(event);
    		
    	} catch (Exception e) {
    		logger.error(e);
    	}
    	
    	System.exit(0);
    }

    public DestinationSender getQueueSender() {
    	return sender;
    }
    
    public void setQueueSender(DestinationSender sender) {
        this.sender = sender;
    }
}
