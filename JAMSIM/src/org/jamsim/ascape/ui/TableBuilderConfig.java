package org.jamsim.ascape.ui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Load table builder config from a JSON file.
 * 
 * @author mvon007
 * 
 */
public class TableBuilderConfig {

	/**
	 * Variables to measures and subgropus.
	 * 
	 * eg: { "householdsize": { "measures": ["frequencies", "means",
	 * "quantiles"], "subgroups": ["z1singleLvl1"] }, "kids": { "measures":
	 * ["frequencies", "means", "quantiles"], "subgroups": ["z1singleLvl1"] } }
	 */
	private final Map<String, Map<String, List<String>>> variablesData;

	private final Map<String, List<String>> variablesBySummaryMeasure;

	private final Map<String, List<String>> subgroupsByVariable;

	/**
	 * Load from json file.
	 * 
	 * @param jsonFileName
	 *            json file name
	 * @throws FileNotFoundException
	 *             no file
	 */
	public TableBuilderConfig(String jsonFileName) throws FileNotFoundException {
		Gson gson = new Gson();
		Reader reader = new BufferedReader(new FileReader(jsonFileName));

		Type mapType = new TypeToken<Map<String, Map<String, List<String>>>>() {
			// anonymous class
		}.getType();
		variablesData = gson.fromJson(reader, mapType);

		variablesBySummaryMeasure = extractVariablesbySummaryMeasure(variablesData);
		subgroupsByVariable = extractSubgroupsByVariable(variablesData);

	}

	public Map<String, List<String>> extractVariablesbySummaryMeasure(
			Map<String, Map<String, List<String>>> variablesData) {
		Map<String, List<String>> variablesBySummaryMeasure = new HashMap<String, List<String>>();
		
		return variablesBySummaryMeasure;
	}

	public Map<String, List<String>> extractSubgroupsByVariable(Map<String, Map<String, List<String>>> variablesData) {
		Map<String, List<String>> subgroupsByVariable = new HashMap<String, List<String>>();
		
		return subgroupsByVariable;
	}
	
	public List<String>> getVariablesForFrequencies() {
		return variablesBySummaryMeasure;
	}

	public List<String>> getVariablesForMeans() {
		return variablesBySummaryMeasure;
	}

	
	public List<String>> getVariablesForQuintiles() {
		return variablesBySummaryMeasure;
	}

	
	public Map<String, List<String>> getSubgroupsByVariable() {
		return subgroupsByVariable;
	}


}
