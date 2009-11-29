package org.jamsim.ascape.stats;

public interface StatsPredicate<T> {

	String getName();
	
	boolean getValue(T scapeMember);
	
}
