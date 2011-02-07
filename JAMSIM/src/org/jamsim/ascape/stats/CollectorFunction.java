package org.jamsim.ascape.stats;

import org.apache.commons.lang.mutable.MutableDouble;
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
	protected final StatsPredicate<T> predicate;

	/**
	 * Value to collect from the scape members.
	 */
	protected final StatsFunction<T> valueFunction;

	private final MutableDouble denominator;

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
			StatsFunction<T> valueFunction, MutableDouble denominator) {
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
			StatsFunction<T> valueFunction,
			StatsPredicate<T> predicate, MutableDouble denominator) {
		super(name);
		this.predicate = predicate;
		this.valueFunction = valueFunction;
		this.denominator = denominator;
	}

	@Override
	public double getValue(Object object) {
		@SuppressWarnings("unchecked")
		T scapeMember = (T) object;
		return valueFunction.getValue(scapeMember);
	}

	@Override
	public boolean meetsCondition(Object object) {
		@SuppressWarnings("unchecked")
		T scapeMember = (T) object;
		if (predicate == null) {
			return true;
		}
		return predicate.test(scapeMember);
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
		return (getSum() / denominator.doubleValue());
	}

}