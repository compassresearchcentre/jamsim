package org.jamsim.math.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.ArrayUtils;
import org.jamsim.math.IntervalsIntMap;
import org.junit.BeforeClass;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class BinarySearchLowerTest {

	private static final double DELTA = 0;
	private static final double EPSILON = 1.0e-15;
	private static final double step15sx =
			0.055412371 + 0.030927835 + 0.052835052 + 0.006443299
					+ 0.06443299 + 0.105670103 + 0.119845361 + 0.190721649
					+ 0.056701031 + 0.059278351 + 0.003865979 + 0.086340206
					+ 0.073453608 + 0.00257732 + 0.091494845;
	private static final double step14sx = step15sx - 0.091494845;
	private static final double step13sx = step14sx - 0.00257732;
	private static final double step12sx = step13sx - 0.073453608;
	private static final double step11sx = step12sx - 0.086340206;
	private static final double step10sx = step11sx - 0.003865979;
	private static final double step9sx = step10sx - 0.059278351;
	private static final double step8sx = step9sx - 0.056701031;
	private static final double step7sx = step8sx - 0.190721649;
	private static final double step6sx = step7sx - 0.119845361;
	private static final double step5sx = step6sx - 0.105670103;
	private static final double step4sx = step5sx - 0.06443299;
	private static final double step3sx = step4sx - 0.006443299;
	private static final double step2sx = step3sx - 0.052835052;
	private static final double step1sx = step2sx - 0.030927835;
	@DataPoint
	public static final double[] stepssx =
			{ step1sx, step2sx, step3sx, step4sx, step5sx, step6sx, step7sx,
					step8sx, step9sx, step10sx, step11sx, step12sx, step13sx,
					step14sx, step15sx };

	private static final double step1 = 0.055412371;
	private static final double step2 = step1 + 0.030927835;
	private static final double step3 = step2 + 0.052835052;
	private static final double step4 = step3 + 0.006443299;
	private static final double step5 = step4 + 0.06443299;
	private static final double step6 = step5 + 0.105670103;
	private static final double step7 = step6 + 0.119845361;
	private static final double step8 = step7 + 0.190721649;
	private static final double step9 = step8 + 0.056701031;
	private static final double step10 = step9 + 0.059278351;
	private static final double step11 = step10 + 0.003865979;
	private static final double step12 = step11 + 0.086340206;
	private static final double step13 = step12 + 0.073453608;
	private static final double step14 = step13 + 0.00257732;
	private static final double step15 = step14 + 0.091494845;
	@DataPoint
	public static final double[] steps =
			{ step1, step2, step3, step4, step5, step6, step7, step8, step9,
					step10, step11, step12, step13, step14, step15 };

	@DataPoint
	private static final double[] probs =
			{ 0.055412371, 0.030927835, 0.052835052, 0.006443299, 0.06443299,
					0.105670103, 0.119845361, 0.190721649, 0.056701031,
					0.059278351, 0.003865979, 0.086340206, 0.073453608,
					0.00257732, 0.091494845 };

	private static final int[] destCats =
			{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, };

	@DataPoint
	public static final double[] stepsFromProbs =
			ArrayUtils.subarray(IntervalsIntMap.newCumulativeDistribution(
					probs, destCats).getIntervals(), 1, 100);

	public int getStep(double key) {
		return binarySearchLower(stepssx, key);
	}

	public int binarySearchLower(double[] array, double key) {
		return IntervalsIntMap.binarySearchLower(array, key, DELTA);
	}

	@BeforeClass
	public static void display() {
		System.out.println("stepssx       : " + ArrayUtils.toString(stepssx));
		System.out.println("steps         : " + ArrayUtils.toString(steps));
		System.out.println("stepsFromProbs: "
				+ ArrayUtils.toString(stepsFromProbs));
		System.out.println(ArrayUtils
				.toString(IntervalsIntMap.newCumulativeDistribution(probs,
						destCats).getIntervals()));
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerLessThanZero(double[] array) {
		assertEquals(-1, binarySearchLower(array, -EPSILON));
	}

	@Theory
	public void compareArrays() {
		assertArrayEquals(steps, stepssx, 1.0e-16);
		assertArrayEquals(stepssx, stepsFromProbs, 1.0e-16);

	}

	@Theory
	public void testGetCDCat15ReallocChildBearerStep1AndLess(double[] array) {
		assertEquals(-1, binarySearchLower(array, 0));
		assertEquals(-1, binarySearchLower(array, step1sx - EPSILON));
		assertEquals(-1, binarySearchLower(array, step1sx));
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerStep1(double[] array) {
		assertEquals(0, binarySearchLower(array, step1sx + EPSILON));
		assertEquals(0, binarySearchLower(array, step2sx));
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerStep2(double[] array) {
		assertEquals(1, binarySearchLower(array, step2sx + EPSILON));
		assertEquals(1, binarySearchLower(array, step3sx));
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerStep3(double[] array) {
		assertEquals(2, binarySearchLower(array, step3sx + EPSILON));
		assertEquals(2, binarySearchLower(array, step4sx));
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerAllSteps(double[] array) {
		for (int i = 0; i < stepssx.length - 1; i++) {
			assertEquals(i, binarySearchLower(array, stepssx[i] + EPSILON));
			assertEquals(i, binarySearchLower(array, stepssx[i + 1]));
		}
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerStep14(double[] array) {
		assertEquals(13, binarySearchLower(array, step14sx + EPSILON));
		assertEquals(13, binarySearchLower(array, step15sx));
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerStep15(double[] array) {
		assertEquals(14, binarySearchLower(array, step15sx + EPSILON));
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerUpperBound1(double[] array) {
		// random in range will generate a number between 0 (inclusive) and 1
		// (exclusive)
		assertEquals(13, binarySearchLower(array, 1));
	}

}
