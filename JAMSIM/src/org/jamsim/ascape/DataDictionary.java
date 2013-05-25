package org.jamsim.ascape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
	 * @param variableName
	 *            variable name
	 * @return variable description
	 */
	public String getDescription(String variableName) {
		if (!desc.containsKey(variableName)) {
			throw new RuntimeException(variableName + " not in data dictionary");
		}

		return desc.get(variableName);
	}
	
	public List<String> getDescriptions(Collection<String> variableNames) {
		List<String> descriptions = new ArrayList<String>(variableNames.size());
				
		for (String variableName: variableNames) {
			descriptions.add(getDescription(variableName));
		}
		
		return descriptions;
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
