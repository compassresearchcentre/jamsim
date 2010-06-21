package org.jamsim.ascape.navigator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jamsim.ascape.output.OutputException;

/**
 * Provider of a simulation output node for a given run number and/or at the end
 * of the simulation.
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
	 * @throws OutputException
	 *             if problem creating output node
	 */
	DefaultMutableTreeNode getOutputNode(int run) throws OutputException;

	/**
	 * Output node provided at the end of all runs.
	 * 
	 * @return output node, or {@code null} if none.
	 * @throws OutputException
	 *             if problem creating output node
	 */
	DefaultMutableTreeNode getEndOfSimOutputNode() throws OutputException;

}
