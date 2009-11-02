package org.jamsim.math;

/**
 * Maps a set of contiguous intervals on the real number line to a set of
 * integer values.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class IntervalsIntMap {

	private static final double COMPARE_DELTA = 0;
	private final double leftBound;
	private final double[] rightBounds;
	private final double rightMostBound;
	private final int[] values;

	/**
	 * Construct a contiguous set of intervals. The bottom of the range is
	 * specified by {@code leftBound}. Each interval is specified by the
	 * previous bound and its right bound, specified in {@code rightBounds}. eg:
	 * {@code (a,b], (b,c], (c,d]}, where {@code a = leftBound} and {@code b, c,
	 * d} equal {@code rightBounds[0], rightBounds[1], rightBounds[2]}
	 * respectively.
	 * 
	 * @param leftBound
	 *            the bottom of the interval range.
	 * @param rightBounds
	 *            array, sorted from lowest to highest, of successive right
	 *            bounds of each interval
	 * @param values
	 *            the set of values mapped to by each interval
	 */
	public IntervalsIntMap(double leftBound, double[] rightBounds,
			int[] values) {
		this.leftBound = leftBound;

		if (rightBounds.length != values.length) {
			throw new IllegalArgumentException("rightBounds.length ("
					+ rightBounds.length + ") != values.length ("
					+ values.length + ")");
		}
		this.rightBounds = rightBounds;
		this.rightMostBound = rightBounds[rightBounds.length - 1];
		this.values = values;
	}

	/**
	 * Return all intervals, including the lowest bound (ie: the left bound).
	 * 
	 * @return array of the left bound and all right bounds.
	 */
	public double[] getIntervals() {
		double[] intervals = new double[rightBounds.length + 1];
		intervals[0] = leftBound;
		System.arraycopy(rightBounds, 0, intervals, 1, rightBounds.length);
		return intervals;
	}

	/**
	 * Return the right bounds; exclude the lowest bound (ie: the left bound).
	 * 
	 * @return array of all right bounds.
	 */
	public double[] getRightBounds() {
		return rightBounds.clone();
	}

	/**
	 * Get the values that the intervals map to.
	 * 
	 * @return values array
	 */
	public int[] getValues() {
		return values.clone();
	}

	/**
	 * Creates a new instance, generating the right bounds from {@code
	 * probabilities}.
	 * 
	 * @param probabilities
	 *            probabilities
	 * @param values
	 *            values that map to {@code probabilities}
	 * @return intervals, from 0 - 1 (inclusive), mapped to {@code value}. Or in
	 *         other words, a cumulative distribution of probabilities mapped to
	 *         an integer value.
	 */
	public static IntervalsIntMap newInstanceFromProbabilities(
			double[] probabilities, int[] values) {
		double[] rightBounds = new double[probabilities.length];
		double sumProbs = sumArray(probabilities);

		// build cumulative distribution up from the bottom
		rightBounds[0] = probabilities[0];
		for (int i = 0; i < probabilities.length - 1; i++) {
			rightBounds[i + 1] =
					rightBounds[i] + probabilities[i + 1] / sumProbs;
		}

		return new IntervalsIntMap(0, rightBounds, values);
	}

	/**
	 * Return the mapped value that is less than or equal to {@code x} but not
	 * less than then next lowest right bound. In other words, return the mapped
	 * value for the interval (a,b] ie: {x E R | a < x <= b}.
	 * 
	 * @param x
	 *            right bound of this probability band
	 * @return mapped value
	 */
	public int getMappedValue(double x) {

		// if x <= leftBound
		if (x - leftBound <= COMPARE_DELTA) {
			throw new IllegalArgumentException("x (" + x + ") <= leftBound ("
					+ leftBound + ").");
		}

		// if x <= rightBounds[0]
		if (x - rightBounds[0] <= COMPARE_DELTA) {
			return values[0];
		}

		// if x > rightMostBound
		if (x - rightMostBound > COMPARE_DELTA) {
			throw new IllegalArgumentException("x (" + x
					+ ") > rightMostBound (" + rightMostBound + ").");
		}

		int indexBeforeRightBound =
				binarySearchLower(rightBounds, x, COMPARE_DELTA);

		if (indexBeforeRightBound == -1) {
			throw new IllegalStateException("indexBeforeRightBound == -1");
		}

		return values[indexBeforeRightBound + 1];
	}

	/**
	 * Returns the index of the greatest array value strictly less than the
	 * given key.
	 * 
	 * @param array
	 *            an array sorted from lowest to highest
	 * @param key
	 *            key
	 * @param delta
	 *            the distance between the key and the array value that is
	 *            allowed for the 2 values to be considered equal.
	 * @return the index of the greatest key strictly less than the given key,
	 *         or {@code -1} if no array values are less than {@code key}.
	 */
	public static int binarySearchLower(double[] array, double key,
			double delta) {

		int low = 0;
		int high = array.length - 1;

		while (low <= high) {
			int mid = low + high >>> 1;
			double midvalue = array[mid];
			double cmp = midvalue - key;

			if (cmp < -delta) {
				low = mid + 1;
			} else if (cmp > delta) {
				high = mid - 1;
			} else {
				// key found, return previous key
				return mid - 1;
			}
		}

		// key not found, return the index
		// before the highest value < key
		return low - 1;

	}

	/**
	 * Return the sum of all the values in {@code array}.
	 * 
	 * @param array
	 *            double values to sum
	 * @return sum of double values in {@code array}.
	 */
	public static double sumArray(double[] array) {
		double sum = 0;

		for (int i = 0; i < array.length; i++) {
			sum = sum + array[i];
		}

		return sum;
	}

}
