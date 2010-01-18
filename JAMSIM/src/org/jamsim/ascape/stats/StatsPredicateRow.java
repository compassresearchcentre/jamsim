package org.jamsim.ascape.stats;

import org.apache.commons.lang.mutable.MutableDouble;
import org.ascape.model.Scape;

/**
 * A stats row that represents a predicate, a denominator function, and a
 * denominator value. Can be combined with a function to produce a
 * {@link CollectorFunction} via a call to
 * {@link #getCollectorFunction(StatsFunction, Scape)}.
 * 
 * 
 * @param <T>
 *            scape member
 * @author Oliver Mannion
 * @version $Revision$
 */
public class StatsPredicateRow<T> {

	private final String name;
	private final StatsPredicate<T> predicate;
	private final MutableDouble denominator;
	private final StatsFunction<T> denominatorFunction;

	/**
	 * Basic constructor. Name is assumed from the predicate. Denominator value
	 * is a new zero {@link MutableDouble}.
	 * 
	 * @param predicate
	 *            predicate used during {@link #incDenominatorValue(Object)}.
	 * @param denominatorFunction
	 *            denominator function used during
	 *            {@link #incDenominatorValue(Object)}.
	 */
	public StatsPredicateRow(StatsPredicate<T> predicate,
			StatsFunction<T> denominatorFunction) {
		this(predicate.getName(), predicate, denominatorFunction);
	}

	/**
	 * Constructor with name. Denominator value is a new zero
	 * {@link MutableDouble}.
	 * 
	 * @param name
	 *            name
	 * @param predicate
	 *            predicate used during {@link #incDenominatorValue(Object)}.
	 * @param denominatorFunction
	 *            denominator function used during
	 *            {@link #incDenominatorValue(Object)}.
	 */
	public StatsPredicateRow(String name, StatsPredicate<T> predicate,
			StatsFunction<T> denominatorFunction) {
		this(name, predicate, denominatorFunction, new MutableDouble(0));
	}

	/**
	 * Main constructor.
	 * 
	 * @param name
	 *            name
	 * @param predicate
	 *            predicate used during {@link #incDenominatorValue(Object)}.
	 * @param denominatorFunction
	 *            denominator function used during
	 *            {@link #incDenominatorValue(Object)}.
	 * @param denominator
	 *            denominator value
	 */
	public StatsPredicateRow(String name, StatsPredicate<T> predicate,
			StatsFunction<T> denominatorFunction, MutableDouble denominator) {
		this.name = name;
		this.predicate = predicate;
		this.denominatorFunction = denominatorFunction;
		this.denominator = denominator;
	}

	/**
	 * Get a {@link CollectorFunction} that is the product of this predicate row
	 * and denominator, and the supplied function.
	 * 
	 * @param function
	 *            function
	 * @param iteratingScape
	 *            iterating scape. If specified produces a
	 *            {@link CollectorFunctionPerIteration}, otherwise produces a
	 *            {@link CollectorFunction}.
	 * @return collector function
	 */
	public CollectorFunction<T> getCollectorFunction(
			StatsFunction<T> function, Scape iteratingScape) {
		if (iteratingScape == null) {
			return new CollectorFunction<T>(name, function, predicate,
					denominator);
		} else {
			return new CollectorFunctionPerIteration<T>(name, function,
					predicate, denominator, iteratingScape);
		}
	}

	/**
	 * Get the denominator value.
	 * 
	 * @return denominator
	 */
	public MutableDouble getDenominator() {
		return denominator;
	}

	/**
	 * If the scapeMember matches the predicate, then increment the denominator
	 * value by the denominator function.
	 * 
	 * @param scapeMember
	 *            scape member to test, and provides value via denominator
	 *            function
	 */
	public void incDenominatorValue(T scapeMember) {
		if (predicate == null || predicate.test(scapeMember)) {
			denominator.add(denominatorFunction.getValue(scapeMember));
		}
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(name).append(": ").append(denominator);
		return sbuf.toString();
	}
}
