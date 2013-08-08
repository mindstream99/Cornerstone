package com.paxxis.cornerstone.common;

public abstract class RunWithPriority implements Comparable<RunWithPriority>, Runnable {
	
	public abstract int getPriority();

	@Override
	public int compareTo(RunWithPriority other) {
		if (other == null) {
			return 0;
		}
		
		return getPriority() - other.getPriority();
	}
}
