package org.omancode.util.tests;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.omancode.util.DateUtil;

public class DateUtilTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testNowToSortableUniqueDateString() {
		System.out.println(DateUtil.nowToSortableUniqueDateString());
	}

}
