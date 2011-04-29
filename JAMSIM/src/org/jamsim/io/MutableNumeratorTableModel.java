package org.jamsim.io;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.jamsim.math.MutableNumerator;

/**
 * Table Model for an array of {@link MutableNumerator}s. Modifications to the
 * table model update the numerator of the underlying {@link MutableNumerator}s.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class MutableNumeratorTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3155978328221657804L;

	private static final String[] COLUMN_NAMES = { "Level", "Base (%)",
			"Weighting (%)" };

	private final MutableNumerator[] values;

	/**
	 * Display adjustment factor. Values are multiplied by this amount for
	 * display only.
	 */
	private final int displayFactor;

	/**
	 * Auto adjust the last unedited value or not.
	 */
	private final boolean autoAdjust;

	/**
	 * Total to use when adjusting the last unedited value.
	 */
	private final double autoAdjustTotal;

	private final List<Integer> uneditedValuesIndex =
			new ArrayList<Integer>();

	/**
	 * Construct table model with adjustment factor of 1 and no auto adjust
	 * total.
	 * 
	 * @param values
	 *            row values from parameter set
	 */
	public MutableNumeratorTableModel(MutableNumerator[] values) {
		this(values, 1, Double.NaN);
	}

	/**
	 * Construct table model with an adjustment factor.
	 * 
	 * @param values
	 *            row values from parameter set
	 * @param displayScaleFactor
	 *            display scale factor. Values are multiplied by this amount for
	 *            display only.
	 * @param autoAdjustTotal
	 *            total to automatically adjust the last unedited
	 *            {@link MutableNumerator}, or Double.NaN if no adjustment of
	 *            the last unedited {@link MutableNumerator} is to occur. This
	 *            total is before {@code displayScaleFactor} is applied for
	 *            display.
	 */
	public MutableNumeratorTableModel(MutableNumerator[] values,
			int displayScaleFactor, double autoAdjustTotal) {
		this.values = values;
		this.displayFactor = displayScaleFactor;
		this.autoAdjustTotal = autoAdjustTotal;
		this.autoAdjust = !Double.isNaN(autoAdjustTotal);
		filluneditedValuesIndex(values.length);
	}

	private void filluneditedValuesIndex(int size) {
		for (int i = 0; i < size; i++) {
			uneditedValuesIndex.add(i);
		}
	}

	@Override
	public String getColumnName(int col) {
		return COLUMN_NAMES[col];
	}

	@Override
	public int getColumnCount() {
		return 3;
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

		} else if (c == 1 || c == 2) {
			// values
			return Double.class;

		} else {
			throw new IllegalStateException("column " + c + " does not exist");
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		// values column is editable
		return (col == 2);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			// names
			return values[rowIndex].getName();

		} else if (columnIndex == 1) {
			// original values
			return values[rowIndex].getOriginalValue() * displayFactor;

		} else if (columnIndex == 2) {
			// values
			return values[rowIndex].doubleValue() * displayFactor;

		} else {
			throw new IllegalStateException("column " + columnIndex
					+ " does not exist");
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {

		if (col != 2) {
			throw new IllegalStateException("column " + col
					+ " is not editable");
		}

		double dvalue = ((Double) value).doubleValue() / displayFactor;

		values[row].setNumerator(dvalue);
		fireTableCellUpdated(row, col);

		if (autoAdjust) {

			uneditedValuesIndex.remove(Integer.valueOf(row));

			if (uneditedValuesIndex.size() == 1) {
				// get and remove last uneditedvaluesIndex
				int lastUneditedValueIndex = uneditedValuesIndex.remove(0);

				// update last unedited value

				double sumOfEditedValues = 0;

				for (int i = 0; i < values.length; i++) {
					if (i != lastUneditedValueIndex) {
						sumOfEditedValues += values[i].doubleValue();
					}
				}

				double remainder = autoAdjustTotal - sumOfEditedValues;

				// System.err.println("updating " + lastUneditedValueIndex +
				// " to " + remainder);

				values[lastUneditedValueIndex].setNumerator(remainder);
				fireTableCellUpdated(lastUneditedValueIndex, col);

				// reset
				filluneditedValuesIndex(values.length);

			}
		}

	}

}
