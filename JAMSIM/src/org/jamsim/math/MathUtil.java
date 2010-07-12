package org.jamsim.math;

public final class MathUtil {

	/**
	 * Default epsilon. The allowed difference between two doubles for them to
	 * be considered equal.
	 */
	public static final double EPSILON = .000000000000001;

	private MathUtil() {
		// no instantiation
	}

	/**
	 * Probability = exp(logit)/(1+exp(logit)).
	 * 
	 * @param logitValue
	 *            logit value
	 * @return probability
	 */
	public static double probFromLogit(double logitValue) {
		return Math.exp(logitValue) / (1 + Math.exp(logitValue));
	}

	/**
	 * Compare two doubles are within {@link #EPSILON}.
	 * 
	 * @param d1
	 *            double one
	 * @param d2
	 *            double two
	 * @return {@code true} if {@code d1} and {@code d2} are within
	 *         {@link #EPSILON} of each other.
	 */
	public static boolean equals(double d1, double d2) {
		return equals(d1, d2, EPSILON);
	}

	/**
	 * Compare two doubles are within epsilon.
	 * 
	 * @param d1
	 *            double one
	 * @param d2
	 *            double two
	 * @param epsilon
	 *            epsilon
	 * @return {@code true} if {@code d1} and {@code d2} are within {@code
	 *         epsilon} of each other.
	 */
	public static boolean equals(double d1, double d2, double epsilon) {
		return Math.abs(d1 - d2) < epsilon;
	}
}
