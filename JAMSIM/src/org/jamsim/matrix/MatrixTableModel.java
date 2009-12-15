package org.jamsim.matrix;

import javax.swing.table.AbstractTableModel;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * Table Model for a {@link DoubleMatrix2D}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class MatrixTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1339665392513388667L;

	private final DoubleMatrix2D matrix;

	/**
	 * Default constructor.
	 * 
	 * @param matrix matrix
	 */
	public MatrixTableModel(DoubleMatrix2D matrix) {
		super();
		this.matrix = matrix;
	}

	@Override
	public int getColumnCount() {
		return matrix.columns();
	}

	@Override
	public int getRowCount() {
		return matrix.rows();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return matrix.getQuick(rowIndex, columnIndex);
	}

	@Override
	public String getColumnName(int col) {
		return "col" + col;
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell.
	 */
	@Override
	public Class<Double> getColumnClass(int c) {
		return Double.class;
	}
}
