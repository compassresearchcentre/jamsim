package org.jamsim.math;

/**
 * Random Number Generator (RNG) interface.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface RNG {

	/**
	 * Returns a uniformly distributed random number in the open interval (0,1)
	 * (excluding 0 and 1).
	 * 
	 * @return uniformly distributed pseudo random number
	 */
	double nextUniform01();

	/**
	 * Returns a random number from the Gaussian (normal) distribution with the
	 * given mean and standard deviation.
	 * 
	 * @param mean
	 *            mean
	 * @param standardDeviation
	 *            standard deviation
	 * @return normally distributed pseudo random number
	 */
	double nextGaussian(double mean, double standardDeviation);

}
