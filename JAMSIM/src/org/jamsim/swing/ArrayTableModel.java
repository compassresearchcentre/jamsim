package org.jamsim.swing;

import java.lang.reflect.Array;

import javax.swing.table.AbstractTableModel;

/**
 * Display an array of any type as a table with each values' index.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */

public class ArrayTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 2648816024638626001L;

	private final Object array;

	private static final String[] COLUMN_NAMES = { "Index", "Value" };

	private final Class<?> arrayClass;

	/**
	 * Construct an {@link ArrayTableModel} for an array of any type.
	 * 
	 * @param array
	 *            array to display as a table
	 */
	public ArrayTableModel(Object array) {
		this.array = array;
		arrayClass = Array.get(array, 0).getClass();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return Integer.class;
		} else {
			return arrayClass;
		}
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return COLUMN_NAMES[columnIndex];
	}

	@Override
	public int getRowCount() {
		return Array.getLength(array);
	}

	@Override
	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return Integer.valueOf(row);
		case 1:
			return Array.get(array, row);
		default:
			return null;
		}
	}
}
