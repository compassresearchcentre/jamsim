package org.jamsim.math.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import net.casper.data.model.CDataGridException;
import net.casper.ext.cellreader.CellReader;
import net.casper.ext.cellreader.CellReaders;
import net.casper.io.file.CDataFileDef;

import org.apache.commons.lang.ArrayUtils;
import org.jamsim.casper.CDataFileIntervalsMap;
import org.jamsim.io.FileLoader;
import org.jamsim.math.IntervalsIntMap;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class IntervalsIntMapTest {

	/**
	 * Dataset definition for cumulative distribution for the reallocation of
	 * category 15 for child bearers.
	 * <p>
	 * Variables:
	 * <dl>
	 * <dt>DestCat
	 * <dd>Destination category to assign.
	 * <dt>Prob
	 * <dd>Probability of this category being the destination category.
	 * </dl>
	 */
	public static final CDataFileDef Cat15ReallocChildBearer =
			new CDataFileDef("CDF Cat 15 Reallocation for Child Bearers",
					"DestCat,Prob", new CellReader<?>[] {
							CellReaders.INTEGER, CellReaders.DOUBLE },
					"DestCat");

	public static final CDataFileIntervalsMap CDCat15ReallocChildBearer =
			new CDataFileIntervalsMap(Cat15ReallocChildBearer, "Prob",
					"DestCat");

	public static FileLoader dsfLoader;

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
	private static final double[] steps =
			{ step1sx, step2sx, step3sx, step4sx, step5sx, step6sx, step7sx,
					step8sx, step9sx, step10sx, step11sx, step12sx, step13sx,
					step14sx, step15sx };
	private static final int[] destCats =
			{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16 };

	private static final double[] probs =
			{ 0.055412371, 0.030927835, 0.052835052, 0.006443299, 0.06443299,
					0.105670103, 0.119845361, 0.190721649, 0.056701031,
					0.059278351, 0.003865979, 0.086340206, 0.073453608,
					0.00257732, 0.091494845 };

	@DataPoint
	public static final IntervalsIntMap cdmapSteps =
			new IntervalsIntMap(0, steps, destCats);
	@DataPoint
	public static final IntervalsIntMap cdmapFromProbs =
			IntervalsIntMap.newInstanceFromProbabilities(probs, destCats);
	@DataPoint
	public static IntervalsIntMap cdmapFromFile;

	public IntervalsIntMapTest() throws IOException {

	}

	@BeforeClass
	public static void display() throws IOException, CDataGridException {
		dsfLoader = new FileLoader(IntervalsIntMapTest.class);
		dsfLoader.loadDataset(CDCat15ReallocChildBearer);
		cdmapFromFile = CDCat15ReallocChildBearer.getIntervalsMap();
	}

	@Test
	public void testnewInstanceFromProbabilities() {
		System.out.println("cdmapSteps    "
				+ ArrayUtils.toString(cdmapSteps.getIntervals()));
		assertArrayEquals(steps, cdmapSteps.getRightBounds(), EPSILON);
		assertArrayEquals(destCats, cdmapSteps.getValues());

		System.out.println("cdmapFromProbs"
				+ ArrayUtils.toString(cdmapFromProbs.getIntervals()));
		assertArrayEquals(steps, cdmapFromProbs.getRightBounds(), EPSILON);
		assertArrayEquals(destCats, cdmapFromProbs.getValues());
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerLessThanZero(
			IntervalsIntMap cdmap) {
		try {
			cdmap.getMappedValue(-EPSILON);
			fail("Exception not generated");
		} catch (IllegalArgumentException e) {

		}
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerAtLowerBoundZero(
			IntervalsIntMap cdmap) {
		try {
			cdmap.getMappedValue(0);
			fail("Exception not generated");
		} catch (IllegalArgumentException e) {

		}

	}

	@Theory
	public void testGetCDCat15ReallocChildBearerDestCatLevel1(
			IntervalsIntMap cdmap) {
		assertEquals(1, cdmap.getMappedValue(step1sx - EPSILON));
		assertEquals(1, cdmap.getMappedValue(step1sx));
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerDestCatLevel2(
			IntervalsIntMap cdmap) {
		assertEquals(2, cdmap.getMappedValue(step1sx + EPSILON));
		assertEquals(2, cdmap.getMappedValue(step2sx));
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerDestCatAllLevels(
			IntervalsIntMap cdmap) {
		for (int i = 1; i < steps.length; i++) {
			assertEquals(destCats[i], cdmap.getMappedValue(steps[i - 1]
					+ EPSILON));
			assertEquals(destCats[i], cdmap.getMappedValue(steps[i]));
		}
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerDestCatLevel14(
			IntervalsIntMap cdmap) {
		assertEquals(14, cdmap.getMappedValue(step13sx + EPSILON));
		assertEquals(14, cdmap.getMappedValue(step14sx));
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerDestCatLevel15(
			IntervalsIntMap cdmap) {
		assertEquals(16, cdmap.getMappedValue(step14sx + EPSILON));
		assertEquals(16, cdmap.getMappedValue(step15sx));
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerAtUpperBoundOne(
			IntervalsIntMap cdmap) {
		// random in range will generate a number between 0 (inclusive) and 1
		// (exclusive)
		assertEquals(16, cdmap.getMappedValue(1));
	}

	@Theory
	public void testGetCDCat15ReallocChildBearerGreaterThanUpperBound(
			IntervalsIntMap cdmap) {
		try {
			cdmap.getMappedValue(1 + EPSILON);
			fail("Exception not generated");
		} catch (IllegalArgumentException e) {

		}
	}

}
