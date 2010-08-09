package org.jamsim.ascape.navigator;

import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.ascape.runtime.swing.navigator.PanelViewNode;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.runtime.swing.navigator.PanelViewTable;
import org.ascape.runtime.swing.navigator.ScapeNode;
import org.ascape.runtime.swing.navigator.TreeBuilder;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.output.OutputDatasetProvider;
import org.jamsim.ascape.output.ROutput;
import org.jamsim.ascape.r.PanelViewDataset;
import org.jamsim.ascape.r.PanelViewRCommand;
import org.jamsim.ascape.ui.PanelViewParameterSet;
import org.jamsim.io.ParameterSet;
import org.omancode.swing.DoubleCellRenderer;

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
	 * Data frame node.
	 */
	private DefaultMutableTreeNode dfNode;

	/**
	 * Graphs node.
	 */
	private DefaultMutableTreeNode graphNode;

	/**
	 * User tables node.
	 */
	private SubFolderNode userNode;

	/**
	 * Parameter sets node.
	 */
	private DefaultMutableTreeNode psNode;

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
		addParameterSetNodes(scape, treeModel);

		// create the Output Tables node
		outputTablesNode =
				new SubFolderNode("Output Tables", scape, treeModel);
		treeModel
				.insertNodeInto(outputTablesNode, this, this.getChildCount());
	}

	private void addDatasetNodes(MicroSimScape<?> scape,
			DefaultTreeModel treeModel) {
		addNodeWithChildrenTables(treeModel, "Datasets", scape.getScapeData()
				.getInputDatasets(), new DoubleCellRenderer(10));
	}

	private void addNodeWithChildrenTables(DefaultTreeModel treeModel,
			String nodeName, Map<String, TableModel> childrenTables,
			DoubleCellRenderer dblRenderer) {

		// create parent node
		DefaultMutableTreeNode parentNode =
				new DefaultMutableTreeNode(nodeName);

		// get all the tables from childrenTables
		// and add them to the navigator as a Panel View Node
		for (Map.Entry<String, TableModel> entry : childrenTables.entrySet()) {

			TableModel tmodel = entry.getValue();
			JTable table = new JTable(tmodel); // NOPMD
			table.setName(entry.getKey());
			table.setDefaultRenderer(Double.class, dblRenderer);

			// add PanelViewNode to the tree
			PanelViewProvider provider = new PanelViewTable(table);
			PanelViewNode newNode = new PanelViewNode(provider);
			parentNode.add(newNode); // NOPMD
		}

		// add parentNode via the Tree Model
		treeModel.insertNodeInto(parentNode, this, this.getChildCount());
	}

	private void addParameterSetNodes(MicroSimScape<?> scape,
			DefaultTreeModel treeModel) {

		Map<String, ParameterSet> psets =
				scape.getScapeData().getParameterSets();

		if (psets != null) {
			for (Map.Entry<String, ParameterSet> entry : psets.entrySet()) {
				ParameterSet pset = entry.getValue();
				addParameterSetNode(pset);
			}
		}
	}

	/**
	 * Add a parameter set node under the "Parameter sets" folder. Creates
	 * "Parameter sets" node if it doesn't exist.
	 * 
	 * @param pset
	 *            parameter set
	 */
	public final void addParameterSetNode(ParameterSet pset) {
		if (psNode == null) {
			// create parameter sets parent folder node
			psNode = new DefaultMutableTreeNode("Parameter sets");
			treeModel.insertNodeInto(psNode, this, this.getChildCount());
		}

		// add PanelViewNode to the tree
		PanelViewProvider provider = new PanelViewParameterSet(pset);
		PanelViewNode newNode = new PanelViewNode(provider);
		treeModel.insertNodeInto(newNode, psNode, psNode.getChildCount());
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
				new ROutput(name, name, scape.getScapeRInterface(), rcmd);

		PanelViewDataset provider = new PanelViewDataset(basefileDS);

		treeModel.insertNodeInto(new PanelViewNode(provider), this, this
				.getChildCount());
	}

	/**
	 * Add a data frame node under the "Dataframes" folder. Creates "Dataframes"
	 * node if it doesn't exist.
	 * 
	 * @param dataFrameName
	 *            data frame name
	 */
	public void addDataFrameNode(String dataFrameName) {
		if (dfNode == null) {
			// create on demand
			dfNode = new DefaultMutableTreeNode("Dataframes");
			treeModel.insertNodeInto(dfNode, this, this.getChildCount());
		}

		String rcmd = "str(" + dataFrameName + ", max.level=1)";
		PanelViewProvider provider =
				new PanelViewRCommand(scape.getScapeRInterface(),
						dataFrameName, rcmd);
		treeModel.insertNodeInto(new PanelViewNode(provider), dfNode, dfNode
				.getChildCount());
	}

	/**
	 * Add a panel view node under "Graphs". Creates "Graphs" node if it doesn't
	 * exist.
	 * 
	 * @param provider
	 *            provider of the panel view to create node for
	 */
	public void addGraphNode(PanelViewProvider provider) {
		if (graphNode == null) {
			// create on demand
			graphNode = new DefaultMutableTreeNode("Graphs");
			treeModel.insertNodeInto(graphNode, this, this.getChildCount());
		}

		treeModel.insertNodeInto(new PanelViewNode(provider), graphNode,
				graphNode.getChildCount());

	}

	/**
	 * Add a panel view node under "User Tables". Creates "User Tables" node if
	 * it doesn't exist.
	 * 
	 * @param provider
	 *            provider of the panel view to create node for
	 * @param groupName
	 *            of group sub folder to add node under, or {@code null} if to
	 *            add directly under "User Tables".
	 * @return newly added node
	 */
	public PanelViewNode addUserNode(PanelViewProvider provider,
			String groupName) {
		if (userNode == null) {
			// create on demand
			userNode = new SubFolderNode("User Tables", scape, treeModel);
			treeModel.insertNodeInto(userNode, this, this.getChildCount());
		}

		PanelViewNode newNode = new PanelViewNode(provider);
		userNode.addChildNode(newNode, groupName);
		return newNode;
	}

	public void removeNodeFromParent(MutableTreeNode node) {
		treeModel.removeNodeFromParent(node);
	}
}