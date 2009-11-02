package org.jamsim.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.data.model.CDataRowSet;
import net.casper.data.model.CRowMetaData;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * A 2D matrix of doubles with an index.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class IndexedDenseDoubleMatrix2D extends DenseDoubleMatrix2D {

	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = -1622843589477208375L;

	/**
	 * Index created from the primary keys of a casper dataset. Can be multiple
	 * columns of any type of object.
	 * 
	 */
	protected Object[][] index;

	/**
	 * List of the column names of the matrix.
	 */
	private final List<String> columnNames = new ArrayList<String>();

	public IndexedDenseDoubleMatrix2D(CDataCacheContainer container)
			throws CDataGridException {
		this(container.getAll());
	}

	/**
	 * Construct an instance from a CDataRowSet. Generates an index array of the
	 * primary key columns in the dataset, and stores these internally in the
	 * object instance. The matrix is then set up with all other columns, which
	 * must contain doubles.
	 * 
	 * @param cdrs
	 *            a casper CDataRowSet
	 * @throws CDataGridException
	 *             if problem reading dataset
	 * @throws IllegalArgumentException
	 *             if a non double value is encountered or some other problem
	 *             with the CDataRowSet
	 */
	public IndexedDenseDoubleMatrix2D(CDataRowSet cdrs)
			throws CDataGridException {
		// must call super constructor so setup empty 0x0 matrix
		super(0, 0);
		double[][] result;

		// get meta information from CDataRowSet
		CRowMetaData meta = cdrs.getMetaDefinition();

		// create list of the columns that are primary keys
		List<Integer> primaryKeys = new ArrayList<Integer>();
		for (int pkIndex : meta.getPrimaryKeyColumnIndices()) {
			primaryKeys.add(pkIndex);
		}
		int keys = primaryKeys.size();

		// create list of column names,
		// for those columns not primary keys
		int cols = meta.getColumnCount();
		String[] allColumnNames = meta.getColumnNames();
		for (int col = 0; col < cols; col++) {
			if (!primaryKeys.contains(col)) {
				columnNames.add(allColumnNames[col]);
			}
		}

		int resultCols = cols - keys;
		int rows = cdrs.getNumberRows();

		cdrs.reset(); // reset cursor to beginning

		// index to hold primary key values
		index = new Object[rows][keys];
		// matrix of non PK values
		result = new double[rows][resultCols];

		// fill primary key index and matrix double[][] array (result)
		int row = 0;
		int keyCol = 0;
		int resultCol = 0;
		while (cdrs.next()) {
			for (int col = 0; col < cols; col++) {
				if (primaryKeys.contains(col)) {
					index[row][keyCol++] = cdrs.getObject(col);
				} else {
					result[row][resultCol++] = cdrs.getDouble(col);
				}
			}
			keyCol = 0;
			resultCol = 0;
			row++;
		}

		/**
		 * Setup matrix internals with result:
		 * 
		 * setup matrix with number of rows and columns create empty matrix copy
		 * values from result[][] to matrix
		 */

		setUp(rows, resultCols);
		this.elements = new double[rows * resultCols];
		assign(result);
	}

	/**
	 * Display the index as a string.
	 * 
	 * @return a comma delimited string of all rows in the index
	 */
	public String indexToString() {
		StringBuffer buf = new StringBuffer();

		for (Object[] o : index) {
			buf.append(Arrays.toString(o));
			buf.append(System.getProperty("line.separator"));
		}

		return buf.toString();
	}

	/**
	 * Does a lookup in the index for the supplied key and returns the
	 * corresponding matrix row number. The caller requires knowledge of the
	 * dataset primary key types, eg: if the type of the primary key is Double[]
	 * then passing in an Object[] with Integer[] values will throw an
	 * exception.
	 * 
	 * @param key
	 *            a primary key value
	 * @return row number, or exception thrown if key is not found
	 */
	public int indexLookupRow(Object[] key) {
		for (int i = 0; i < rows; i++) {
			if (Arrays.equals(index[i], key)) {
				return i;
			}
		}
		throw new RuntimeException("Key: " + Arrays.toString(key)
				+ " does not exist in matrix");
	}

	/**
	 * Get ordered list of column names for this matrix.
	 * 
	 * @return ordered list of column names for this matrix.
	 */
	public String[] getColumnNames() {
		return columnNames.toArray(new String[columnNames.size()]);
	}
}