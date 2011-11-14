package org.jamsim.ascape.navigator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.ascape.runtime.swing.navigator.PopupMenuProvider;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.output.Saveable;
import org.omancode.util.io.FileUtil;

/**
 * Navigator node that displays subfolders of nodes for simulation runs. A sub
 * folder can be a run subfolder, eg: "Run 1", "Run 2", .... "All Runs" etc. or
 * a group subfolder (which has it's own run subfolders under it).
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class SubFolderNode extends DefaultMutableTreeNode implements
		PopupMenuProvider, Saveable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6134673429670006279L;
	private final DefaultTreeModel treeModel;
	private final MicroSimScape<?> msscape;

	private final Map<Integer, SubFolderNode> runNodes =
			new HashMap<Integer, SubFolderNode>();

	private final Map<String, SubFolderNode> groupNodes =
			new HashMap<String, SubFolderNode>();

	private final String allRunsNodeName;

	private final SaverAction saverAction = new SaverAction();

	/**
	 * The run number of the node representing all runs.
	 */
	public static final int ALLRUNS = 0;

	/**
	 * Construct a {@link SubFolderNode} with the All Runs node named
	 * "All runs".
	 * 
	 * @param name
	 *            name of node in the tree
	 * @param scape
	 *            used to get the output directory when saving nodes
	 * @param treeModel
	 *            tree model to add children nodes
	 */
	public SubFolderNode(String name, MicroSimScape<?> scape,
			DefaultTreeModel treeModel) {
		this(name, scape, treeModel, "All runs");
	}

	/**
	 * Construct a {@link SubFolderNode}.
	 * 
	 * @param name
	 *            name of node in the tree
	 * @param scape
	 *            used to get the output directory when saving nodes
	 * @param treeModel
	 *            tree model to add children nodes
	 * @param allRunsNodeName
	 *            name of node 0, ie: the node that contains results for all
	 *            runs
	 */
	public SubFolderNode(String name, MicroSimScape<?> scape,
			DefaultTreeModel treeModel, String allRunsNodeName) {
		super(name);
		this.msscape = scape;
		this.treeModel = treeModel;
		this.allRunsNodeName = allRunsNodeName;
	}

	/**
	 * Add a child node directly under the appropriate run subfolder for this
	 * node.
	 * 
	 * @param runNumber
	 *            the run number subfolder under which to create the child node.
	 *            If the run number is {@link #ALLRUNS}, then it is created
	 *            under the "All Runs" node.
	 * @param newNode
	 *            panel view node
	 */
	public void addChildNode(MutableTreeNode newNode, int runNumber) {
		SubFolderNode runParent = getRunSubFolderNode(runNumber);
		treeModel.insertNodeInto(newNode, runParent,
				runParent.getChildCount());
	}

	/**
	 * Add a child node directly under this node.
	 * 
	 * @param newNode
	 *            panel view node
	 */
	public void addChildNode(MutableTreeNode newNode) {
		addChildNode(newNode, null);
	}

	/**
	 * Add a child node directly under this node, or to a path under this node.
	 * 
	 * @param newNode
	 *            panel view node
	 * @param path
	 *            a path to a sub folder node, eg: "Base/Means" which represents
	 *            the folder Means under the folder Base, or just "Base" which
	 *            will add to the folder Base, or {@code null} to add directly
	 *            under this node.
	 */
	public void addChildNode(MutableTreeNode newNode, String path) {
		SubFolderNode groupNode =
				(path == null) ? this : getGroupSubFolderNode(path);

		treeModel.insertNodeInto(newNode, groupNode,
				groupNode.getChildCount());
	}

	/**
	 * Get the run sub folder node for the given run number. If it does not
	 * exist it is created.
	 * 
	 * @param runNumber
	 *            a run number, or {@link #ALLRUNS} for the "All Runs" node.
	 * @return run folder node, eg: "Run 1", "Run 2", .... "All Runs" etc.
	 */
	public SubFolderNode getRunSubFolderNode(int runNumber) {

		SubFolderNode runNode = runNodes.get(runNumber);

		// create node if it doesn't already exist
		if (runNode == null) {

			if (runNumber == ALLRUNS) {
				runNode =
						new SubFolderNode(allRunsNodeName, msscape, treeModel);

			} else {
				runNode =
						new SubFolderNode("Run " + runNumber, msscape,
								treeModel);
			}
			runNodes.put(runNumber, runNode);

			// add to tree
			treeModel.insertNodeInto(runNode, this, this.getChildCount());
		}

		return runNode;
	}

	/**
	 * Add a {@link SubFolderNode} group subfolder.
	 * 
	 * @param groupName
	 *            name of the group node
	 * @return newly created group node
	 */
	private SubFolderNode addGroupSubFolderNode(String groupName) {
		SubFolderNode groupNode =
				new SubFolderNode(groupName, msscape, treeModel);
		treeModel.insertNodeInto(groupNode, this, this.getChildCount());
		groupNodes.put(groupName, groupNode);
		return groupNode;
	}

	/**
	 * Gets a {@link SubFolderNode} child group subfolder. If it doesn't exist,
	 * it is created.
	 * 
	 * @param path
	 *            a path to a sub folder node, eg: "Base/Means" which represents
	 *            the folder Means under the folder Base, or just "Base" which
	 *            will add to the folder Base, or {@code null} or empty string
	 *            to get this node.
	 * @return group node
	 */
	public SubFolderNode getGroupSubFolderNode(String path) {
		if ("".equals(path) || path == null) {
			return this;
		}
		String[] pathNodes = path.split("/");

		String pathHead = pathNodes[0];
		SubFolderNode groupNode = groupNodes.get(pathHead);

		// create node if it doesn't already exist
		if (groupNode == null) {
			groupNode = addGroupSubFolderNode(pathHead);
		}

		String restOfPath =
				(pathNodes.length == 1) ? "" : path
						.substring(pathHead.length() + 1);

		return groupNode.getGroupSubFolderNode(restOfPath);
	}

	/**
	 * Right click popup menu that allows saving of this node.
	 * 
	 * @return popup menu.
	 */
	public JPopupMenu getPopupMenu() {
		String label = "Save to CSV (" + toString() + ")";

		JPopupMenu popup = new JPopupMenu();
		JMenuItem item = new JMenuItem(label);
		item.addActionListener(saverAction);

		popup.add(item);
		return popup;
	}

	/**
	 * Action that saves this node to a CSV file.
	 */
	private class SaverAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				saveToCSV(FileUtil.addTrailingSlash(msscape
						.getOutputDirectory()) + getNodePath(false));
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}

	}

	/**
	 * Perform {@link Saveable#saveToCSV(String)} on all {@link Saveable}
	 * children.
	 * 
	 * @param directory
	 *            directory
	 * @throws IOException
	 *             if problem saving
	 */
	@Override
	public void saveToCSV(String directory) throws IOException {

		for (Object node : children) {
			if (node instanceof Saveable) {
				((Saveable) node).saveToCSV(FileUtil
						.addTrailingSlash(directory) + toString());
			}
		}

	}

	/**
	 * Get a string representation of this node's path, from the top most parent
	 * {@link SubFolderNode}. eg: "Output Tables\Frequency Table\Run 1"
	 * 
	 * @param includeThisNode
	 *            include this node in the path
	 * @return node path
	 */
	public String getNodePath(boolean includeThisNode) {
		TreeNode[] nodes = getPath();
		StringBuilder sb = new StringBuilder(256);

		for (TreeNode node : nodes) {
			if (node instanceof SubFolderNode
					&& (includeThisNode || !node.equals(this))) {
				sb.append(node.toString());
				sb.append("\\");
			}
		}

		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}
}