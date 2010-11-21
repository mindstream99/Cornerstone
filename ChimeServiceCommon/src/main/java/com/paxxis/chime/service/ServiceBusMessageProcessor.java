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

package com.paxxis.chime.service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Processes messages using a thread pool.  The size of the
 * thread pool indicates the number of messages that can be
 * processed concurrently.
 *
 * @author Robert Englander  
 */
public class ServiceBusMessageProcessor {
	
	/**
	 * This is a blocking queue that blocks on the offer method.  Normally the offer method will
	 * return false if the element can't be added.  The ThreadPoolExecutor calls offer, and we want to block,
	 * so offer is overridden so that it calls put instead.
	 * 
	 */
	private static class MessageBlockingQueue<M> extends ArrayBlockingQueue<M> {
		private static final long serialVersionUID = 1L;

		public MessageBlockingQueue(int capacity) {
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
    
    private int poolSize;
    private int maxMessages;
    
    
    /**
     * Monitors the ExecutorService until it is terminated, at
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
    
    /**
     * Constructor.
     *
     * @param poolSize the size of the thread pool.  
     */
    public ServiceBusMessageProcessor(int poolSize, int max) {
        this.poolSize = poolSize;
        maxMessages = max;
        if (maxMessages == 0) {
        	maxMessages = poolSize;
        }
        
        executor = new ThreadPoolExecutor(poolSize, poolSize, 0, TimeUnit.MILLISECONDS, 
        						new MessageBlockingQueue<Runnable>(maxMessages));
    }

    /**
     * Submit a message for processing
     *
     * @param message 
     */
    public void submit(Runnable message) {
        executor.execute(message);
    }
    
    public void halt() {
        isHalted = true;
        List<Runnable> unprocessed = executor.shutdownNow();
        halted.addAll(unprocessed);
    }
    
    public boolean isHalted() {
        return isHalted;
    }
    
    public void restart() {
        if (isHalted) {
            executor = new ThreadPoolExecutor(poolSize, poolSize, 0, TimeUnit.MILLISECONDS, 
					new MessageBlockingQueue<Runnable>(maxMessages));
            
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
     * Determine if the request processor is shut down.
     *
     * @return true if shutdown, false otherwise.
     */
    public boolean isShutdown() {
        return executor.isTerminated();
    }
}

