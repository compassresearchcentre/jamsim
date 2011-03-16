package org.jamsim.shared;

import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPInteger;

/**
 * Share constants that might be used in multiple places in a simulation.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class Constants {

	private Constants() {
		// no instantiation
	}
	
	/**
	 * Integer missing value constant.
	 */
	public static final int MISSING_VALUE_INTEGER = REXPInteger.NA; // NOPMD

	/**
	 * Double missing value constant.
	 */
	public static final double MISSING_VALUE_DOUBLE = REXPDouble.NA; // NOPMD

	/**
	 * Newline.
	 */
	public static final String NEWLINE = System.getProperty("line.separator");


}
