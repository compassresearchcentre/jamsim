package org.jamsim.util.tests;

import static org.junit.Assert.*;

import org.jamsim.util.DateUtil;
import org.junit.BeforeClass;
import org.junit.Test;

public class DateUtilTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testNowToSortableUniqueDateString() {
		System.out.println(DateUtil.nowToSortableUniqueDateString());
	}

}
