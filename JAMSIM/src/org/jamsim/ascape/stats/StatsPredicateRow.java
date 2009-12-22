package org.jamsim.ascape.stats;

import org.apache.commons.lang.mutable.MutableDouble;

/**
 * A stats row that contains a predicate and a denominator function.
 * 
 * @param <T>
 *            scape member
 * @author Oliver Mannion
 * @version $Revision$
 */
public class StatsPredicateRow<T> {

	private final String name;
	private final StatsPredicate<T> predicate;
	private double denominator;
	private final StatsFunction<T> denominatorFunction;

	/**
	 * Default constructor.
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
	 * Constructor with name.
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
		this.name = name;
		this.predicate = predicate;
		this.denominatorFunction = denominatorFunction;
	}

	public String getName() {
		return name;
	}

	public StatsPredicate<T> getPredicate() {
		return predicate;
	}

	public String getDenominatorName() {
		return denominatorFunction.getName();
	}

	public MutableDouble getDenominator() {
		return new MutableDouble(denominator);
	}

	public void incDenominatorValue(T scapeMember) {
		if (predicate == null || predicate.test(scapeMember)) {
			denominator =
					denominator + denominatorFunction.getValue(scapeMember);
		}
	}

	public void setDenominator(double denominator) {
		this.denominator = denominator;
	}
}
