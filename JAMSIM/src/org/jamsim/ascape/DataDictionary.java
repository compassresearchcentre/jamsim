package org.jamsim.ascape;

import java.util.Map;

/**
 * Dictionary mapping variable names to their description.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class DataDictionary {

	private final Map<String, String> dict;

	/**
	 * Construct {@link DataDictionary}.
	 * 
	 * @param dict
	 *            dictionary
	 */
	public DataDictionary(Map<String, String> dict) {
		this.dict = dict;
	}

	/**
	 * Get variable description from name.
	 * 
	 * @param variable
	 *            variable name
	 * @return variable description
	 */
	public String getDescription(String variable) {
		if (!dict.containsKey(variable)) {
			throw new RuntimeException(variable + " not in data dictionary");
		}

		return dict.get(variable);
	}

	/**
	 * Get map of all variable names and descriptions.
	 * 
	 * @return map of variable descriptions keyed by variable name
	 */
	public Map<String, String> getMap() {
		return dict;
	}
}
