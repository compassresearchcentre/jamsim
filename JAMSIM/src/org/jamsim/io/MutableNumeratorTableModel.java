package org.jamsim.io;

import javax.swing.table.AbstractTableModel;

import org.jamsim.math.MutableNumerator;

/**
 * Table Model for an array of {@link MutableNumerator}s. Modifications to the
 * table model update the underlying {@link MutableNumerator}s.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class MutableNumeratorTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3155978328221657804L;

	private static final String[] COLUMN_NAMES = { "Name", "Value" };

	private final MutableNumerator[] values;

	/**
	 * Construct table model.
	 * 
	 * @param values
	 *            row values from parameter set
	 */
	public MutableNumeratorTableModel(MutableNumerator[] values) {
		this.values = values;
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
		return values.length;
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
			return values[rowIndex].getName();

		} else if (columnIndex == 1) {
			// values
			return values[rowIndex].doubleValue();

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

		values[row].setNumerator(dvalue);
		fireTableCellUpdated(row, col);

	}

}
