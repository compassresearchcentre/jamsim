package org.jamsim.ascape;

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
 * Utility class of methods related to JTrees
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
			TreePath tp = e.getPath();

			// Get the selected node.
			Object node = tp.getLastPathComponent();

			/* if node exists */
			if (node != null) {

				/*
				 * if node is an ActionListener, execute the actionPerformed
				 * method
				 */
				if (node instanceof ActionListener) {
					((ActionListener) node).actionPerformed(null);
				}
			}
		}

	}

	/**
	 * Tree node that displays a panel view when clicked on
	 * 
	 * @author Oliver Mannion
	 * 
	 */
	public static class PanelViewNode extends DefaultMutableTreeNode
			implements ActionListener {

		private static final long serialVersionUID = 6327681491877012320L;
		protected PanelView panelView;
		protected Scape scape;
		protected String name;

		/**
		 * Create a PanelViewNode that when clicked will display the passed in
		 * JTable. The node's title is the name of the table.
		 * 
		 * @param table
		 *            JTable to display in a PanelView when this node is
		 *            clicked.
		 */
		public PanelViewNode(Scape scape, JTable table) {
			super(table);
			this.scape = scape;
			this.name = table.getName();

			// Create a PanelView from the Table
			panelView = AscapeGUIUtil.createPanelView(table);

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
			if (name != null) {
				return name;
			} else {
				return super.toString();
			}
		}

	}

}
