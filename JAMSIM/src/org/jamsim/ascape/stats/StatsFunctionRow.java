package org.jamsim.ascape.stats;

import org.apache.commons.lang.mutable.MutableDouble;
import org.ascape.model.Scape;

/**
 * A stats row representing a function and a mutable denominator value. Can be
 * combined with a predicate to produce a {@link CollectorFunction} via a call
 * to {@link #getCollectorFunction(StatsPredicate, Scape)}.
 * 
 * @param <T>
 *            scape member
 * @author Oliver Mannion
 * @version $Revision$
 */
public class StatsFunctionRow<T> {

	private final String name;
	private final StatsFunction<T> function;
	private MutableDouble denominator;

	/**
	 * Construct {@link StatsFunctionRow} with the same name as its function.
	 * 
	 * @param function
	 *            value function
	 * @param denominator
	 *            mutable double
	 */
	public StatsFunctionRow(StatsFunction<T> function,
			MutableDouble denominator) {
		this(function.getName(), function, denominator);
	}

	/**
	 * Construct {@link StatsFunctionRow}.
	 * 
	 * @param name
	 *            name
	 * @param function
	 *            value function
	 * @param denominator
	 *            mutable double
	 */
	public StatsFunctionRow(String name, StatsFunction<T> function,
			MutableDouble denominator) {
		this.name = name;
		this.function = function;
		this.denominator = denominator;
	}

	/**
	 * Get a {@link CollectorFunction} that is the product of this function row
	 * and denominator, and the supplied predicate.
	 *  
	 * @param predicate
	 *            predicate
	 * @param iteratingScape
	 *            iterating scape. If specified produces a
	 *            {@link CollectorFunctionPerIteration}, otherwise produces a
	 *            {@link CollectorFunction}.
	 * @return collector function
	 */
	public CollectorFunction<T> getCollectorFunction(
			StatsPredicate<T> predicate, Scape iteratingScape) {
		if (iteratingScape == null) {
			return new CollectorFunction<T>(name, function, predicate,
					denominator);
		} else {
			return new CollectorFunctionPerIteration<T>(name, function,
					predicate, denominator, iteratingScape);
		}

	}
}
