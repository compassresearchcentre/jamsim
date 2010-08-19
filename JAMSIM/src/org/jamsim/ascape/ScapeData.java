package org.jamsim.ascape;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.swing.table.TableModel;

import org.ascape.model.Agent;
import org.jamsim.io.ParameterSet;
import org.jamsim.math.RNG;

/**
 * Defines data external to a scape that can be accessed by agents. Also defines
 * how base agents are loaded.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface ScapeData {

	/**
	 * Return a map of tables which will be displayed in the Navigator under the
	 * "Datasets" node. These can be any data the simulation wants to display in
	 * tabulated form, that does not change after loading. The map key is the
	 * name used for the table in the Navigator.
	 * 
	 * @return map of tables for display.
	 */
	Map<String, TableModel> getInputDatasets();

	/**
	 * Return a map of tables which will be displayed in the Navigator under the
	 * "Parameter Set" node. These can be any data the simulation wants to make
	 * available as parameters for manipulation by the user. The map key is the
	 * name used for the table in the Navigator.
	 * 
	 * @return map of tables for display.
	 */
	Map<String, ParameterSet> getParameterSets();


	/**
	 * Get random number generator.
	 * 
	 * @return random number generator.
	 */
	RNG getRNG();

	/**
	 * Return a collection of agents from the base file.
	 * 
	 * @param basefile
	 *            base file to load agents from
	 * @return collection of agents
	 * @throws IOException
	 *             if problem loading from base file
	 */
	Collection<? extends Agent> loadAgents(File basefile) throws IOException;
	
	/**
	 * Get the data dictionary.
	 * 
	 * @return data dictionary
	 */
	DataDictionary getDataDictionary();

}
