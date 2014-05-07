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
package com.paxxis.cornerstone.messaging.common.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TemporaryQueue;

public class JMSDestination implements com.paxxis.cornerstone.messaging.common.Destination {


    private Destination destination;
    private boolean temporary;

    public JMSDestination(Destination destination) {
        this(destination, false);
    }

    public JMSDestination(Destination destination, boolean temporary) {
        this.destination = destination;
        this.temporary = temporary;
    }

    @Override
    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public void delete() {
        if (destination instanceof TemporaryQueue) {
            try {
                ((TemporaryQueue) destination).delete();
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Destination getDestination() {
        return destination;
    }
}
