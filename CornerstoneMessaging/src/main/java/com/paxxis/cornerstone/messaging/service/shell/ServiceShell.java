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

package com.paxxis.cornerstone.messaging.service.shell;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import com.paxxis.cornerstone.database.DatabaseUpdater;
import com.paxxis.cornerstone.messaging.service.IServiceBusManager;
import com.paxxis.cornerstone.messaging.service.IServiceController;

/**
 * Shell provides a basic command line interface to administrative functions.
 * 
 * @author Robert Englander
 *
 */
public class ServiceShell {

	private CommandLine commandLine = null;
	private Options options = null;
	
	public ServiceShell(String[] args) throws Exception {
		options = initialize(args);

		CommandLineParser parser = new PosixParser();
		commandLine = parser.parse(options, args, false);
	}

	/**
	 * Sub classes override this method in order to add more options
	 * to the option group before the command line is parsed.
	 */
	protected void addOptions(OptionGroup group) {
	}
	
	private Options initialize(String[] args) {
		OptionGroup group = new OptionGroup();

		Option helpOpt = new Option(null, "help", false, "this message");
		helpOpt.setArgs(2);
		helpOpt.setValueSeparator(',');
		group.addOption(helpOpt);

		Option opt = new Option("sh", "shutdown", true, "the service to shut down");
		opt.setArgs(2);
		opt.setValueSeparator(',');
		group.addOption(opt);

		Option updateOpt = new Option(null, "dbupdate", false, "the database update parameters");
		updateOpt.setArgs(3);
		updateOpt.setValueSeparator(',');
		group.addOption(updateOpt);
		
		Option unbindOpt = new Option(null, "unbind", true,
				"the unbind option to be used ONLY if the service cannot be shut down gracefully");
		unbindOpt.setArgs(2);
		unbindOpt.setValueSeparator(',');
		group.addOption(unbindOpt);
		addOptions(group);
		
		Options options = new Options();
		options.addOptionGroup(group);
		return options;
	}
	
	protected CommandLine getCommandLine() {
		return commandLine;
	}
	
	protected Options getOptions() {
	    return options;
	}
	
	/**
	 * Sub classes override this method in order to process their specific commands.
	 */
	protected boolean process() throws Exception {
	    return false;
	}

	protected final void execute() throws Exception {
		if (processHelp(commandLine) || processShutdown(commandLine) || processDatabaseUpdates(commandLine) || processUnbind(commandLine)
				|| process()) {
		    //currently we are assuming once something is processed it is finished...
		    return;
		}
	    printHelp();
	}
	
	private boolean processUnbind(CommandLine cmd) throws Exception {
		String[] vals = cmd.getOptionValues("unbind");
		if (vals != null) {
			if (vals.length != 2) {
				throw new ParseException("Invalid argument for unbind option");
			}
			doUnbind(vals);
			return true;
		}
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doUnbind(String[] vals) throws NamingException {
		String url = "rmi://localhost:" + vals[1];
		String serviceName = vals[0];
		System.out.println("Unbinding " + url + "/" + serviceName);
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
		env.put(Context.PROVIDER_URL, url);

		Context ictx = new InitialContext(env);
		try {
			ictx.unbind(serviceName);
			System.out.println("Done");
		} catch (Exception ex) {
			System.err.println(ex.getMessage() + ex.getCause().getMessage());
		}
	}

	private boolean processDatabaseUpdates(CommandLine cmd) throws Exception {
		String vals[] = getCommandLine().getOptionValues("dbupdate");
		if (vals != null) {
			if (vals.length != 2 && vals.length != 3) {
				throw new ParseException("Invalid argument for database update option");
			}
			doDatabaseUpdate(vals);
			return true;
		}
		
		return false;
	}
	
	private void doDatabaseUpdate(String[] inputs) {
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.registerShutdownHook();
        ctx.refresh();
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
        xmlReader.loadBeanDefinitions(new FileSystemResource(inputs[0]));

        DatabaseUpdater updater = ctx.getBean(DatabaseUpdater.class);
        
        String target = null;
        if (inputs.length == 3) {
        	target = inputs[2];
        }
        
        updater.update(inputs[1], target);

        ctx.close();
	}
	
	private boolean processShutdown(CommandLine cmd) throws Exception {
		String[] vals = cmd.getOptionValues("sh");
		if (vals != null) {
			if (vals.length != 2) {
				throw new ParseException("Invalid argument for shutdown option");
			}
			doShutdown(vals);
			return true;
		}
		return false; 
	}
	
	private boolean processHelp(CommandLine cmd) throws Exception {
	    if (cmd.hasOption("help")) {
	        printHelp();
	        return true;
	    }
	    return false;
	}
	
    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java " + this.getClass().getName(), options);
    }
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void doShutdown(String[] vals) throws Exception {
		StringBuilder buf = new StringBuilder("service:jmx:rmi://localhost/jndi/rmi://localhost:");
		String serviceName = vals[0];
		
		buf.append(vals[1]).append("/").append(serviceName);
		String serviceUrl = buf.toString();
		
		JMXServiceURL url = new JMXServiceURL(serviceUrl);
		JMXConnector jmxc = null;
		try {
			jmxc = JMXConnectorFactory.connect(url, null);
			
		} catch (Exception e) {
			throw new Exception("Unable to establish JMX connection at " + serviceUrl);
		}
		
		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
		
		Set<ObjectInstance> mBeansSet = mbsc.queryMBeans(new ObjectName(serviceName + ":*"), null);
		List<IServiceController> serviceProxies = new ArrayList<IServiceController>();
		Class serviceBusInterface = Class.forName(IServiceBusManager.class.getName());
		Class serviceControllerInterface = Class.forName(IServiceController.class.getName());

		for (ObjectInstance mBeanObject : mBeansSet) {
			ObjectName mbeanName = mBeanObject.getObjectName();
			Class mbeanClass = Class.forName(mBeanObject.getClassName());
			if (serviceBusInterface.isAssignableFrom(mbeanClass)) {
				IServiceBusManager requestConnector = JMX.newMBeanProxy(mbsc, mbeanName, IServiceBusManager.class, true);
				System.out.print(mbeanName + " terminating....");
				requestConnector.disconnect();
				while (true) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException ie) {

					}

					if (!requestConnector.isConnected()) {
						break;
					}
				}
				System.out.println(" Done");
			} else if (serviceControllerInterface.isAssignableFrom(mbeanClass)) {
				// save off the service proxies to make sure we disconnect
				// all connectors before shutting down the service itself
				IServiceController mbeanProxy = JMX.newMBeanProxy(mbsc, mbeanName, IServiceController.class, true);
				serviceProxies.add(mbeanProxy);
			}
		}

		for (IServiceController mbeanProxy : serviceProxies) {
			try {
				mbeanProxy.shutdown();
			} catch (UndeclaredThrowableException ex) {
			}
		}

		System.out.println("Service terminated");
	}

}
