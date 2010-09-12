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

import java.sql.DriverManager;
import org.apache.activemq.broker.BrokerService;

/**
 *
 * @author Robert Englander
 */
public class MessageBroker {

    BrokerService broker = null;

    public void initialize() {
        broker = new BrokerService();

        try {
            // configure the broker
            broker.addConnector("tcp://localhost:61616");

            broker.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void destroy() {
        if (broker != null) {
            try {
                broker.stop();

                // this doesn't really belong here, but until an appropriate place is found...
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            } catch (Exception ex) {
                //Logger.getLogger(MessageBroker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
