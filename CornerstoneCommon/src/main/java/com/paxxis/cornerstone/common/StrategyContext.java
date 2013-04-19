/*
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Paxxis Technology LLC
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
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.paxxis.cornerstone.base.Strategy;

public class StrategyContext {
	private static final Logger LOGGER = Logger.getLogger(StrategyContext.class);

	private List<Strategy> strategies = new ArrayList<Strategy>();

	public StrategyContext() {

	}

	public void setStrategies(Collection<? extends Strategy> strategies) {
		this.strategies.addAll(strategies);
	}

	public void initialize() {
	}

	public void destroy() {

	}

	@SuppressWarnings("unchecked")
	public <T extends Strategy> T getStrategy(Class<T> clazz) {
		T result = null;
		for (Strategy strategy : strategies) {
			if (clazz.isAssignableFrom(strategy.getClass())) {
				result = (T)strategy;
				break;
			}
		}
		return result;
	}

}
