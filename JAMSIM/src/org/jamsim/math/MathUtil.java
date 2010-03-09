package org.jamsim.math;

public final class MathUtil {

	private MathUtil() {
		// no instantiation
	}
	
	/**
	 * Probability = exp(logit)/(1+exp(logit)).
	 * 
	 * @param logitValue logit value
	 * @return probability
	 */
	public static double probFromLogit(double logitValue) {
		return Math.exp(logitValue) / (1 + Math.exp(logitValue));
	}
}
