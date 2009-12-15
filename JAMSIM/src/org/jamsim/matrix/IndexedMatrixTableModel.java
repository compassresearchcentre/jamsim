package org.jamsim.matrix;

import javax.swing.table.AbstractTableModel;

/**
 * A TableModel wrapper for a {@link IndexedDenseDoubleMatrix2D}. The matrix
 * index coluns are shown before the matrix itself.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class IndexedMatrixTableModel extends AbstractTableModel {
	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = -4901947312398943011L;

	private final IndexedDenseDoubleMatrix2D matrix;
	private final Object[][] index;
	private final String[] columnNames;
	private final Class<?>[] columnTypes;
	private final int numIndexCols;

	/**
	 * Construct a table model for a {@link IndexedDenseDoubleMatrix2D}.
	 * 
	 * @param matrix
	 *            indexed matrix
	 */
	public IndexedMatrixTableModel(IndexedDenseDoubleMatrix2D matrix) {
		this.matrix = matrix;
		this.index = matrix.getIndex();
		this.columnNames = matrix.getAllColumnNames();

		this.columnTypes =
				constructColumnTypesArray(index[0], matrix.columns());

		this.numIndexCols = index[0].length;
	}

	private Class<?>[] constructColumnTypesArray(Object[] indexRow,
			int matrixCols) {
		Class<?>[] colTypes = new Class<?>[indexRow.length + matrixCols];

		for (int i = 0; i < indexRow.length; i++) {
			colTypes[i] = indexRow[i].getClass();
		}

		for (int i = indexRow.length; i < indexRow.length + matrixCols; i++) {
			colTypes[i] = Double.class;
		}

		return colTypes;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return matrix.rows();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		if (columnIndex < numIndexCols) {
			return index[rowIndex][columnIndex];
		} else {
			return matrix.get(rowIndex, columnIndex - numIndexCols);
		}
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnTypes[columnIndex];
	}
}