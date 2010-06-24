package org.jamsim.io;

import javax.swing.table.TableModel;

/**
 * A set of parameters. The parameters are represented by a {@link TableModel}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface ParameterSet {

	/**
	 * Reset the parameters to their default values.
	 */
	void resetDefaults();

	/**
	 * Process a change to the parameter set (ie: the underlying
	 * {@link TableModel}). External methods should call this after making any
	 * changes to the underlying {@link TableModel}.
	 */
	void update();

	/**
	 * Name of this {@link ParameterSet}.
	 * 
	 * @return name
	 */
	String getName();

	/**
	 * {@link TableModel} of parameters represented by the {@link ParameterSet}.
	 * 
	 * @return table model
	 */
	TableModel getTableModel();
}
