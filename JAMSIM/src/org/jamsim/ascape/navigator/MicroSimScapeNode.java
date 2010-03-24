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

		// create the Output Tables node
		outputTablesNode =
				new NodesByRunFolder("Output Tables", scape, treeModel);
		treeModel
				.insertNodeInto(outputTablesNode, this, this.getChildCount());
	}

	private void addDatasetNodes(MicroSimScape<?> scape,
			DefaultTreeModel treeModel) {

		// create Dataset node
		DefaultMutableTreeNode datasetsNode =
				new DefaultMutableTreeNode("Datasets");

		TableCellRenderer dblRenderer = new DoubleCellRenderer();

		// get all the tables from the scape external data
		// and add them to the navigator as a Panel View Node
		for (Map.Entry<String, TableModel> entry : scape.getScapeData()
				.getInputDatasets().entrySet()) {

			TableModel tmodel = entry.getValue();
			JTable table = new JTable(tmodel); // NOPMD
			table.setName(entry.getKey());
			table.setDefaultRenderer(Double.class, dblRenderer);

			// add PanelViewNode to the tree
			PanelViewProvider provider = new PanelViewTable(table);
			PanelViewNode newNode = new PanelViewNode(provider);
			datasetsNode.add(newNode); // NOPMD
		}

		// add datasetsNode via the Tree Model
		treeModel.insertNodeInto(datasetsNode, this, this.getChildCount());
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
		treeModel.insertNodeInto(new PanelViewNode(provider), dfNode,
				dfNode.getChildCount());
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

		treeModel.insertNodeInto(new PanelViewNode(provider),
				graphNode, graphNode.getChildCount());

	}
}