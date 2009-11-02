package org.jamsim.matrix;

import javax.swing.table.AbstractTableModel;

import cern.colt.matrix.DoubleMatrix2D;

public class MatrixTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1339665392513388667L;

	DoubleMatrix2D matrix;

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

	public DoubleMatrix2D getMatrix() {
		return matrix;
	}

	public void setMatrix(DoubleMatrix2D matrix) {
		this.matrix = matrix;
		fireTableStructureChanged();
	}

}
