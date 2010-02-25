package org.jamsim.matrix.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import org.jamsim.matrix.CBuildFromMatrix;
import org.jamsim.matrix.IndexedDenseDoubleMatrix2D;
import org.junit.BeforeClass;
import org.junit.Test;

public class CBuildFromMatrixTest {

	public static IndexedDenseDoubleMatrix2D matrix;
	public static CBuildFromMatrix builder;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String[] indexColumnNames = new String[] { "one", "two", "alpha" };
		Object[][] index =
				new Object[][] { new Object[] { 0, 5, "a" },
						new Object[] { "1", 6, "b" },
						new Object[] { 2, 7, "c" },
						new Object[] { 3, "8", "d" },
						new Object[] { 4, 9, "e" } };

		String[] matrixColumnNames = new String[] { "m1", "m2", "m3" };
		double[][] values =
				new double[][] { new double[] { 100.1, 100.2, 100.3 },
						new double[] { 200.1, 200.2, 200.3 },
						new double[] { 300.1, 300.2, 300.3 },
						new double[] { 400.1, 400.2, 400.3 },
						new double[] { 500.1, 500.2, 500.3 }, };

		matrix =
				new IndexedDenseDoubleMatrix2D(indexColumnNames, index,
						matrixColumnNames, values);

		builder = new CBuildFromMatrix("foo", matrix);
	}

	@Test
	public void testGetColumnTypes() throws IOException {
		Class<?>[] types =
				new Class<?>[] { Integer.class, Integer.class,
						Character.class, Double.class, Double.class,
						Double.class };

	
		assertArrayEquals(types, builder.getColumnTypes());
	
	}

	@Test
	public void testReadRow() throws IOException {

		Object[] row1 = new Object[] { 0, 5, 'a', 100.1, 100.2, 100.3 };
		Object[] row2 = new Object[] { 1, 6, 'b', 200.1, 200.2, 200.3 };
		Object[] row3 = new Object[] { 2, 7, 'c', 300.1, 300.2, 300.3 };
		Object[] row4 = new Object[] { 3, 8, 'd', 400.1, 400.2, 400.3 };
		Object[] row5 = new Object[] { 4, 9, 'e', 500.1, 500.2, 500.3 };

		Object[] readRow1 = builder.readRow();
		assertArrayEquals(row1, readRow1);

		assertArrayEquals(row2, builder.readRow());
		assertArrayEquals(row3, builder.readRow());
		assertArrayEquals(row4, builder.readRow());
		assertArrayEquals(row5, builder.readRow());
		assertArrayEquals(null, builder.readRow());

	}
}
