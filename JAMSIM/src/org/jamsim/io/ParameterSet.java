package org.jamsim.io;

import java.util.prefs.Preferences;

import javax.swing.table.TableModel;

import org.jamsim.shared.InvalidDataException;

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
	 * {@link TableModel}). Validates the values then notifies all
	 * {@link java.util.Observer}s that there has been a change.
	 * 
	 * External methods should call this after making any changes to the
	 * underlying {@link TableModel}.
	 * 
	 * @throws InvalidDataException
	 *             if the underlying {@link TableModel} values are invalid
	 */
	void validateAndNotify() throws InvalidDataException;

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
	
	/**
	 * Save the state to the preferences.
	 * 
	 * @param prefs preferences.
	 */
	void saveState(Preferences prefs);

	/**
	 * Load the state from preferences.
	 * 
	 * @param prefs preferences.
	 */
	void loadState(Preferences prefs);
	
}
