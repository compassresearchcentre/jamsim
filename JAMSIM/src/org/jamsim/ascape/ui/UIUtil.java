package org.jamsim.ascape.ui;

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.ascape.util.swing.PanelViewUtil;
import org.omancode.util.swing.DoubleCellRenderer;
import org.omancode.util.swing.JTableExcelStyleEdit;

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
		JTableExcelStyleEdit table = new JTableExcelStyleEdit(tmodel); // NOPMD
		table.setName(name);

		// allow cell selection instead of row selection
		table.setCellSelectionEnabled(true);

		// prevent non editable cells from receiving focus
		table.setPreventNonEditCellFocus(true);

		TableCellRenderer dblRenderer = getDoubleCellRenderer();
		table.setDefaultRenderer(Double.class, dblRenderer);

		//SwingUtil.packAllColumns(table);
		table.setRowHeight(22);
				
		// allow sorting using the column headers
		//table.setAutoCreateRowSorter(true);

		// do not resize the table column widths when the frame is resized;
		// instead show the scroll bars
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		//PanelViewUtil.sizeAllColumnsToHeaderWidths(table);
		
		TableColumn col;

		for (int i = 0; i < table.getColumnCount(); i++) {
			col = table.getColumnModel().getColumn(i);
			col.setPreferredWidth(220);
		}
		return table;
	}
}