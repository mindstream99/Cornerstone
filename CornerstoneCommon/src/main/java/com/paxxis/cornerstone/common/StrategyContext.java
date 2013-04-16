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
