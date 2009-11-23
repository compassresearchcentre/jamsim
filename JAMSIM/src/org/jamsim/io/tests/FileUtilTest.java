package org.jamsim.io.tests;

import static org.junit.Assert.*;

import org.jamsim.io.FileUtil;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileUtilTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testAddTrailingSlash() {
		assertEquals("d:\\foo\\", FileUtil.addTrailingSlash("d:\\foo"));
		assertEquals("d:\\foo\\", FileUtil.addTrailingSlash("d:\\foo\\"));
	}

}
