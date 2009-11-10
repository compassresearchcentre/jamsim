package org.jamsim.ascape;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.ascape.model.Scape;
import org.ascape.runtime.RuntimeEnvironment;
import org.ascape.runtime.swing.DesktopEnvironment;
import org.ascape.view.vis.PanelView;

/**
 * Utility functions related to the Ascape GUI.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class AscapeGUIUtil {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private AscapeGUIUtil() {
	}

	/**
	 * Return the dimensions of Ascape's desktop pane, i.e: the pane where
	 * charts, tables etc. are displayed.
	 * 
	 * @param scape
	 *            scape
	 * @return dimensions of Ascape's desktop pane.
	 */
	public static Dimension getDesktopSize(Scape scape) {
		RuntimeEnvironment runtime = scape.getRunner().getEnvironment();
		if (runtime instanceof DesktopEnvironment) {
			return ((DesktopEnvironment) runtime).getUserFrame()
					.getDeskScrollPane().getViewport().getSize();
		} else {
			return null;
		}
	}

	/**
	 * Create Panel View that resizes its components when it is resized. This is
	 * a convenience method.
	 * 
	 * @param name
	 * @return a PanelView.
	 */
	public static PanelView createResizablePanelView(String name) {
		return createPanelView(name, new BorderLayout());
	}

	/**
	 * Create a new Panel View with the specified name and LayoutManager.
	 * 
	 * @param name
	 *            name of the panel view, can be {@code null}.
	 * @param mgr
	 *            LayoutManager, if {@code null} the default (FlowLayout) is
	 *            used.
	 * @return a PanelView.
	 */
	public static PanelView createPanelView(String name, LayoutManager mgr) {
		// create a new PanelView and override updateScapeGraphics()
		// otherwise when using the BorderLayout manager the model
		// will stall (for some reason you don't need this
		// overridden method when using the FlowLayout manager)
		PanelView pv = new PanelViewNoStall();

		// set the layout manager
		if (mgr != null) {
			pv.setLayout(mgr);
		}

		// set the name
		if (name != null) {
			pv.setName(name);
		}

		return pv;
	}

	/**
	 * Subtract one dimension from another.
	 * 
	 * @param d1
	 *            dimension 1
	 * @param d2
	 *            dimension 2
	 * @return {@code d1 - d2}
	 */
	public static Dimension subtract(Dimension d1, Dimension d2) {
		return new Dimension(d1.width - d2.width, d1.height - d2.height);
	}

	/**
	 * Add two dimensions together.
	 * 
	 * @param d1
	 *            dimension 1
	 * @param d2
	 *            dimension 2
	 * @return {@code d1 + d2}
	 */
	public static Dimension add(Dimension d1, Dimension d2) {
		return new Dimension(d1.width + d2.width, d1.height + d2.height);
	}

	/**
	 * Return the smallest width and height of two dimensions.
	 * 
	 * @param d1
	 *            dimension 1
	 * @param d2
	 *            dimension 2
	 * @return new dimensions with {@code width = min(d1.width,d2.width} and
	 *         {@code height = min(d1.height,d2.height} .
	 */
	public static Dimension min(Dimension d1, Dimension d2) {
		return new Dimension(Math.min(d1.width, d2.width), Math.min(
				d1.height, d2.height));
	}

	/**
	 * The border around the max size of a Panel View. See
	 * {@link #createPanelView(JTable, Dimension)}.
	 */
	public static final Dimension BORDER_EDGES = new Dimension(20, 20);

	/**
	 * Create a PanelView that displays a table.
	 * 
	 * @param table
	 *            table to display.
	 * @param maxSize
	 *            max display dimensions of the table, less
	 *            {@link #BORDER_EDGES}. If {@code null} will display the table
	 *            at it's preferred size.
	 * @return PanelView containing the table.
	 */
	public static PanelView createPanelView(JTable table, Dimension maxSize) {
		// sizeAllColumnsToHeaderWidths(table);

		// tell the scroll pane enclosing the table to size its viewport
		// to the table's preferred size.
		// table.setPreferredScrollableViewportSize(table.getPreferredSize());

		if (maxSize == null) {
			table
					.setPreferredScrollableViewportSize(table
							.getPreferredSize());
		} else {

			Dimension prefSize = table.getPreferredSize();

			table.setPreferredScrollableViewportSize(min(prefSize, subtract(
					maxSize, BORDER_EDGES)));
		}

		// allow sorting using the column headers
		table.setAutoCreateRowSorter(true);

		// do not resize the table column widths when the frame is resized;
		// instead show the scroll bars
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// create a new PanelView with the BorderLayout so that when
		// the panel view is resized so are the components on it
		// use the name of the table for the name of the Panel
		PanelView pv = createPanelView(table.getName(), new BorderLayout());

		// In order for the table column headings to be visible,
		// it must be on a scroll pane
		JScrollPane scrollPane = new JScrollPane(table);
		pv.add(scrollPane, BorderLayout.CENTER);

		return pv;
	}

	/**
	 * Fit all the columns of a table to the width of the header.
	 * 
	 * @param table
	 *            table to fit.
	 */
	public static void sizeAllColumnsToHeaderWidths(JTable table) {
		TableColumn col;

		for (int i = 0; i < table.getColumnCount(); i++) {
			col = table.getColumnModel().getColumn(i);
			col.sizeWidthToFit();
		}
	}

	/**
	 * A PanelView subclass that implements updateScapeGraphics so that the
	 * model doesn't stall during after scape update events.
	 * 
	 * @author Oliver Mannion
	 * 
	 */
	public static class PanelViewNoStall extends PanelView {
		/**
		 * Called when scape reports an update event. (No need to call this
		 * method after updating panel.)
		 */
		@Override
		public void updateScapeGraphics() {
			super.updateScapeGraphics();

			// via the delegate tell the scape that the PanelView
			// has finished updating. Must be called otherwise the
			// model will stall
			delegate.viewPainted();
		}
	}

}