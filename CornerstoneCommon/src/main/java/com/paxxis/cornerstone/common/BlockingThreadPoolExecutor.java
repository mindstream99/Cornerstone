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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.paxxis.cornerstone.service.ShutdownListener;

/**
 * Processes runnables using a blocking thread pool.
 *
 * @author Robert Englander  
 */
public class BlockingThreadPoolExecutor {
	
	/**
	 * This is a blocking queue that blocks on the offer method.  Normally the offer method will
	 * return false if the element can't be added.  The ThreadPoolExecutor calls offer, and we want to block,
	 * so offer is overridden so that it calls put instead.
	 * 
	 */
	private static class OfferBlockingQueue<M> extends ArrayBlockingQueue<M> {
		private static final long serialVersionUID = 1L;

		public OfferBlockingQueue(int capacity) {
			super(capacity);
		}

		public boolean offer(M element) {
			try {
				put(element);
			} catch (InterruptedException ie) {
				throw new RejectedExecutionException();
			}
			
			return true;
		}
	}
	
    // the thread pool executor
    private ThreadPoolExecutor executor = null;

    // halted (pending work from a halted _executor)
    private ArrayList<Runnable> halted = new ArrayList<Runnable>();
    private boolean isHalted = false;
    
    private String threadPoolName = null;
    private int poolSize = 10;
    private int maxRunnables = 10;
    
    
    /**
     * Monitors the executor until it is terminated, at
     * which point it informs the registered listener and then
     * terminates itself.
     */
    class ShutdownMonitor extends Thread {
        // the registered listener
        private ShutdownListener listener;
        
        /**
         * Constructor
         *
         * @param listener the shutdown listener
         */
        ShutdownMonitor(ShutdownListener listener) {
            this.listener = listener;
        }
        
        /**
         * Checks the state of the executor instance on a 1 second tick.  This
         * is probably equivalent to using the _executor.awaitTermination(...) method,
         * but this gives us more opportunity for control if we need it.
         */
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
                
                if (executor.isTerminated()) {
                    break;
                }
            }
            
            listener.onShutdownComplete();
        }
    }
    
    public BlockingThreadPoolExecutor() {
    }
    
    public void initialize() {
        executor = new ThreadPoolExecutor(
                            poolSize, 
                            poolSize, 
                            0, 
                            TimeUnit.MILLISECONDS, 
        					new OfferBlockingQueue<Runnable>(maxRunnables),
        					new DefaultThreadFactory(threadPoolName));
        executor.prestartAllCoreThreads();
    }

    /**
     * Submit a runnable for processing.  Since the executor is constructed with a OfferBlockingQueue,
     * this method will block if the waiting queue is full, returning only when the runnable can be accepted.
     *
     * @param message 
     */
    public void submit(Runnable runnable) {
        executor.execute(runnable);
    }
    
    public void halt() {
        isHalted = true;
        List<Runnable> unprocessed = executor.shutdownNow();
        halted.addAll(unprocessed);
    }
    
    public void setThreadPoolName(String threadPoolName) {
		this.threadPoolName = threadPoolName;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	public void setMaxRunnables(int maxRunnables) {
		this.maxRunnables = maxRunnables;
	}

	public boolean isHalted() {
        return isHalted;
    }
    
    public void restart() {
        if (isHalted) {
            executor = new ThreadPoolExecutor(
                                poolSize, 
                                poolSize, 
                                0, 
                                TimeUnit.MILLISECONDS, 
								new OfferBlockingQueue<Runnable>(maxRunnables),
								new DefaultThreadFactory(threadPoolName));
            
            for (Runnable work : halted) {
                submit(work);
            }
            
            halted.clear();
        }
    }
    
    /**
     * Initiate a controlled shutdown.
     *
     * @param listener the listener that gets informed when the
     * shutdown is complete.
     */
    public void shutdown(ShutdownListener listener) {
        // get a shutdown monitor started before we ask the
        // executor instance to shut down
        ShutdownMonitor mon = new ShutdownMonitor(listener);
        mon.start();
        
        executor.shutdown();
    }
    
    /**
     * Determine if the executor is shut down.
     *
     * @return true if shutdown, false otherwise.
     */
    public boolean isShutdown() {
        return executor.isTerminated();
    }

}

