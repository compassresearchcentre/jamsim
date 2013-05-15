package org.jamsim.ascape;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Dictionary mapping variable names to their description.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class DataDictionary {

	private final Map<String, String> desc; // desc from varname
	private final Map<String, String> varname;

	/**
	 * Construct {@link DataDictionary}.
	 * 
	 * @param dict
	 *            dictionary
	 */
	public DataDictionary(Map<String, String> dict) {
		this.desc = dict;

		this.varname = new HashMap<String, String>(dict.size());

		for (Entry<String, String> desc : dict.entrySet()) {
			varname.put(desc.getValue(), desc.getKey());
		}
	}

	/**
	 * Get variable description from name.
	 * 
	 * @param variable
	 *            variable name
	 * @return variable description
	 */
	public String getDescription(String variable) {
		if (!desc.containsKey(variable)) {
			throw new RuntimeException(variable + " not in data dictionary");
		}

		return desc.get(variable);
	}

	/**
	 * Get varname from description.
	 * 
	 * @param description description
	 * @return varname
	 */
	public String getVarname(String description) {
		if (!varname.containsKey(description)) {
			throw new RuntimeException("No varname with description "
					+ description);
		}

		return varname.get(description);
	}

	/**
	 * Get map of all variable names and descriptions.
	 * 
	 * @return map of variable descriptions keyed by variable name
	 */
	public Map<String, String> getMap() {
		return desc;
	}
}
