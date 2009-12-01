package org.jamsim.ascape.stats;

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

	
	public CollectorFunctionPerIteration(String name,
			StatsFunction<T> valueFunction, double denominator, Scape scape) {
		this(name, valueFunction, null, denominator, scape);
	}
	
	public CollectorFunctionPerIteration(String name,
			StatsFunction<T> valueFunction,
			StatsPredicate<T> predicate, double denominator, Scape scape) {
		super(name, valueFunction, predicate, denominator);
		this.scape = scape;
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
