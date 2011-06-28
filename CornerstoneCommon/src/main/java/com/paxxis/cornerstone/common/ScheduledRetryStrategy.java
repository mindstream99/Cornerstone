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

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * 
 * @author Rob Englander
 *
 */
public class ScheduledRetryStrategy implements RetryStrategy {

	private int retryMax = 5;
	private long retryIncrement = 100;
	private TimeUnit retryUnits = TimeUnit.MILLISECONDS;
	private int retryFactor = 5;

	/** the retry execution pool */
	private ScheduledExecutionPool retryExecutor;

	/** indicates if a private retry executor instance is in use instead of an injected instance */
	private boolean privateRetryExecutor = true;

	public ScheduledRetryStrategy() {
	}

	public void setRetryMax(int retryMax) {
		this.retryMax = retryMax;
	}

	public void setRetryIncrement(long retryIncrement) {
		this.retryIncrement = retryIncrement;
	}

	public void setRetryUnits(String retryUnits) {
		this.retryUnits = TimeUnit.valueOf(retryUnits.toUpperCase());
	}

	public void setRetryFactor(int retryFactor) {
		this.retryFactor = retryFactor;
	}

	public void setRetryExecutor(ScheduledExecutionPool retryExecutor) {
		this.retryExecutor = retryExecutor;
		privateRetryExecutor = false;
	}

	public void initialize() {
		if (privateRetryExecutor) {
			retryExecutor = new ScheduledExecutionPool();
		}
	}

	public void destroy() {
		if (privateRetryExecutor) {
			retryExecutor.destroy();
		}
	}

	public void execute(Retryable retryable) {
		if (retryable.getRetryCount() == retryMax) {
			Logger.getLogger(ScheduledRetryStrategy.class).warn("[" + Thread.currentThread().getName() + "] Giving up on " + retryable.toString());
		} else {
			Random rand = new Random();
			long wait = retryIncrement * (1 + rand.nextInt(retryFactor));
			retryExecutor.schedule(retryable, wait, retryUnits);
			Logger.getLogger(ScheduledRetryStrategy.class).info("[" + Thread.currentThread().getName() + "] Retry " + retryable.toString() + " in " + wait + " msecs");
		}
	}
}
