package org.jamsim.math;

import javax.swing.table.AbstractTableModel;

public class IntervalsIntMapTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -3290472926883702484L;
	private static final String[] COLUMN_NAMES = { "Interval", "Value" };
	private static final Class<?>[] COLUMN_TYPES =
			{ Double.class, Integer.class };

	private final double[] intervals;
	private final int[] values;

	public IntervalsIntMapTableModel(IntervalsIntMap iimap) {
		intervals = iimap.getIntervals();
		values = iimap.getValues();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
		return intervals.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return intervals[rowIndex];
		} else {
			if (rowIndex == 0) {
				return null;
			} else {
				return values[rowIndex - 1];
			}
		}
	}

	@Override
	public String getColumnName(int columnIndex) {
		return COLUMN_NAMES[columnIndex];
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return COLUMN_TYPES[columnIndex];
	}

}
