package org.jamsim.ascape.navigator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.ascape.model.event.DefaultScapeListener;
import org.ascape.model.event.ScapeEvent;
import org.ascape.model.event.ScapeListener;
import org.jamsim.ascape.output.OutputException;

/**
 * A {@link ScapeListener} that displays Navigator output nodes after each
 * iteration and/or at the end of the simulation. Nodes displayed are provided
 * by a {@link OutputNodeProvider}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class OutputNode extends DefaultScapeListener {
	private static final long serialVersionUID = -5105471052036807288L;

	private final NodesByRunFolder outputTablesNode;

	private final OutputNodeProvider nodeProvider;

	private int runNumber = 0;

	private boolean scapeClosed = false;

	private final String nodeGroupName;

	/**
	 * Create an output node that appears under {@code outputTablesNode} after
	 * iteration and/or simulation end.
	 * 
	 * @param outputTablesNode
	 *            navigator output tables tree node
	 * @param nodeProvider
	 *            node provider
	 */
	public OutputNode(NodesByRunFolder outputTablesNode,
			OutputNodeProvider nodeProvider) {
		this(outputTablesNode, null, nodeProvider);
	}

	/**
	 * Create an output node that appears after iteration and/or simulation end.
	 * 
	 * @param outputTablesNode
	 *            navigator output tables tree node
	 * @param nodeGroupName
	 *            node group name or {@code null} if this node will appear under
	 *            outputTablesNode
	 * @param nodeProvider
	 *            node provider
	 */
	public OutputNode(NodesByRunFolder outputTablesNode,
			String nodeGroupName, OutputNodeProvider nodeProvider) {
		super(nodeProvider.toString());
		this.outputTablesNode = outputTablesNode;
		this.nodeGroupName = nodeGroupName;
		this.nodeProvider = nodeProvider;
	}

	@Override
	public void scapeStopped(ScapeEvent scapeEvent) {
		runNumber++;

		try {
			DefaultMutableTreeNode node =
					nodeProvider.getOutputNode(runNumber);
			if (node != null) {
				addOutputNode(node, runNumber);
			}
		} catch (OutputException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void scapeClosing(ScapeEvent scapeEvent) {

		// scapeClosing gets called twice when the scape closes
		// so we need a flag to make sure
		// it doesn't get called twice
		if (runNumber > 0 && !scapeClosed) {
			scapeClosed = true;
			createMultiRunNode();
		}

	}

	private void createMultiRunNode() {
		try {
			DefaultMutableTreeNode node =
					nodeProvider.getEndOfSimOutputNode();
			if (node != null) {
				addOutputNode(node, NodesByRunFolder.ALLRUNS);
			}
		} catch (OutputException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Add node to appropriate run sub folder.
	 * 
	 * @param node
	 *            node to add
	 * @param runNumber
	 *            the run number subfolder under which to create the child node.
	 *            If the run number is {@link #ALLRUNS}, then it is created
	 *            under the "All Runs" node.
	 */
	public void addOutputNode(DefaultMutableTreeNode node, int runNumber) {

		NodesByRunFolder parentNode = outputTablesNode;

		if (nodeGroupName != null) {
			// get group node at time of creation of child node
			// creates group node if it does not already exist
			parentNode = outputTablesNode.getChildGroupNode(nodeGroupName);
		}

		parentNode.addChildNode(runNumber, node);
	}

}