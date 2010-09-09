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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Processes messages using a thread pool.  The size of the
 * thread pool indicates the number of messages that can be
 * processed concurrently.
 *
 * @author Robert Englander  
 */
public class ServiceBusMessageProcessor
{
    // the thread pool executor
    ExecutorService _executor = null;

    // halted (pending work from a halted _executor)
    ArrayList<Runnable> _halted = new ArrayList<Runnable>();
    boolean _isHalted = false;
    
    int _poolSize;
    
    /**
     * Monitors the ExecutorService until it is terminated, at
     * which point it informs the registered listener and then
     * terminates itself.
     */
    class ShutdownMonitor extends Thread
    {
        // the registered listener
        ShutdownListener _listener;
        
        /**
         * Constructor
         *
         * @param listener the shutdown listener
         */
        ShutdownMonitor(ShutdownListener listener)
        {
            _listener = listener;
        }
        
        /**
         * Checks the state of the ExecutorService instance on a 1 second tick.  This
         * is probably equivalent to using the _executor.awaitTermination(...) method,
         * but this gives us more opportunity for control if we need it.
         */
        public void run()
        {
            while (true)
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException ie)
                {}
                
                if (_executor.isTerminated())
                {
                    break;
                }
            }
            
            _listener.onShutdownComplete();
        }
    }
    
    /**
     * Constructor.
     *
     * @param poolSize the size of the thread pool.  
     * 
     * @throws IllegalArgumentException if the poolSize is less than 1.
     */
    public ServiceBusMessageProcessor(int poolSize) throws IllegalArgumentException
    {
        _poolSize = poolSize;
        _executor = Executors.newFixedThreadPool(poolSize);
    }

    /**
     * Submit a message for processing
     *
     * @param message 
     */
    public void submit(Runnable message) throws RejectedExecutionException
    {
        _executor.execute(message);
    }
    
    public void halt()
    {
        _isHalted = true;
        List<Runnable> unprocessed = _executor.shutdownNow();
        _halted.addAll(unprocessed);
    }
    
    public boolean isHalted()
    {
        return _isHalted;
    }
    
    public void restart()
    {
        if (_isHalted)
        {
            _executor = Executors.newFixedThreadPool(_poolSize);
            
            for (Runnable work : _halted)
            {
                submit(work);
            }
            
            _halted.clear();
        }
    }
    
    /**
     * Initiate a controlled shutdown.
     *
     * @param listener the listener that gets informed when the
     * shutdown is complete.
     */
    public void shutdown(ShutdownListener listener)
    {
        // get a shutdown monitor started before we aske the
        // _executor instance to shut down
        ShutdownMonitor mon = new ShutdownMonitor(listener);
        mon.start();
        
        _executor.shutdown();
    }
    
    /**
     * Determine if the request processor is shut down.
     *
     * @return true if shutdown, false otherwise.
     */
    public boolean isShutdown()
    {
        return _executor.isTerminated();
    }
}

