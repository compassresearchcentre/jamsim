package org.jamsim.ascape.stats;

import net.sf.functionalj.Function1;

import org.ascape.util.data.StatCollectorCondCSA;

/**
 * Collect data values on a subset of the scape. The subset to collect is
 * defined by a function predicate. The value to collect is defined by a value
 * function.
 * 
 * @param <T>
 *            type of the scape member this collector gets data from.
 * @author Oliver Mannion
 * @version $Revision$
 */
public class CollectorFunction<T> extends StatCollectorCondCSA {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1466048165913497853L;

	/**
	 * Predicate to determine which scape members to collect data for. If
	 * {@code null}, collects data for all members.
	 * 
	 */
	protected final Function1<Boolean, T> predicate;

	/**
	 * Value to collect from the scape members.
	 */
	protected final Function1<Double, T> valueFunction;

	private final double denominator;

	/**
	 * Construct a {@link CollectorFunction} that returns values for all scape
	 * members. i.e: has no predicate.
	 * 
	 * @param name
	 *            name of the value being collected
	 * @param valueFunction
	 *            value to collect from the scape members
	 * @param denominator
	 *            value to divide the sum by in {@link #getRatio()}.
	 */
	public CollectorFunction(String name,
			Function1<Double, T> valueFunction, double denominator) {
		this(name, valueFunction, null, denominator);
	}

	/**
	 * Construct a {@link CollectorFunction}.
	 * 
	 * @param name
	 *            name of the value being collected
	 * @param valueFunction
	 *            value to collect from the scape members
	 * @param predicate
	 *            predicate to determine which scape members to collect data
	 *            for. If {@code null}, collects data for all members.
	 * @param denominator
	 *            value to divide the sum by in {@link #getRatio()}.
	 */
	public CollectorFunction(String name,
			Function1<Double, T> valueFunction,
			Function1<Boolean, T> predicate, double denominator) {
		super(name);
		this.predicate = predicate;
		this.valueFunction = valueFunction;
		this.denominator = denominator;
	}

	@Override
	public double getValue(Object object) {
		T scapeMember = (T) object;
		double value = valueFunction.call(scapeMember);
		return value;
	}

	@Override
	public boolean meetsCondition(Object object) {
		T scapeMember = (T) object;
		if (predicate == null) {
			return true;
		}
		return predicate.call(scapeMember);
	}

	/**
	 * {@link #getRatio()} * 100.
	 * 
	 * @return percent
	 */
	public double getPercent() {
		return getRatio() * 100;
	}

	/**
	 * {@link #getSum()} / denominator specified during construction.
	 * 
	 * @return ratio ratio
	 */
	public double getRatio() {
		return (getSum() / denominator);
	}

}