package org.jamsim.ascape.stats;

import java.io.Serializable;

public interface StatsPredicate<T> extends Serializable {

	String getName();
	
	boolean getValue(T pParam);
	
}
