package org.jamsim.ascape.navigator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.ascape.model.event.DefaultScapeListener;
import org.ascape.model.event.ScapeEvent;
import org.ascape.model.event.ScapeListener;
import org.ascape.runtime.swing.navigator.NodesByRunFolder;

/**
 * A {@link ScapeListener} that displays Navigator output nodes after each
 * iteration and/or at the end of the run. Nodes displayed are provided by a
 * {@link OutputNodeProvider}.
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

	/**
	 * Master constructor.
	 * 
	 * @param outputTablesNode
	 *            navigator output tables tree node
	 * @param nodeProvider
	 *            node provider
	 */
	public OutputNode(NodesByRunFolder outputTablesNode,
			OutputNodeProvider nodeProvider) {
		super(nodeProvider.toString());
		this.outputTablesNode = outputTablesNode;
		this.nodeProvider = nodeProvider;
	}

	@Override
	public void scapeStopped(ScapeEvent scapeEvent) {
		runNumber++;

		DefaultMutableTreeNode node = nodeProvider.getOutputNode(runNumber);
		if (node != null) {
			addOutputNode(node);
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
		DefaultMutableTreeNode node = nodeProvider.getEndOfRunOutputNode();
		if (node != null) {
			addOutputNode(node);
		}
	}

	/**
	 * Add node to appropriate run sub folder.
	 * 
	 * @param node
	 *            node to add
	 */
	public void addOutputNode(DefaultMutableTreeNode node) {
		outputTablesNode.addChildNode(scapeClosed ? NodesByRunFolder.ALLRUNS
				: runNumber, node);
	}

}