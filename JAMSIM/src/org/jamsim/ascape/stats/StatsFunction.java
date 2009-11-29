package org.jamsim.ascape.stats;

public interface StatsFunction<T> {

	String getName();
	
	double getValue(T scapeMember);
	
}
