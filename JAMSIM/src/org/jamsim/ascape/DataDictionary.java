package org.jamsim.ascape;

import java.util.Map;

/**
 * Dictionary mapping variable names to their description.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface DataDictionary {

	/**
	 * Get variable description from name.
	 * 
	 * @param varName
	 *            variable name
	 * @return variable description
	 */
	String getDescription(String varName);

	/**
	 * Get map of all variable names and descriptions.
	 * 
	 * @return map of variable descriptions keyed by variable name
	 */
	Map<String, String> getMap();

}