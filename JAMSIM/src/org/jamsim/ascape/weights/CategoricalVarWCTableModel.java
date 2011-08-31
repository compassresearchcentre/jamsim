package org.jamsim.ascape.weights;

import javax.swing.table.AbstractTableModel;

import org.jamsim.math.MutableNumerator;
import org.omancode.math.LastPosRemainder;

/**
 * Table Model for the weighting of a categorical variable. Based on an array of
 * {@link MutableNumerator}s. Modifications to the table model update the
 * numerator of the underlying {@link MutableNumerator}s.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class CategoricalVarWCTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3155978328221657804L;

	private static final String[] COLUMN_NAMES = { "Category", "Base (%)",
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
	 * Used to calculate the remainder for the last unedited value.
	 */
	private final LastPosRemainder posRemainder;

	/**
	 * Construct table model with adjustment factor of 1 and no auto adjust
	 * total.
	 * 
	 * @param values
	 *            row values from parameter set
	 */
	public CategoricalVarWCTableModel(MutableNumerator[] values) {
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
	 *            total used to automatically adjust the last unedited
	 *            {@link MutableNumerator} in a row, or Double.NaN if no
	 *            adjustment of the last unedited {@link MutableNumerator} is to
	 *            occur. This total is before {@code displayScaleFactor} is
	 *            applied for display.
	 */
	public CategoricalVarWCTableModel(MutableNumerator[] values,
			int displayScaleFactor, double autoAdjustTotal) {
		this.values = values;
		this.displayFactor = displayScaleFactor;

		this.posRemainder =
				new LastPosRemainder(values.length, autoAdjustTotal);
		this.autoAdjust = !Double.isNaN(autoAdjustTotal);
	}

	@Override
	public String getColumnName(int col) {
		return COLUMN_NAMES[col];
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
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
		// weighting column is editable
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

		double dvalue =
				(value == null) ? 0 : ((Double) value).doubleValue()
						/ displayFactor;

		values[row].setNumerator(dvalue);
		fireTableCellUpdated(row, col);

		if (autoAdjust) {

			int lastPos = posRemainder.fill(row, dvalue);

			if (lastPos != -1) {
				values[lastPos].setNumerator(posRemainder.getRemainder());
				fireTableCellUpdated(lastPos, col);
			}

		}

	}

}
