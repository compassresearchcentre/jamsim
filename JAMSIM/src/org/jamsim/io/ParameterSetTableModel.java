package org.jamsim.io;

import javax.swing.table.AbstractTableModel;

import org.jamsim.math.MutableNumerator;

/**
 * Table Model for a {@link ParameterSet} that allows editing of the
 * {@link ParameterSet}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ParameterSetTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3155978328221657804L;

	private static final String[] COLUMN_NAMES = { "Name", "Value" };

	private final String[] rowNames;
	private final MutableNumerator[] rowValues;

	/**
	 * Construct table model.
	 * 
	 * @param rowNames
	 *            row names from parameter set
	 * @param rowValues
	 *            row values from parameter set
	 */
	public ParameterSetTableModel(String[] rowNames,
			MutableNumerator[] rowValues) {
		this.rowNames = rowNames;
		this.rowValues = rowValues;
	}

	@Override
	public String getColumnName(int col) {
		return COLUMN_NAMES[col];
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return rowValues.length;
	}

	@Override
	public Class<?> getColumnClass(int c) {
		if (c == 0) {
			// names
			return String.class;

		} else if (c == 1) {
			// values
			return Double.class;

		} else {
			throw new IllegalStateException("column " + c + " does not exist");
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		// values column is editable
		return (col == 1);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			// names
			return rowNames[rowIndex];

		} else if (columnIndex == 1) {
			// values
			return rowValues[rowIndex];

		} else {
			throw new IllegalStateException("column " + columnIndex
					+ " does not exist");
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {

		if (col != 1) {
			throw new IllegalStateException("column " + col
					+ " is not editable");
		}

		double dvalue = ((Double) value).doubleValue();

		rowValues[row].setNumerator(dvalue);
		fireTableCellUpdated(row, col);

	}

}
