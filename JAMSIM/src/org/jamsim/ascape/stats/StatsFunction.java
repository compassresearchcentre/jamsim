package org.jamsim.ascape.stats;

import java.io.Serializable;

/**
 * Returns a simulation double value on a scape member. 
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 * @param <T> type of scape member.
 */
public interface StatsFunction<T> extends Serializable {

	/**
	 * Value name.
	 * 
	 * @return value name
	 */
	String getName();

	/**
	 * Value.
	 * 
	 * @param member scape member
	 * @return value.
	 */
	double getValue(T member);

}
