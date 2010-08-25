package org.omancode.swing;

import java.awt.Component;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

/**
 * {@link JTable} that provides Excel style editing of cells. 
 * <ul><li>When editing a cell via typing, all text is replaced. 
 * <li>When pressing F2, all text is selected.
 * <li>When the focus is lost from the table, any edits are saved
 * <li>Ability to prevent focus from entering uneditable cells.
 * </ul>
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class JTableExcelStyleEdit extends JTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7148157021372169205L;

	/**
	 * Prevent the focus from moving to a non-editable cell.
	 */
	private boolean preventNonEditCellFocus = false;

	/**
	 * Set whether focus is prevented from moving to a non-editable cell. By
	 * default this is {@code false} (ie: do not prevent).
	 * 
	 * @param preventNonEditCellFocus
	 *            prevent non edit cell focus
	 */
	public void setPreventNonEditCellFocus(boolean preventNonEditCellFocus) {
		this.preventNonEditCellFocus = preventNonEditCellFocus;
	}


	/**
	 * Get the preventing focus in non-editable cells status.
	 * 
	 * @return prevent non edit cell focus
	 */
	public boolean isPreventNonEditCellFocus() {
		return preventNonEditCellFocus;
	}

	/**
	 * Constructs a default {@link JTableExcelStyleEdit} that is initialized
	 * with a default data model, a default column model, and a default
	 * selection model.
	 * 
	 * @see #createDefaultDataModel
	 * @see #createDefaultColumnModel
	 * @see #createDefaultSelectionModel
	 */
	public JTableExcelStyleEdit() {
		this(null, null, null);
	}

	/**
	 * Constructs a {@link JTableExcelStyleEdit} that is initialized with
	 * <code>dm</code> as the data model, a default column model, and a default
	 * selection model.
	 * 
	 * @param dm
	 *            the data model for the table
	 * @see #createDefaultColumnModel
	 * @see #createDefaultSelectionModel
	 */
	public JTableExcelStyleEdit(TableModel dm) {
		this(dm, null, null);
	}

	/**
	 * Constructs a {@link JTableExcelStyleEdit} that is initialized with
	 * <code>dm</code> as the data model, <code>cm</code> as the column model,
	 * and a default selection model.
	 * 
	 * @param dm
	 *            the data model for the table
	 * @param cm
	 *            the column model for the table
	 * @see #createDefaultSelectionModel
	 */
	public JTableExcelStyleEdit(TableModel dm, TableColumnModel cm) {
		this(dm, cm, null);
	}

	/**
	 * Constructs a {@link JTableExcelStyleEdit} that is initialized with
	 * <code>dm</code> as the data model, <code>cm</code> as the column model,
	 * and <code>sm</code> as the selection model. If any of the parameters are
	 * <code>null</code> this method will initialize the table with the
	 * corresponding default model. The <code>autoCreateColumnsFromModel</code>
	 * flag is set to false if <code>cm</code> is non-null, otherwise it is set
	 * to true and the column model is populated with suitable
	 * <code>TableColumns</code> for the columns in <code>dm</code>.
	 * 
	 * @param dm
	 *            the data model for the table
	 * @param cm
	 *            the column model for the table
	 * @param sm
	 *            the row selection model for the table
	 * @see #createDefaultDataModel
	 * @see #createDefaultColumnModel
	 * @see #createDefaultSelectionModel
	 */
	public JTableExcelStyleEdit(TableModel dm, TableColumnModel cm,
			ListSelectionModel sm) {
		super(dm, cm, sm);

		// tell the JTable to put the focus in the cell (ie: the flashing text
		// entry bar) after the user starts typing when the cell is selected
		setSurrendersFocusOnKeystroke(true);

		// tell JTable to stop editing and save any changes when the table
		// loses focus. This means edits will be saved when clicking on
		// another component, eg: button.
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}

	/**
	 * Constructs a {@link JTableExcelStyleEdit} with <code>numRows</code> and
	 * <code>numColumns</code> of empty cells using
	 * <code>DefaultTableModel</code>. The columns will have names of the form
	 * "A", "B", "C", etc.
	 * 
	 * @param numRows
	 *            the number of rows the table holds
	 * @param numColumns
	 *            the number of columns the table holds
	 * @see javax.swing.table.DefaultTableModel
	 */
	public JTableExcelStyleEdit(int numRows, int numColumns) {
		this(new DefaultTableModel(numRows, numColumns));
	}

	/**
	 * Constructs a {@link JTableExcelStyleEdit} to display the values in the
	 * <code>Vector</code> of <code>Vectors</code>, <code>rowData</code>, with
	 * column names, <code>columnNames</code>. The <code>Vectors</code>
	 * contained in <code>rowData</code> should contain the values for that row.
	 * In other words, the value of the cell at row 1, column 5 can be obtained
	 * with the following code:
	 * <p>
	 * 
	 * <pre>
	 * ((Vector) rowData.elementAt(1)).elementAt(5);
	 * </pre>
	 * <p>
	 * 
	 * @param rowData
	 *            the data for the new table
	 * @param columnNames
	 *            names of each column
	 */
	public JTableExcelStyleEdit(Vector rowData, Vector columnNames) {
		this(new DefaultTableModel(rowData, columnNames));
	}

	/**
	 * Constructs a {@link JTableExcelStyleEdit} to display the values in the
	 * two dimensional array, <code>rowData</code>, with column names,
	 * <code>columnNames</code>. <code>rowData</code> is an array of rows, so
	 * the value of the cell at row 1, column 5 can be obtained with the
	 * following code:
	 * <p>
	 * 
	 * <pre>
	 * rowData[1][5];
	 * </pre>
	 * <p>
	 * All rows must be of the same length as <code>columnNames</code>.
	 * <p>
	 * 
	 * @param rowData
	 *            the data for the new table
	 * @param columnNames
	 *            names of each column
	 */
	public JTableExcelStyleEdit(final Object[][] rowData,
			final Object[] columnNames) {
		this(new AbstractTableModel() {
			public String getColumnName(int column) {
				return columnNames[column].toString();
			}

			public int getRowCount() {
				return rowData.length;
			}

			public int getColumnCount() {
				return columnNames.length;
			}

			public Object getValueAt(int row, int col) {
				return rowData[row][col];
			}

			public boolean isCellEditable(int row, int column) {
				return true;
			}

			public void setValueAt(Object value, int row, int col) {
				rowData[row][col] = value;
				fireTableCellUpdated(row, col);
			}
		});
	}

	/**
	 * Select the text when the cell starts editing.
	 * <ul>
	 * <li>text will be replaced when you start typing in a cell
	 * <li>text will be selected when you use F2 to start editing.
	 * </ul>
	 * 
	 * @param editor
	 *            the <code>TableCellEditor</code> to set up
	 * @param row
	 *            the row of the cell to edit, where 0 is the first row
	 * @param column
	 *            the column of the cell to edit, where 0 is the first column
	 * @return the <code>Component</code> being edited
	 */
	@Override
	public Component prepareEditor(TableCellEditor editor, int row, int column) {

		Component comp = super.prepareEditor(editor, row, column);

		if (comp instanceof JTextComponent) {
			((JTextField) comp).selectAll();
		}

		return comp;
	}

	/**
	 * This method is called when the user tries to move to a different cell. If
	 * the cell they're trying to move to is not editable, we look for then next
	 * cell in the proper direction that is editable.
	 * 
	 * @param rowIndex
	 *            affects the selection at <code>row</code>
	 * @param columnIndex
	 *            affects the selection at <code>column</code>
	 * @param toggle
	 *            see description above
	 * @param extend
	 *            if true, extend the current selection
	 */
	@Override
	public void changeSelection(int rowIndex, int columnIndex,
			boolean toggle, boolean extend) {

		if (!preventNonEditCellFocus
				|| getModel().isCellEditable(rowIndex, columnIndex)) {
			// It's an editable cell, so leave the selection here.
			super.changeSelection(rowIndex, columnIndex, toggle, extend);

		} else {

			// Find the row and column we're coming from.
			int curRow = getEditingRow();
			int curCol = getEditingColumn();
			if (curRow == -1) {
				curRow = getSelectedRow();
			}
			if (curCol == -1) {
				curCol = getSelectedColumn();
			}

			// We may need to wrap-around.
			int nRows = getRowCount();
			int nCols = getColumnCount();

			// If we can't find a cell to move to, we'll stay here.
			int nextRow = rowIndex;
			int nextCol = columnIndex;

			if (columnIndex == curCol) {
				// Up or down motion - go only up or down.
				int direction = rowIndex - curRow;
				if (direction > 1) {
					direction = 1;
				} else if (direction < -1) {
					direction = -1;
				}
				nextRow =
						findNextEditableRow(rowIndex, columnIndex, direction,
								nRows);
			} else if (rowIndex == curRow) {
				// Left-or-right motion - use the "natural" (for Americans)
				// order:
				// left-to-right, top-to-bottom, or vice-versa if we're trying
				// to move to the left. We'll wrap from the bottom row to the
				// top
				// and vice-versa if necessary.
				int direction = columnIndex - curCol;
				if (direction > 1) {
					direction = 1;
				} else if (direction < -1) {
					direction = -1;
				}
				int[] nextCell =
						findNextEditableCell(rowIndex, columnIndex,
								direction, nRows, nCols);
				nextRow = nextCell[0];
				nextCol = nextCell[1];
			} else {
				// Both row and column differ. This probably means we've
				// moved off the end of a row, or else the user has clicked
				// on some random cell. The direction is controlled
				// by the row difference (this doesn't always do something
				// intuitive; always setting direction=1 might work better).
				int direction = rowIndex - curRow;
				if (direction > 1) {
					direction = 1;
				} else if (direction < -1) {
					direction = -1;
				}
				if ((rowIndex == 0) && (curRow == nRows - 1)) {
					direction = 1;
				}
				int[] nextCell =
						findNextEditableCell(rowIndex, columnIndex,
								direction, nRows, nCols);
				nextRow = nextCell[0];
				nextCol = nextCell[1];
			}
			// Go to the cell we found.
			super.changeSelection(nextRow, nextCol, toggle, extend);
		}
	}

	// Search for an editable cell starting at row,col and using the
	// "natural" order.
	private int[] findNextEditableCell(int startRow, int startCol,
			int direction, int nRows, int nCols) {
		int row = startRow;
		int col = startCol;
		do {
			col = col + direction;
			if (col >= nCols) {
				col = 0;
				row += direction;
			}
			if (col < 0) {
				col = nCols - 1;
				row += direction;
			}
			if (row >= nRows) {
				row = 0;
			} else if (row < 0) {
				row = nRows - 1;
			}
			// System.out.println("FNEC looking at " + row + ',' + col);
			if (isCellEditable(row, col)) {
				return new int[] { row, col };
			}
		} while (!((row == startRow) && (col == startCol)));

		// Nothing editable found; stay here.
		return new int[] { startRow, startCol };
	}

	// Search directly above/below for an editable cell.
	private int findNextEditableRow(int startRow, int col, int direction,
			int nRows) {
		int row = startRow;
		do {
			row = row + direction;
			if (row < 0) {
				row = nRows - 1;
			} else if (row >= nRows) {
				row = 0;
			}
			if (isCellEditable(row, col)) {
				return row;
			}
		} while (row != startRow);
		// Nothing editable found, stay here.
		return startRow;
	}

}