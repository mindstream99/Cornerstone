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

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * An embedded app server.
 *
 * @author Robert Englander
 */
public class AppServer extends Thread {

    private Server server = null;
    private int port = 8080;
    private String warFile = "";

    @Override
    public void run() {

        try {
            server = new Server(port);

            WebAppContext webapp = new WebAppContext();
            
            webapp.setContextPath("/");
            webapp.setWar(warFile);
            server.setHandler(webapp);
            
            server.start();
            server.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setPort(int p) {
        port = p;
    }

    public void setWarFile(String file) {
        warFile = file;
    }

    public void initialize() {
        start();
    }

    @Override
    public void destroy() {
        if (server != null) {
            try {
                server.stop();
                server.destroy();
            } catch (Exception e) {

            }
        }
    }
}
