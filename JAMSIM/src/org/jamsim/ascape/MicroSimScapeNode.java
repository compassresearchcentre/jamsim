package org.jamsim.ascape;

import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.ascape.runtime.swing.navigator.PanelViewNode;
import org.ascape.runtime.swing.navigator.PanelViewNodes;
import org.ascape.runtime.swing.navigator.ScapeNode;
import org.ascape.runtime.swing.navigator.TreeBuilder;
import org.jamsim.swing.DoubleCellRenderer;

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

	private final PanelViewNodes outputTablesNode;

	/**
	 * Get output tables node. Exposed so scapes can add output tables during /
	 * after a simulation run.
	 * 
	 * @return output tables node
	 */
	public PanelViewNodes getOutputTablesNode() {
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

		DefaultTreeModel treeModel = treeBuilder.getTreeModel();

		addDatasetNodes(scape, treeModel);

		// create the Output Tables node
		outputTablesNode = new PanelViewNodes("Output Tables", scape, treeModel);
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
				.getTableModels().entrySet()) {

			TableModel tmodel = entry.getValue();
			JTable table = new JTable(tmodel); // NOPMD
			table.setName(entry.getKey());
			table.setDefaultRenderer(Double.class, dblRenderer);

			// add PanelViewNode to the tree
			datasetsNode.add(new PanelViewNode(scape, table)); // NOPMD
		}

		// add datasetsNode via the Tree Model
		treeModel.insertNodeInto(datasetsNode, this, this.getChildCount());
	}

}