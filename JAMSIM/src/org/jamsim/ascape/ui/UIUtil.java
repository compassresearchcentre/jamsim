package org.jamsim.ascape.ui;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.omancode.swing.DoubleCellRenderer;
import org.omancode.swing.SwingUtil;

/**
 * Utility functions related to Ascape Swing GUI components provided by JAMSIM.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class UIUtil {

	private UIUtil() {

	}

	/**
	 * Create a {@link DoubleCellRenderer}.
	 * 
	 * @return double cell renderer
	 */
	public static DoubleCellRenderer getDoubleCellRenderer() {
		return new DoubleCellRenderer(2);
	}

	/**
	 * Create a {@link JTable} with appropriate renderer and columns widths.
	 * 
	 * @param tmodel
	 *            table model
	 * @param name
	 *            table name
	 * @return table
	 */
	public static JTable createTable(TableModel tmodel, String name) {
		JTable table = new JTable(tmodel); // NOPMD
		table.setName(name);

		TableCellRenderer dblRenderer = getDoubleCellRenderer();
		table.setDefaultRenderer(Double.class, dblRenderer);
		
		SwingUtil.packAllColumns(table, 5);

		return table;

	}

}
