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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


/**
 * Used to wait for an object to be supplied
 * by another thread.  It's used to synchronize a producer and
 * consumer thread and deliver the data to exactly one consumer.
 *
 * @author Robert Englander
 */
public class DataLatch
{
    private static final Logger logger = Logger.getLogger(DataLatch.class);

    // the object that is being waited for
    private Object obj;
    private CountDownLatch latch = new CountDownLatch(1);
    private boolean interrupted = false;
    private boolean timedout = false;
    private RuntimeException exception = null;
    private Thread waiting;


    /**
     * Block until the object is available. This method does not failfast and consequently may
     * return null. 
     *
     * @return the object
     */
    public Object waitForObject() {
        return waitForObject(0, false);
    }

    /**
     * Block until the object is available or until the timeout is reached. 
     * This method does not failfast and consequently may return null.
     *
     * @return the object
     */
    public Object waitForObject(long timeout) {
        return waitForObject(timeout, false);
    }
    
    /**
     * Block until the object is available failing with a runtime exception if failfast is true.
     * 
     * @param failfast
     * @return 
     */
    public Object waitForObject(boolean failfast) {
        return waitForObject(0, failfast);
    }
    
    /**
     * Block until the object is available or until the timeout is reached.  This is called
     * by the consumer side of the interaction. It is illegal to call this method from 
     * multiple threads.  It is also illegal to call this method more than once. If failfast 
     * is true a runtime exception is thrown if for any reason a non-null object cannot be
     * returned.
     *
     * @param timeout the timeout
     * @param failfast if true throw an exception if we cannot return a valid (non-null) object
     * @return the object
     * @throws TimeoutException on timeout if failfast is true
     * @throws InterruptedRuntimeException on interruption if failfast is true
     * @throws NullDataException on a null object being set if failfast is true
     */
    public Object waitForObject(long timeout, boolean failfast) {
        synchronized (this) {
            if (!isWaitable()) {
                IllegalStateException e = new IllegalStateException("Illegal state for consumption"); 
                logger.error(e);
                throw e;
            }
            this.waiting = Thread.currentThread();
        }

        try {
            if (timeout < 1) {
                //this effectively waits forever - normally zero or less means
                //wait forever but in CountDownLatch it means don't wait...
                timeout = Long.MAX_VALUE;
            }
            this.timedout = !this.latch.await(timeout, TimeUnit.MILLISECONDS);
            if (this.timedout) {
                this.exception = new TimeoutException("Timed out after " + timeout);
            }
        } catch (InterruptedException ex) {
            // no-op is correct behavior
            this.interrupted = true;
            this.exception = new InterruptedRuntimeException(ex);
        }

        synchronized (this) {
            // grab the object for return to the caller
            Object result = obj;
            // set internals to null so data latch cannot be reused
            obj = null;
            this.latch = null;
            this.waiting = null;
            if (result == null && this.exception == null) {
                this.exception = new NullDataException("Null object set on data latch");
            }
            if (failfast && this.exception != null) {
                throw this.exception;
            }
            return result;
        }
    }

    /**
     * Set the object that the threads are synchronizing on.  This
     * is called by the producer side of the interaction. Calling this
     * method more than once or after the consumer has already attempted to
     * retrieve the data has no effect.  
     *
     * @param obj the object
     */
    public synchronized void setObject(Object obj) {
        if (!isSettable()) {
            //no point to setting the object, the waiting thread is long gone...
            logger.warn("Trying to set data on latch that is not settable");
            return;
        }
        this.obj = obj;
        this.latch.countDown();
    }

    /**
     * Return the runtime exception associated with this data latch.
     * 
     * @return the exception or null if none
     */
    public synchronized RuntimeException getException() {
        return this.exception;
    }
    
    /**
     * Poll if the data has been set (no waiting).  To get the data call
     * waitForObject.
     */
    public synchronized boolean hasObject() {
        return this.obj != null;
    }

    /**
     * Will return true if the waiting thread was interrupted.
     */
    public synchronized boolean isInterrupted() {
        return this.interrupted;
    }

    /**
     * Will return true if the waiting thread timed-out.
     */
    public synchronized boolean hasTimedout() {
        return this.timedout;
    }

    /**
     * Will return true if this data latch is in a valid state for consumers
     */
    public synchronized boolean isWaitable() {
        return this.waiting == null && isSettable();
    }

    /**
     * Will return true if this data latch is in a valid state for producers
     */
    public synchronized boolean isSettable() {
        return this.latch != null;
    }

    /**
     * Convenience method to determine if data latch was successfully used
     * without time-out or interruption of consumer. This is in the case that
     * waitForObject returns null and you need to know if that is because
     * the a timeout or interrupt occurred.
     */
    public synchronized boolean isSuccessful() {
        return this.latch == null && !this.timedout && !this.interrupted;
    }

}

