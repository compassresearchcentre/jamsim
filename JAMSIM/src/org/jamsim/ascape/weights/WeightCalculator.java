package org.jamsim.ascape.weights;

import java.util.Map;
import java.util.Observer;

import org.jamsim.io.ParameterSet;

/**
 * An Observable {@link ParameterSet}s that calculates a weight when provided
 * with a variable map.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface WeightCalculator extends ParameterSet {

	/**
	 * Preference key used to store the last used weight calculator between
	 * runs.
	 */
	String WCALC_KEY = "weight calculator";

	/**
	 * Return the appropriate factor weight based on the values in {@code vars}.
	 * 
	 * @param vars
	 *            map of variable names and values
	 * @return weight
	 */
	double getWeight(Map<String, ?> vars);

	/**
	 * Return the base, or un-weighted, value of every agent. This may be 1, if
	 * 1 agent corresponds to 1 member of the whole population. Or it may be
	 * greater than 1 if the number of agents are being scaled up to match a
	 * particular population size.
	 * 
	 * @return weight equal
	 */
	double getWeightEqual();

	/**
	 * Adds an observer to the set of observers for this object, provided that
	 * it is not the same as some observer already in the set.
	 * 
	 * @param o
	 *            observer
	 */
	void addObserver(Observer o);

}
