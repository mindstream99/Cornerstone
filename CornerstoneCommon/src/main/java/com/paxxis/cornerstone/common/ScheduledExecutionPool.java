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

package com.paxxis.cornerstone.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * 
 * @author Robert Englander
 *
 */
public class ScheduledExecutionPool implements ScheduledExecutor {
    private static final Logger logger = Logger.getLogger(ScheduledExecutionPool.class);

    private ScheduledExecutorService scheduledExecutor = null;
    private int poolSize = 1;
    
    public ScheduledExecutionPool() {
    }

	public ScheduledFuture<?> schedule(Runnable runnable, int delay, TimeUnit timeUnit) {
		return scheduledExecutor.schedule(runnable, delay, timeUnit);
	}
    
    public void setPoolSize(int val) {
    	if (val > 0) {
    		poolSize = val;
    	} else {
    		logger.warn("ScheduledExecutionPool poolSize property can't be " + val + ". Using poolSize of " + poolSize);
    	}
    }

    public int getPoolSize() {
    	return poolSize;
    }

    public void initialize() {
        scheduledExecutor = Executors.newScheduledThreadPool(poolSize);
    }
    
    public void destroy() {
    	scheduledExecutor.shutdownNow();
    }
}


