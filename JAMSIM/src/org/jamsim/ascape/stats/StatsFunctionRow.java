package org.jamsim.ascape.stats;

import org.apache.commons.lang.mutable.MutableDouble;

/**
 * A stats row that contains a function and a denominator function.
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
	private final StatsFunction<T> denominatorFunction;

	/**
	 * Construct {@link StatsFunctionRow} with a denominator of 0.
	 * 
	 * @param function
	 *            value function
	 * @param denominatorFunction
	 *            used to obtain a denominator name
	 */
	public StatsFunctionRow(StatsFunction<T> function,
			StatsFunction<T> denominatorFunction) {
		this(function.getName(), function, denominatorFunction,
				new MutableDouble(0));
	}

	/**
	 * Construct {@link StatsFunctionRow}.
	 * 
	 * @param function
	 *            value function
	 * @param denominatorFunction
	 *            used to obtain a denominator name
	 * @param denominator
	 *            mutable double
	 */
	public StatsFunctionRow(StatsFunction<T> function,
			StatsFunction<T> denominatorFunction, MutableDouble denominator) {
		this(function.getName(), function, denominatorFunction, denominator);
	}

	/**
	 * Construct {@link StatsFunctionRow}.
	 * 
	 * @param name
	 *            name
	 * @param function
	 *            value function
	 * @param denominatorFunction
	 *            used to obtain a denominator name
	 * @param denominator
	 *            mutable double
	 */
	public StatsFunctionRow(String name, StatsFunction<T> function,
			StatsFunction<T> denominatorFunction, MutableDouble denominator) {
		this.name = name;
		this.function = function;
		this.denominatorFunction = denominatorFunction;
		this.denominator = denominator;
	}

	public String getName() {
		return name;
	}

	public StatsFunction<T> getFunction() {
		return function;
	}

	public String getDenominatorName() {
		return denominatorFunction.getName();
	}

	public MutableDouble getDenominator() {
		return denominator;
	}

	public void setDenominatorValue(double value) {
		this.denominator.setValue(value);
	}

}
