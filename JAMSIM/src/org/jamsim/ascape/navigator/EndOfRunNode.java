package org.jamsim.ascape.navigator;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Provides a pre-existing {@link DefaultMutableTreeNode} at the end of a run.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class EndOfRunNode implements OutputNodeProvider {

	private final DefaultMutableTreeNode node;

	/**
	 * Construct output node provider that provides an existing node at the end
	 * of the run.
	 * 
	 * @param node
	 *            node
	 */
	public EndOfRunNode(DefaultMutableTreeNode node) {
		this.node = node;
	}

	@Override
	public DefaultMutableTreeNode getEndOfRunOutputNode() {
		return node;
	}

	@Override
	public String getName() {
		return node.toString();
	}

	@Override
	public DefaultMutableTreeNode getOutputNode(int run) {
		return null;
	}

}
