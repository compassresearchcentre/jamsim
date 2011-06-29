package org.jamsim.math;

import org.apache.commons.lang.ArrayUtils;

/**
 * Logic tests and arithmetic operations on arrays.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class ArrayMath {

	private ArrayMath() {
		// no instantiation
	}

	/**
	 * Check to see if there is at least 1 {@code true} value in the array.
	 * 
	 * @param array
	 *            boolean array
	 * @return {@code true} if a least one {@code true} value in the array.
	 */
	public static boolean atLeastOne(boolean[] array) {
		return ArrayUtils.contains(array, true);
	}

	/**
	 * Check to see if all values in the array are {@code false}.
	 * 
	 * @param array
	 *            boolean array
	 * @return {@code true} if all values in the array are {@code false}.
	 */
	public static boolean allFalse(boolean[] array) {
		return !atLeastOne(array);
	}

	/**
	 * Adds the double values in two arrays together and returns the sum.
	 * 
	 * @param array1
	 *            array 1
	 * @param array2
	 *            array 2
	 * @return the sum of array1 + array2
	 */
	public static double[] sum(double[] array1, double[] array2) {
		if (array1.length != array2.length) {
			throw new IllegalArgumentException("array1.length ("
					+ array1.length + ") != array2.length (" + array2.length
					+ ")");
		}

		double[] sum = new double[array1.length];

		for (int i = 0; i < array1.length; i++) {
			sum[i] = array1[i] + array2[i];
		}

		return sum;
	}

	/**
	 * Adds the double values in two arrays together and returns the sum.
	 * 
	 * @param array1
	 *            array 1
	 * @param array2
	 *            array 2
	 * @return the sum of array1 + array2
	 */
	public static int[] sum(int[] array1, int[] array2) {
		if (array1.length != array2.length) {
			throw new IllegalArgumentException("array1.length ("
					+ array1.length + ") != array2.length (" + array2.length
					+ ")");
		}

		int[] sum = new int[array1.length];

		for (int i = 0; i < array1.length; i++) {
			sum[i] = array1[i] + array2[i];
		}

		return sum;
	}

	/**
	 * Multiply each element of the array by scale.
	 * 
	 * @param array
	 *            doubles to multiply
	 * @param factor
	 *            scalar to multiply by.
	 * @return product
	 */
	public static double[] multiply(double[] array, double factor) {
		double[] product = new double[array.length];

		for (int i = 0; i < array.length; i++) {
			product[i] = array[i] * factor;
		}

		return product;
	}

}
