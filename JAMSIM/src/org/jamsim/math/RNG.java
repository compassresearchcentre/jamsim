package org.jamsim.math;

/**
 * Random Number Generator (RNG).
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface RNG {

	/**
	 * Returns the next pseudorandom, uniformly distributed double value between
	 * 0.0 and 1.0 from this random number generator's sequence.
	 * 
	 * @return pseudo random number in range 0.0d (inclusive) to 1.0d
	 *         (exclusive)
	 */
	double next();
}
