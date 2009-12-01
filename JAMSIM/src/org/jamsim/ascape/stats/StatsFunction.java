package org.jamsim.ascape.stats;

import java.io.Serializable;

public interface StatsFunction<T> extends Serializable {

	String getName();
	
	double getValue(T pParam);
	
}
