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

package com.paxxis.chime.indexing;

/**
 * Periodically optimizes the indexes.
 *
 * @author Robert Englander
 */
public class IndexingOptimizer {

    class Optimizer extends Thread {

        private boolean shutdown = false;
        private int cycle;
        private int spindle = 0;

        public Optimizer(int cycle) {
            setName("IndexOptimizer");
            this.cycle = cycle;
        }

        public void terminate() {
            shutdown = true;
            interrupt();
        }

        @Override
        public void run() {

            while (true) {

                if (shutdown) {
                    break;
                }

                try {
                    sleep(cycle);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (shutdown) {
                    break;
                }

                try {
                    optimize();
                }
                catch (Exception ee)
                {}
            }
        }

        private void optimize() {
            Indexer.instance().optimize(spindle);
            spindle++;
            if (spindle == Indexer.getIndexNames().length) {
                spindle = 0;
            }
        }
    }

    private Optimizer optimizer;
    private int cycle = 300000;

    public void setCycle(int val) {
        cycle = 60000 * val;
    }

    /**
     * Called by the spring container
     */
    public void initialize() {
        optimizer = new Optimizer(cycle);
        optimizer.start();
    }

    /**
     * Called by the spring container
     */
    public void destroy() {
        optimizer.terminate();
    }
}
