package org.jamsim.ascape.navigator;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.ascape.runtime.swing.navigator.PanelViewNode;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.runtime.swing.navigator.PanelViewTable;
import org.ascape.runtime.swing.navigator.ScapeNode;
import org.ascape.runtime.swing.navigator.TreeBuilder;
import org.ascape.util.swing.AscapeGUIUtil;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.output.OutputDatasetProvider;
import org.jamsim.ascape.output.REXPDatasetProvider;
import org.jamsim.ascape.output.ROutput;
import org.jamsim.ascape.output.Saveable;
import org.jamsim.ascape.r.PanelViewDatasetProvider;
import org.jamsim.ascape.r.PanelViewRTextCommand;
import org.jamsim.ascape.ui.PanelViewParameterSet;
import org.jamsim.ascape.ui.UIUtil;
import org.jamsim.io.ParameterSet;

/**
 * Navigator tree node for a {@link MicroSimScape}. Same as {@link ScapeNode}
 * but in addition:
 * <ul>
 * <li>creates a "Datasets" node child with {@link PanelViewNode}s for all the
 * data in the scape
 * <li>creates an "Output Tables" node and exposes access to this node so scapes
 * can add tables during or after the simulation run
 * </ul>
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class MicroSimScapeNode extends ScapeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2933396206340650491L;

	private final DefaultTreeModel treeModel;

	/**
	 * Output tables nodes.
	 */
	private final SubFolderNode outputTablesNode;

	/**
	 * Map of data frames added. Used to prevent addition of duplicates.
	 */
	private final Map<String, String> dataFrameNodeMap =
			new HashMap<String, String>();

	/**
	 * Map of table nodes added.
	 */
	private final Map<String, SubFolderNode> tableNodeMap =
			new HashMap<String, SubFolderNode>();

	private final MicroSimScape<?> scape;

	/**
	 * Get output tables node. Exposed so scapes can add output tables during /
	 * after a simulation run.
	 * 
	 * @return output tables node
	 */
	public SubFolderNode getOutputTablesNode() {
		return outputTablesNode;
	}

	/**
	 * Instantiates a new scape node.
	 * 
	 * @param scape
	 *            the scape
	 * @param treeBuilder
	 *            tree builder
	 */
	public MicroSimScapeNode(MicroSimScape<?> scape, TreeBuilder treeBuilder) {
		super(scape, treeBuilder);

		this.scape = scape;

		treeModel = treeBuilder.getTreeModel();

		addDatasetNodes(scape, treeModel);
		addParameterSetNodes(scape);

		// create the Output Tables node
		outputTablesNode =
				new SubFolderNode("Output Tables", scape, treeModel);
		treeModel
				.insertNodeInto(outputTablesNode, this, this.getChildCount());
	}

	private void addDatasetNodes(MicroSimScape<?> scape,
			DefaultTreeModel treeModel) {
		addNodeWithChildrenTables(treeModel, "Datasets", scape.getScapeData()
				.getInputDatasets());
	}

	private void addNodeWithChildrenTables(DefaultTreeModel treeModel,
			String nodeName, Map<String, TableModel> childrenTables) {

		// create parent node
		DefaultMutableTreeNode parentNode =
				new DefaultMutableTreeNode(nodeName);

		// get all the tables from childrenTables
		// and add them to the navigator as a Panel View Node
		for (Map.Entry<String, TableModel> entry : childrenTables.entrySet()) {

			JTable table =
					UIUtil.createTable(entry.getValue(), entry.getKey());

			// add PanelViewNode to the tree
			PanelViewProvider provider = new PanelViewTable(table);
			PanelViewNode newNode = new PanelViewNode(provider);
			parentNode.add(newNode); // NOPMD
		}

		// add parentNode via the Tree Model
		treeModel.insertNodeInto(parentNode, this, this.getChildCount());
	}

	private void addParameterSetNodes(MicroSimScape<?> scape) {

		Map<String, ParameterSet> psets =
				scape.getScapeData().getParameterSets();

		if (psets != null) {
			for (Map.Entry<String, ParameterSet> entry : psets.entrySet()) {
				ParameterSet pset = entry.getValue();
				addParameterSetNode(new PanelViewParameterSet(pset,
						scape.getPrefs()));
			}
		}
	}

	/**
	 * Add a parameter set node under the "Parameter sets" folder. Creates
	 * "Parameter sets" node if it doesn't exist.
	 * 
	 * @param provider
	 *            panel view provider
	 */
	public final void addParameterSetNode(PanelViewProvider provider) {
		addNodeToOnDemandFolder(new PanelViewNode(provider),
				"Parameter sets", null);
	}

	/**
	 * Add node which displays the contents of the basefile when clicked on.
	 * 
	 * @param name
	 *            basefile node name
	 * @param rcmd
	 *            R command which returns a dataframe, ie: the basefile
	 */
	public void addBasefileNode(String name, String rcmd) {
		OutputDatasetProvider basefileDS =
				new ROutput(name, scape.getScapeRInterface(), rcmd);

		PanelViewDatasetProvider provider =
				new PanelViewDatasetProvider(basefileDS);

		treeModel.insertNodeInto(new PanelViewNode(provider), this,
				this.getChildCount());
	}

	/**
	 * Add a data frame node under the "Dataframes" folder. Creates "Dataframes"
	 * node if it doesn't exist. Exits silently without creating a duplicate if
	 * a node of the same name already exists.
	 * 
	 * Must be called after Navigator has been created, eg: in
	 * createGraphicViews or later.
	 * 
	 * @param dataFrameName
	 *            data frame name in R
	 */
	public void addDataFrameNode(String dataFrameName) {

		if (!dataFrameNodeMap.containsKey(dataFrameName)) {

			String rcmd =
					"str(" + dataFrameName
							+ ", max.level=1, give.attr=FALSE)";
			PanelViewProvider provider =
					new PanelViewRTextCommand(scape.getScapeRInterface(),
							dataFrameName, rcmd);

			addNodeToOnDemandFolder(new PanelViewNode(provider),
					"Dataframes", null);

			dataFrameNodeMap.put(dataFrameName, null);

		}

	}

	/**
	 * Add a panel view node under "Graphs". Creates "Graphs" node if it doesn't
	 * exist.
	 * 
	 * @param provider
	 *            provider of the panel view to create node for
	 * @param subFolderName
	 *            a navigator subfolder under "Graphs" in which to create node,
	 *            or {@code null} to create node directly under "Graphs"
	 * @return newly added node
	 */
	public PanelViewNode addGraphNode(PanelViewProvider provider,
			String subFolderName) {
		PanelViewNode newNode = new PanelViewNode(provider);
		addNodeToOnDemandFolder(newNode, "Graphs", subFolderName);
		return newNode;
	}

	/**
	 * Add a node for a {@link REXPDatasetProvider} under "Model Inputs".
	 * 
	 * Convenience method that can be called from R without having to first cast
	 * the provider.
	 * 
	 * @param provider
	 *            provider
	 * @param subFolderName
	 *            name of sub folder to add node under, or {@code null} to add
	 *            directly under "Model Inputs".
	 */
	public void addInputNode(REXPDatasetProvider provider,
			String subFolderName) {
		addTableNode(provider, "Model Inputs", subFolderName);
	}

	/**
	 * Add a dataset node under "User Tables". The node is automatically opened
	 * after it is added.
	 * 
	 * Takes a provider instead of a dataset directly, so the provider can serve
	 * up whatever it wants each time the node is opened.
	 * 
	 * Takes a {@link OutputDatasetNodeProvider} instead of a
	 * {@link PanelViewProvider} because a dataset is the most likely type of
	 * node to be produced and we can do the wrapping in a PanelView for the
	 * caller.
	 * 
	 * 
	 * @param provider
	 *            provider
	 * @param subFolderName
	 *            name of sub folder to add node under, or {@code null} to add
	 *            directly under "User Tables".
	 */
	public void addUserNode(OutputDatasetProvider provider,
			String subFolderName) {
		PanelViewNode newNode =
				addUserNode(new PanelViewDatasetProvider(provider),
						subFolderName);

		// expand tree and newly added node
		try {
			AscapeGUIUtil.getNavigator().setSelectionPath(
					new TreePath(newNode.getPath()));
		} catch (RuntimeException e) {
			removeNodeFromParent(newNode);
			throw e;
		}
	}

	/**
	 * Add a panel view node under "User Tables". Creates "User Tables" node if
	 * it doesn't exist.
	 * 
	 * @param provider
	 *            provider of the panel view to create node for
	 * @param subFolderName
	 *            name of sub folder to add node under, or {@code null} if to
	 *            add directly under "User Tables".
	 * @return newly added node
	 */
	public PanelViewNode addUserNode(PanelViewProvider provider,
			String subFolderName) {
		PanelViewNode newNode = new PanelViewNode(provider);
		addNodeToOnDemandFolder(newNode, "User Tables", null);
		return newNode;
	}

	/**
	 * Add a panel view node under "Output Tables".
	 * 
	 * @param provider
	 *            provider
	 * @param subFolderName
	 *            name of sub folder to add node under, or {@code null} to add
	 *            directly under "Output Tables".
	 * @return newly added node
	 */
	public PanelViewNode addOutputNode(PanelViewProvider provider,
			String subFolderName) {
		PanelViewNode newNode = new PanelViewNode(provider);
		outputTablesNode.addChildNode(newNode, subFolderName);
		return newNode;
	}

	/**
	 * Add a saveable panel view node under "Output Tables".
	 * 
	 * @param provider
	 *            provider
	 * @param subFolderName
	 *            name of sub folder to add node under, or {@code null} to add
	 *            directly under "Output Tables".
	 * @param saver
	 *            {@link Saveable} that saves the contents of
	 *            {@link PanelViewProvider} when required
	 * @return newly added node
	 */
	public PanelViewNode addOutputNode(PanelViewProvider provider,
			Saveable saver, String subFolderName) {
		SaveablePanelViewNode newNode =
				new SaveablePanelViewNode(provider, saver);
		outputTablesNode.addChildNode(newNode, subFolderName);
		return newNode;
	}

	/**
	 * Add a saveable node for an {@link OutputDatasetProvider} under
	 * "Output Tables".
	 * 
	 * @param provider
	 *            provider
	 * @param subFolderName
	 *            name of sub folder to add node under, or {@code null} to add
	 *            directly under "Output Tables".
	 */
	public void addOutputNode(OutputDatasetProvider provider,
			String subFolderName) {
		PanelViewDatasetProvider pvprovider =
				new PanelViewDatasetProvider(provider);
		addOutputNode(pvprovider, pvprovider, subFolderName);
	}

	/**
	 * Add a saveable node for a {@link REXPDatasetProvider} under
	 * "Output Tables".
	 * 
	 * Convenience method that can be called from R without having to first cast
	 * the provider.
	 * 
	 * @param provider
	 *            provider
	 * @param subFolderName
	 *            name of sub folder to add node under, or {@code null} to add
	 *            directly under "Output Tables".
	 */
	public void addOutputNode(REXPDatasetProvider provider,
			String subFolderName) {
		addOutputNode((OutputDatasetProvider) provider, subFolderName);
	}

	/**
	 * Add a saveable node for a {@link REXPDatasetProvider} under
	 * "Base Tables".
	 * 
	 * Convenience method that can be called from R.
	 * 
	 * @param provider
	 *            provider
	 * @param subFolderName
	 *            name of sub folder to add node under, or {@code null} to add
	 *            directly under "Output Tables".
	 */
	public void addBaseTableNode(REXPDatasetProvider provider,
			String subFolderName) {
		addTableNode(provider, "Base Tables", subFolderName);
	}

	/**
	 * Add a node to a folder and optionally a subfolder, creating that folder
	 * if it doesn't already exist.
	 * 
	 * @param newNode
	 *            new node to add
	 * @param folderName
	 *            name of folder to create node/subfolder under. created if it
	 *            doesn't already exist.
	 * @param subFolderName
	 *            name of sub folder to add node under, or {@code null} to add
	 *            directly under folderName.
	 */
	public final void addNodeToOnDemandFolder(MutableTreeNode newNode,
			String folderName, String subFolderName) {

		SubFolderNode folderNode = tableNodeMap.get(folderName);

		if (folderNode == null) {
			// create on demand
			folderNode = new SubFolderNode(folderName, scape, treeModel);
			treeModel.insertNodeInto(folderNode, this, this.getChildCount());
			tableNodeMap.put(folderName, folderNode);
		}

		folderNode.addChildNode(newNode, subFolderName);
	}

	/**
	 * Add a saveable node for a {@link REXPDatasetProvider} under the folder
	 * and subfolder specified. Creates the folder and/or subfolder if it
	 * doesn't already exist.
	 * 
	 * Convenience method that can be called from R.
	 * 
	 * @param provider
	 *            provider
	 * @param folderName
	 *            name of folder to create node/subfolder under
	 * @param subFolderName
	 *            name of sub folder to add node under, or {@code null} to add
	 *            directly under folderName.
	 */
	public void addTableNode(REXPDatasetProvider provider, String folderName,
			String subFolderName) {
		PanelViewDatasetProvider pvprovider =
				new PanelViewDatasetProvider(provider);

		SaveablePanelViewNode newNode =
				new SaveablePanelViewNode(pvprovider, pvprovider);

		addNodeToOnDemandFolder(newNode, folderName, subFolderName);
	}

	/**
	 * Remove node via the tree model (so as to generate events).
	 * 
	 * @param node
	 *            node to remove
	 */
	public void removeNodeFromParent(MutableTreeNode node) {
		treeModel.removeNodeFromParent(node);
	}
}