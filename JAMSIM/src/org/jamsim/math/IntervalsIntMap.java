package org.jamsim.math;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Arrays;

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
	 * @return intervals, from 0 (exclusive) - 1 (inclusive), mapped to {@code
	 *         value}. Or in other words, a cumulative distribution of
	 *         probabilities mapped to an integer value.
	 */
	public static IntervalsIntMap newInstanceFromProbabilities(
			double[] probabilities, int[] values) {
		double[] rightBounds = new double[probabilities.length];
		double sumProbs = sumArray(probabilities);

		// build cumulative distribution up from the bottom
		rightBounds[0] = probabilities[0] / sumProbs;
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

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer(256);

		sbuf.append("Intervals: ").append(Arrays.toString(getIntervals()))
				.append('\n');
		sbuf.append("Values: ").append(Arrays.toString(getValues())).append(
				'\n');

		return sbuf.toString();

	}

	/**
	 * Draw from a cumulative distribution of probabilities. First, create a
	 * cumulative distribution from the set of probabilities that are included
	 * (i.e.: {@code includeProb[i] = true}). Second, using {@code random} draw
	 * a probability from the CD and return its index.
	 * 
	 * @param includeProb
	 *            the set of all probabilities to include. Only where {@code
	 *            includeProb[i] = true} will that probability be used to create
	 *            the CD.
	 * @param probabilities
	 *            the probability of all conditions. Only the probabilities of
	 *            conditions that are {@code true} in {@code includeProb} will
	 *            be used to create the cumulative distribution.
	 * @param random
	 *            a random number in the range {@code (0,1]}.
	 * @return an index in the range {@code [0, includeProb.length]}. This is
	 *         the position of the probability in the {@code includeProb} array.
	 */
	public static int drawIndexFromProbs(boolean[] includeProb,
			double[] probabilities, double random) {

		IntArrayList indexOfProbsIncluded =
				new IntArrayList(includeProb.length);
		DoubleArrayList includedProbsList =
				new DoubleArrayList(includeProb.length);

		// create lists of:
		// 1) the index of included probabilities
		// 2) the included probability values
		for (int i = 0; i < includeProb.length; i++) {
			if (includeProb[i]) {
				indexOfProbsIncluded.add(i);
				includedProbsList.add(probabilities[i]);
			}
		}

		if (indexOfProbsIncluded.size() == 0) {
			// no included probabilities
			return -1;
		}

		// create cumulative distribution
		IntervalsIntMap cdmap =
				IntervalsIntMap.newInstanceFromProbabilities(
						includedProbsList.toDoubleArray(),
						indexOfProbsIncluded.toIntArray());

		// select an index from the cumulative distribution
		return cdmap.getMappedValue(random);
	}

}
