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

package com.paxxis.cornerstone.service.monitoring;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.paxxis.cornerstone.base.monitoring.ServiceMetrics;
import com.paxxis.cornerstone.base.monitoring.ServiceMetricsEvent;
import com.paxxis.cornerstone.base.monitoring.TimestampedValue;
import com.paxxis.cornerstone.common.JavaObjectPayload;
import com.paxxis.cornerstone.common.ScheduledExecutionPool;
import com.paxxis.cornerstone.service.DestinationPublisherPool;
import com.paxxis.cornerstone.service.DestinationSender;
import com.paxxis.cornerstone.service.spring.CornerstoneService;

/**
 * Collects service metrics and publishes them to a service bus topic.
 * 
 * @author Rob Englander
 *
 */
public class MetricsCollector {

	private final static int DEFAULTMAXVALUES = 10;
	private final static int MINMAXVALUES = 1;
	private final static int DEFAULTCOLLECTIONFREQUENCY = 30000;
	private final static int MINCOLLECTIONFREQUENCY = 1000;
	
	/**
	 * This is used internally to associate a metric name, the getter method, and
	 * a list of timestamped metric values.
	 */
	private static class MetricData {
		String name;
		Method method;
		ServiceMetrics metrics;
	}

	/** the interface that contains metric getters */
	private String metricInterface = null;
	
	/** the source of the metrics (the object that implements metricInterface) */
	private Object metricSource = null;

	/** the maximum number of values to keep for each metric */
	private int maxValues = DEFAULTMAXVALUES;
	
	/** mapping of metric name to metric data */
	private HashMap<String, MetricData> metricDataMap = new HashMap<String, MetricData>();
	
	/** the collection frequency */
	private long collectionFrequency = DEFAULTCOLLECTIONFREQUENCY;

	/** the publisher pool for publishing metric events */
	private DestinationPublisherPool<DestinationSender> publisherPool = null;
	
	/** the scheduled executor to use */
	private ScheduledExecutionPool collectionExecutor = null;
	
	/** the service associated with this collector */
	private CornerstoneService service = null;
	
	/** the currently scheduled collection Future */
	private ScheduledFuture<?> future = null;
	
	public MetricsCollector() {
	}

	public void setService(CornerstoneService service) {
		this.service = service;
	}
	
	public void setInterface(String metricInterface) {
		this.metricInterface = metricInterface;
	}
	
	public void setSource(Object source) {
		this.metricSource = source;
	}
	
	public void setMaxValues(int maxValues) {
		if (maxValues < MINMAXVALUES) {
			throw new IllegalArgumentException("maxValues must be greater than " 
					+ MINMAXVALUES + ".");
		}

		this.maxValues = maxValues;
	}
	
	public void setCollectionFrequency(long frequency) {
		if (frequency < MINCOLLECTIONFREQUENCY) {
			throw new IllegalArgumentException("collectionFrequency must be greater or equal to " 
					+ MINCOLLECTIONFREQUENCY + ".");
		}

		this.collectionFrequency = frequency;
	}
	
	public void setPublisherPool(DestinationPublisherPool<DestinationSender> publisherPool) {
		this.publisherPool = publisherPool;
	}
	
	public void setCollectionExecutor(ScheduledExecutionPool collectionExecutor) {
		this.collectionExecutor = collectionExecutor;
	}
	
	public void initialize() {
		try {
			Class<?> clazz = Class.forName(metricInterface);
	        Method[] list = clazz.getMethods();
	        for (Method method : list) {
	        	if (isGetter(method)) {
	        		String metricName = getPropertyName(method);
	        		Class<?> metricType = getPropertyType(method);
	        		if (isSupportedType(metricType)) {
	        			MetricData data = new MetricData();
	        			data.name = metricName;
	        			data.method = method;
	        			data.metrics = new ServiceMetrics(metricName, maxValues);
	        			metricDataMap.put(metricName, data);
	        		}
	        	}
	        }
	        
        	Runnable r = new Runnable() {
        		public void run() {
        			// construct a metrics event message
        			ServiceMetricsEvent metricsEvent = new ServiceMetricsEvent();
        			metricsEvent.setServiceInstance(service.getServiceInstance());

        			Long start = System.currentTimeMillis();
        			for (String methodName : metricDataMap.keySet()) {
    	            	MetricData metric = metricDataMap.get(methodName);
        	            try {
							Serializable result = (Serializable)metric.method.invoke(metricSource, new Object[0]);
							TimestampedValue value = new TimestampedValue(result);
							
							// add this result to the list of values
							metric.metrics.addValue(value);
							
							// add it to the metrics event message
							metricsEvent.addMetrics(metric.metrics);
							
						} catch (Exception e) {
							Logger.getLogger(getClass()).error(e);
						}
        			}
        			
        			// publish the metric message
        			publisherPool.publish(metricsEvent, new JavaObjectPayload()); //ServiceMetricsEvent.class));
        			
        			// subtract the collection time from the frequency so that collections happen uniformly
        			long newFreq = collectionFrequency - (System.currentTimeMillis() - start);

        			// TODO adjust the collection frequency if the collection time costs more than a given
        			// percentage of the collection frequency
        			//if (newFreq < (collectionFrequency / 2)) {
        				// it took more than half the collection frequency time to collect metrics.
        			//} else {
        				
        			//}
        			future = collectionExecutor.schedule(this, newFreq, TimeUnit.MILLISECONDS);
        		}
        	};
        	future = collectionExecutor.schedule(r, collectionFrequency, TimeUnit.MILLISECONDS);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	
	}

	public static boolean isSupportedType(Class<?> metricType) {
		boolean supported = false;
		if (metricType.isPrimitive()) {
			if (!(metricType.equals(Byte.TYPE) || metricType.equals(Character.TYPE))) {
				supported = true;
			}
		} else {
			supported = metricType.equals(String.class);
		}
		
		return supported;
	}
	
	public static Class<?> getPropertyType(Method method) {
		return method.getReturnType();
	}
	
	public static String getPropertyName(Method method) {
		String methodName = method.getName();
		String name;
		if (methodName.startsWith("get")) {
			name = methodName.substring(3);
		} else { // "is"
			name = methodName.substring(2);
		}
		
		String propertyName = new String(new char[] {Character.toLowerCase(name.charAt(0))} );
		if (name.length() > 1) {
			propertyName += name.substring(1);
		}
		
		return propertyName;
	}
	
	public static boolean isGetter(Method method) {
		String methodName = method.getName();
		if (!(methodName.startsWith("get") || methodName.startsWith("is"))) {
			return false;
		}
		  
		if (methodName.startsWith("get")) {
			if (methodName.length() == 3 || !Character.isUpperCase(methodName.charAt(3))) {
				return false;
			}
		}
		  
		if (methodName.startsWith("is")) {
			if (methodName.length() == 2 || !Character.isUpperCase(methodName.charAt(2))) {
				return false;
			}
		}

		if (method.getParameterTypes().length != 0) {
			return false;  
		}
		  
		if (void.class.equals(method.getReturnType())) {
			return false;
		}
		  
		return true;
	}	
	
	public void destroy() {
		if (future != null && !future.isDone()) {
			future.cancel(true);
		}
	}
}
