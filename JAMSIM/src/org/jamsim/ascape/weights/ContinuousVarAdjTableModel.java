package org.jamsim.ascape.weights;

import javax.swing.table.AbstractTableModel;

import org.omancode.math.NamedNumber;

/**
 * TableModel for the adjustment of a continuous variable. Display a series of
 * levels, the current proportion at each level, and allows the user to specify
 * an amount to increment each level by.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ContinuousVarAdjTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -480857798617108285L;
	private static final String[] COLUMN_NAMES = { "Level", "%",
			"Adjust By" };

	private NamedNumber[] props;
	private double[] increments;

	/**
	 * Create new {@link ContinuousVarAdjTableModel} using supplied proportions.
	 * 
	 * @param props
	 *            proportions.
	 */
	public ContinuousVarAdjTableModel(NamedNumber[] props) {
		setProps(props);
	}

	/**
	 * Set the proportions displayed.
	 * 
	 * @param props
	 *            proportions
	 */
	public final void setProps(NamedNumber[] props) {
		this.props = props;
		this.increments = new double[props.length];
		fireTableDataChanged();
	}

	/**
	 * Get increments entered by user.
	 * 
	 * @return increments
	 */
	public double[] getIncrements() {
		return increments;
	}

	@Override
	public int getRowCount() {
		return props.length;
	}

	@Override
	public Class<?> getColumnClass(int c) {
		if (c == 0) {
			// levels
			return String.class;

		} else if (c == 1) {
			// props
			return Double.class;

		} else if (c == 2) {
			// increments
			return Double.class;

		} else {
			throw new IllegalStateException("column " + c + " does not exist");
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		// increment column is editable
		return (col == 2);
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public String getColumnName(int col) {
		return COLUMN_NAMES[col];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			// levels
			return props[rowIndex].getName();

		} else if (columnIndex == 1) {
			// props
			return props[rowIndex].doubleValue() * 100;

		} else if (columnIndex == 2) {
			// increments
			return increments[rowIndex];

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

		increments[row] = ((Double) value).doubleValue();
		fireTableCellUpdated(row, col);
	}

}
