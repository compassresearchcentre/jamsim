package org.omancode.swing;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * Display doubles to a specified digit precision.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class DoubleCellRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5432131198692932810L;

	private final NumberFormat formatter = NumberFormat.getInstance();

	// private final NumberFormat formatter = new DecimalFormat("0.0000000000");

	/**
	 * Construct {@link DoubleCellRenderer} which displays numbers to 10 decimal
	 * places.
	 */
	public DoubleCellRenderer() {
		this(2);
	}

	/**
	 * Construct {@link DoubleCellRenderer} with the specified digit precision.
	 * 
	 * @param numberDigitsToDisplay
	 *            number of digits to display in the cell.
	 */
	public DoubleCellRenderer(int numberDigitsToDisplay) {
		formatter.setMaximumFractionDigits(numberDigitsToDisplay);
		// setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
	}

	@Override
	public void setValue(Object value) {
		String text =
				(value == null || "".equals(value)) ? "" : formatter
						.format(value);
		setText(text);
	}

	/**
	 * Get number formatter. Used for testing.
	 * 
	 * @return number formatter
	 */
	public NumberFormat getFormatter() {
		return formatter;
	}

}