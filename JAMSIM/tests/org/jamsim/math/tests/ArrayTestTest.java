package org.jamsim.math.tests;

import static org.junit.Assert.assertEquals;

import org.jamsim.math.ArrayMath;
import org.junit.BeforeClass;
import org.junit.Test;

public class ArrayTestTest {

	boolean[] allFalse = {false, false, false};
	boolean[] allTrue = {true, true, true};
	boolean[] oneTrue = {false, false, true};
	boolean[] oneFalse = {true, true, false};
	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testAtLeastOne() {
		assertEquals(false, ArrayMath.atLeastOne(allFalse));
		assertEquals(true, ArrayMath.atLeastOne(allTrue));
		assertEquals(true, ArrayMath.atLeastOne(oneTrue));
		assertEquals(true, ArrayMath.atLeastOne(oneFalse));
	}

	@Test
	public void testAllFalse() {
		assertEquals(true, ArrayMath.allFalse(allFalse));
		assertEquals(false, ArrayMath.allFalse(allTrue));
		assertEquals(false, ArrayMath.allFalse(oneTrue));
		assertEquals(false, ArrayMath.allFalse(oneFalse));
	}

}
