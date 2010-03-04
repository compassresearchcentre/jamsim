package org.jamsim.math;

import cern.jet.random.Normal;
import cern.jet.random.Uniform;

/**
 * Random Number Generator (RNG) interface implementation using the CERN Colt
 * libraries.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ColtRNG implements RNG {

	@Override
	public double nextGaussian(double mean, double standardDeviation) {
		return Normal.staticNextDouble(mean, standardDeviation);
	}

	@Override
	public double nextUniform01() {
		return Uniform.staticNextDouble();
	}

}
