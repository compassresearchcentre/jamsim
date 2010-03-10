package org.jamsim.ascape.navigator;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Provider of a simulation output node for a given run number and/or at the end
 * of the run.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface OutputNodeProvider {

	/**
	 * Output node name.
	 * 
	 * @return name
	 */
	String getName();

	/**
	 * Output node for the given run number.
	 * 
	 * @param run
	 *            simulation run number
	 * @return output node, or {@code null} if none for this run.
	 */
	DefaultMutableTreeNode getOutputNode(int run);

	/**
	 * Output node provided at the end of the run.
	 * 
	 * @return output node, or {@code null} if none.
	 */
	DefaultMutableTreeNode getEndOfRunOutputNode();

}
