package org.jamsim.ascape;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.swing.table.TableModel;

/**
 * Defines data external to a scape that can be accessed by agents. Also defines
 * how base agents are loaded.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface ScapeData {

	/**
	 * Return a map of tables which will be displayed in the Navigator. These
	 * can be any data the simulation wants to display in tabulated form, that
	 * does not change after loading. The map key is the name used for the table
	 * in the Navigator.
	 * 
	 * @return list of JTables for display in user interface.
	 */
	Map<String, TableModel> getTableModels();

	/**
	 * Return a collection of agents from the base file.
	 * 
	 * @param basefile
	 *            base file to load agents from
	 * @return collection of agents
	 * @throws IOException
	 *             if problem loading from base file
	 */
	Collection<?> getBaseScapeAgents(File basefile) throws IOException;

	// get file loader?

	// get output??

}
