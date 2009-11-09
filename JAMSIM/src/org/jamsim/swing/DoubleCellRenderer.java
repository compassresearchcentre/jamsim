package org.jamsim.swing;

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
	
	/**
	 * Construct {@link DoubleCellRenderer} with the specified digit precision.
	 * 
	 * @param numberDigitsToDisplay
	 *  number of digits to display in the cell.
	 */
	public DoubleCellRenderer(int numberDigitsToDisplay) {
		super();
		formatter.setMaximumFractionDigits(numberDigitsToDisplay);
		//setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
	}

	@Override
	public void setValue(Object value) {
	    setText((value == null) ? "" : formatter.format(value));
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
