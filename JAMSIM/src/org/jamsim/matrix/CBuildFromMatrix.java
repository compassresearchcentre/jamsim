package org.jamsim.matrix;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import net.casper.data.model.CBuilder;
import net.casper.ext.narrow.NarrowException;
import net.casper.ext.narrow.NarrowUtil;
import net.casper.io.file.util.ArrayUtil;

import org.apache.commons.lang.ArrayUtils;

/**
 * Create a casper dataset from a matrix.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class CBuildFromMatrix implements CBuilder {

	private final String name;
	private Object[][] index;
	private final double[][] matrix;
	private final int numColumns;
	private int currentRow;

	/**
	 * List of the column names of the index.
	 */
	private final String[] indexColumnNames;

	/**
	 * List of the column names of the index.
	 */
	private final String[] matrixColumnNames;

	/**
	 * Construct from a matrix.
	 * 
	 * @param name
	 *            name
	 * @param imatrix
	 *            indexed matrix
	 */
	public CBuildFromMatrix(String name, IndexedDenseDoubleMatrix2D imatrix) {
		this.name = name;
		this.index = imatrix.getIndex();
		this.indexColumnNames = imatrix.getIndexColumnNames();
		this.matrixColumnNames = imatrix.getMatrixColumnNames();
		this.matrix = imatrix.toArray();

		if (index.length != matrix.length) {
			throw new IllegalArgumentException("index.length ("
					+ index.length + ") != matrix.length (" + matrix.length
					+ ")");
		}

		numColumns = matrixColumnNames.length + indexColumnNames.length;

	}

	@Override
	public void close() {
		// nothing
	}

	@Override
	public String[] getColumnNames() {
		return (String[]) ArrayUtils.addAll(indexColumnNames,
				matrixColumnNames);
	}

	@Override
	public Class[] getColumnTypes() {

		Object[][] indexT = ArrayUtil.transpose(index);
		NarrowUtil nutil = new NarrowUtil();
		Class<?>[] columnTypes = new Class<?>[numColumns];
		int col;

		// calculate narrowest index column types
		// narrow transposed index
		for (col = 0; col < indexT.length; col++) {
			columnTypes[col] = nutil.calcNarrowestType(indexT[col], false);
			try {
				indexT[col] =
						nutil.narrowArray(indexT[col], columnTypes[col],
								false);
			} catch (NarrowException e) {
				throw new IllegalStateException(e);
			}

		}

		// replace index with narrowed index
		index = ArrayUtil.transpose(indexT);

		// remaining columns are doubles from the matrix
		for (; col < numColumns; col++) {
			columnTypes[col] = Double.class;
		}
		return columnTypes;
	}

	@Override
	public Map getConcreteMap() {
		return new LinkedHashMap();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String[] getPrimaryKeyColumns() {
		//return null;
		return indexColumnNames;
	}

	@Override
	public void open() throws IOException {
		currentRow = 0;
	}

	@Override
	public Object[] readRow() throws IOException {
		if (currentRow == index.length) {
			return null;

		}

		return ArrayUtils.addAll(index[currentRow], ArrayUtils
				.toObject(matrix[currentRow++]));

	}

}
