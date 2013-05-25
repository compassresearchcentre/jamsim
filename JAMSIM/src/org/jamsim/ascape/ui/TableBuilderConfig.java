package org.jamsim.ascape.ui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
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

	private final ListMultimap<String, String> variablesBySummaryMeasure;

	private final ListMultimap<String, String> subgroupsByVariable;

	/**
	 * Load from JSON file.
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

	public ListMultimap<String, String> extractVariablesbySummaryMeasure(
			Map<String, Map<String, List<String>>> variablesData) {
		ListMultimap<String, String> variablesBySummaryMeasure = ArrayListMultimap.create();
		
		for (Map.Entry<String, Map<String, List<String>>> variable: variablesData.entrySet()) {
			String variableName = variable.getKey();
			List<String> measures = variable.getValue().get("measures");
			
			for (String measure: measures) {
				variablesBySummaryMeasure.put(measure, variableName);
			}
		}

		return variablesBySummaryMeasure;
	}

	public ListMultimap<String, String> extractSubgroupsByVariable(Map<String, Map<String, List<String>>> variablesData) {
		ListMultimap<String, String> subgroupsByVariable = ArrayListMultimap.create();

		for (Map.Entry<String, Map<String, List<String>>> variable: variablesData.entrySet()) {
			String variableName = variable.getKey();
			List<String> subgroups = variable.getValue().get("subgroups");
			subgroupsByVariable.putAll(variableName, subgroups);
		}
		
		return subgroupsByVariable;
	}
	
	public List<String> getVariablesForFrequencies() {
		return variablesBySummaryMeasure.get("frequencies");
	}

	public List<String> getVariablesForMeans() {
		return variablesBySummaryMeasure.get("means");
	}

	
	public List<String> getVariablesForQuintiles() {
		return variablesBySummaryMeasure.get("quintiles");
	}
	
	public ListMultimap<String, String> getSubgroupsByVariable() {
		return subgroupsByVariable;
	}

}
