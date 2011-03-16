package org.jamsim.data;

import org.jamsim.shared.Constants;

/**
 * Data operations.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class DataUtil {

	private DataUtil() {
		// no instantiation
	}

	/**
	 * Create a array of missing integer values (ie:
	 * {@link Constants#MISSING_VALUE_INTEGER}).
	 * 
	 * @param size
	 *            size of array
	 * @return array of missing integer values
	 */
	public static int[] missingIntArray(int size) {
		int[] array = new int[size];
	
		for (int i = 0; i < size; i++) {
			array[i] = Constants.MISSING_VALUE_INTEGER;
		}
	
		return array;
	}
	
	
}
