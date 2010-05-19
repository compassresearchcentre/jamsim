package org.jamsim.math;

/**
 * Random Number Generator (RNG) interface implementation that always returns
 * the same number.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class NonRNG implements RNG {

	private final double number;
	
	/**
	 * Default double to return.
	 */
	public static final double DEFAULT_DOUBLE = 0.1;
	
	/**
	 * Construct {@link NonRNG} that always returns {@link #DEFAULT_DOUBLE}.
	 */
	public NonRNG() {
		this(DEFAULT_DOUBLE);
	}

	/**
	 * Construct {@link NonRNG} that always returns {@code number}.
	 * 
	 * @param number
	 *            number to always return
	 */
	public NonRNG(double number) {
		this.number = number;
	}

	@Override
	public double nextGaussian(double mean, double standardDeviation) {
		return number;
	}

	@Override
	public double nextUniform01() {
		return number;
	}

}
