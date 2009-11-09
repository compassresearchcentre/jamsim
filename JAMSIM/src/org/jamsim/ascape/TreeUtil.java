package org.jamsim.ascape;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.ascape.model.Scape;
import org.ascape.view.vis.PanelView;

/**
 * Utility class of methods related to JTrees.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class TreeUtil {

	/**
	 * A Tree Selection Listener that will call the actionPerformed method on
	 * the currently selected node if that node implements the ActionListener
	 * interface.
	 * 
	 * @author Oliver Mannion
	 * 
	 */
	public static class TSLPeformActionOnSelectedNode implements
			TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent e) {

			// Get the path to the selection.
			TreePath path = e.getPath();

			// Get the selected node.
			Object node = path.getLastPathComponent();

			// if node exists and node is an ActionListener, execute the
			// actionPerformed method
			if (node instanceof ActionListener) {
				((ActionListener) node).actionPerformed(null);
			}
		}
	}

	/**
	 * Tree node that displays a panel view when clicked on.
	 * 
	 * @author Oliver Mannion
	 * 
	 */
	public static class PanelViewNode extends DefaultMutableTreeNode
			implements ActionListener {

		private static final long serialVersionUID = 6327681491877012320L;
		private final PanelView panelView;
		private final Scape scape;
		private final String name;

		/**
		 * Create a PanelViewNode that when clicked will display the passed in
		 * JTable. The node's title is the name of the table.
		 * 
		 * @param scape
		 *            scape to associate this instance with.
		 * @param table
		 *            JTable to display in a PanelView when this node is
		 *            clicked.
		 * @param maxSize
		 *            max display dimensions of the table. If {@code null} will
		 *            display the table at it's preferred size.
		 */
		public PanelViewNode(Scape scape, JTable table, Dimension maxSize) {
			super(table);
			this.scape = scape;
			this.name = table.getName();

			// Create a PanelView from the Table
			panelView = AscapeGUIUtil.createPanelView(table, maxSize);

		}

		/**
		 * Called by
		 * {@link TSLPeformActionOnSelectedNode#valueChanged(TreeSelectionEvent)}
		 * when this node is selected.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			displayPanelView();
		}

		/**
		 * Display the PanelView associated with this node.
		 */
		public void displayPanelView() {

			// check to see that panelView is not currently displayed
			// (i.e: has been added to the scape)
			if (scape.getScapeListeners().indexOf(panelView) == -1) {

				// If a PanelView is closed and then re-opened, it is empty
				panelView.validate();
				// add the panelView to the scape, which will display it in the
				// GUI
				scape.addView(panelView);
			}
		}

		@Override
		public String toString() {
			if (name == null) {
				return super.toString();
			} else {
				return name;
			}
		}

	}

}
