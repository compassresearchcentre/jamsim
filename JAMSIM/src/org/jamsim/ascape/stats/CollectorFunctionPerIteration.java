package org.jamsim.ascape.stats;

import org.apache.commons.lang.mutable.MutableDouble;
import org.ascape.model.Scape;

/**
 * Collect data values per iteration. i.e: the value collected is divided by the
 * current number of iterations.
 * 
 * @param <T>
 *            type of the scape member this collector gets data from.
 * @author Oliver Mannion
 * @version $Revision$
 */
public class CollectorFunctionPerIteration<T> extends CollectorFunction<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3623329047212583489L;
	private final Scape scape;

	/**
	 * Construct a {@link CollectorFunctionPerIteration} with no predicate. ie:
	 * will collect data for all members.
	 * 
	 * @param name
	 *            name of the value being collected
	 * @param valueFunction
	 *            value to collect from the scape members
	 * @param denominator
	 *            value to divide the sum by in {@link #getRatio()}.
	 * @param iteratingScape
	 *            used to determine the current iteration
	 */
	public CollectorFunctionPerIteration(String name,
			StatsFunction<T> valueFunction, MutableDouble denominator,
			Scape iteratingScape) {
		this(name, valueFunction, null, denominator, iteratingScape);
	}

	/**
	 * Master constructor for a {@link CollectorFunctionPerIteration}.
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
	 * @param iteratingScape
	 *            used to determine the current iteration
	 */
	public CollectorFunctionPerIteration(String name,
			StatsFunction<T> valueFunction, StatsPredicate<T> predicate,
			MutableDouble denominator, Scape iteratingScape) {
		super(name, valueFunction, predicate, denominator);
		this.scape = iteratingScape;
	}

	@Override
	public double getValue(Object object) {
		T scapeMember = (T) object;
		int currentIteration = scape.getIteration();
		double value = valueFunction.getValue(scapeMember);
		if (value == 0) {
			return 0;
		}
		return value / (double) currentIteration;
	}
}
