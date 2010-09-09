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

package com.paxxis.chime.common;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Used to wait for an object to be supplied
 * by another thread.  It's used to synchronize a producer and
 * consumer thread and deliver the data to the consumer.
 *
 * @author Robert Englander
 */
public class DataLatch extends CountDownLatch
{
    // the object that is being synchronized on
    private Object _obj = null;

    public DataLatch() {
        this(1);
    }

    public DataLatch(int seed) {
        super(seed);
    }

    /**
     * Block until the object is available.  This is called
     * by the consumer side of the interaction.
     *
     * @return the object
     */
    public Object waitForObject(long timeout) {
        try {
            await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            // no-op is correct behavior
        }

        // grab the object for return to the caller
        Object result = null;
        synchronized (this) {
            result = _obj;
        }

        // set the object variable to null so that this
        // monitor can be used again
        _obj = null;

        return result;
    }

    /**
     * Block until the object is available.  This is called
     * by the consumer side of the interaction.
     *
     * @return the object
     */
    public Object waitForObject() {
        return waitForObject(0);
    }

    /**
     * Set the object that the threads are synchronizing on.  This
     * is called by the producer side of the interaction.
     *
     * @param obj the object
     */
    public void setObject(Object obj)
    {
        synchronized (this) {
            _obj = obj;
        }

        countDown();
    }
}

