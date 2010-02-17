package org.omancode.util.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;
import org.omancode.util.MRUSet;

public class MRUSetTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@Test
	public void testMRU() {
		MRUSet<String> mru = new MRUSet<String>(3);

		mru.add("A");
		mru.add("B");
		mru.add("C");

		String[] cba = new String[] { "C", "B", "A" };
		String[] dcb = new String[] { "D", "C", "B" };
		String[] cdb = new String[] { "C", "D", "B" };
		String[] abc = new String[] { "A", "B", "C" };

		System.out.println(mru.element());

		System.out.println(mru);

		assertArrayEquals(cba, mru.toArray(new String[3]));

		mru.add("D");

		System.out.println(mru.element());
		System.out.println(mru);
		assertArrayEquals(dcb, mru.toArray(new String[3]));

		mru.add("C");

		System.out.println(mru.element());
		System.out.println(mru);
		assertArrayEquals(cdb, mru.toArray(new String[3]));

		System.out.println("Addall A,B,C");
		mru.addAll(Arrays.asList(abc));
		System.out.println(mru);
		
		assertArrayEquals(cba, mru.toArray(new String[3]));

		Iterator<String> iterator = mru.iterator();

		assertEquals("C", iterator.next());
		assertEquals("B", iterator.next());
		assertEquals("A", iterator.next());

	}
	
}
