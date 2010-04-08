package org.jamsim.ascape.navigator;

import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.ascape.runtime.swing.navigator.NodesByRunFolder;
import org.ascape.runtime.swing.navigator.PanelViewNode;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.runtime.swing.navigator.ScapeNode;
import org.ascape.runtime.swing.navigator.PanelViewTable;
import org.ascape.runtime.swing.navigator.TreeBuilder;
import org.jamsim.ascape.MicroSimScape;
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

	private final NodesByRunFolder outputTablesNode;
	private final DefaultTreeModel treeModel;
	private DefaultMutableTreeNode dfNode;
	private DefaultMutableTreeNode graphNode;
	private final MicroSimScape<?> scape;

	/**
	 * Get output tables node. Exposed so scapes can add output tables during /
	 * after a simulation run.
	 * 
	 * @return output tables node
	 */
	public NodesByRunFolder getOutputTablesNode() {
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
				new NodesByRunFolder("Output Tables", scape, treeModel);
		treeModel
				.insertNodeInto(outputTablesNode, this, this.getChildCount());
	}

	private void addDatasetNodes(MicroSimScape<?> scape,
			DefaultTreeModel treeModel) {
		addNodeWithChildrenTables(treeModel, "Datasets", scape.getScapeData()
				.getInputDatasets());
	}

	private void addParameterSetNodes(MicroSimScape<?> scape,
			DefaultTreeModel treeModel) {

		Map<String, ParameterSet> psets =
				scape.getScapeData().getParameterSets();

		if (psets != null) {
		
			// create parent node
			DefaultMutableTreeNode parentNode =
					new DefaultMutableTreeNode("Parameter sets");
	
			// get all the tables from childrenTables
			// and add them to the navigator as a Panel View Node
			for (Map.Entry<String, ParameterSet> entry : psets.entrySet()) {
	
				// add PanelViewNode to the tree
				ParameterSet pset = entry.getValue();
				PanelViewProvider provider = new PanelViewParameterSet(pset);
				PanelViewNode newNode = new PanelViewNode(provider);
				parentNode.add(newNode); // NOPMD
			}
	
			// add parentNode via the Tree Model
			treeModel.insertNodeInto(parentNode, this, this.getChildCount());
			
		}
	}

	private void addNodeWithChildrenTables(DefaultTreeModel treeModel,
			String nodeName, Map<String, TableModel> childrenTables) {

		// create parent node
		DefaultMutableTreeNode parentNode =
				new DefaultMutableTreeNode(nodeName);

		TableCellRenderer dblRenderer = new DoubleCellRenderer();

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
}