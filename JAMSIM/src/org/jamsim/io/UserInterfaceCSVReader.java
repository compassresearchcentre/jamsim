package org.jamsim.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jamsim.ascape.ui.RExpression;
import org.jamsim.ascape.ui.SubgroupRExpressionProvider;

/**
 * A CSV reader that reads the data into the Maps for the Scenario Weightings (
 * {@link NewPanelView}) and the Table Builder ({@link TableBuilder})
 * interfaces.
 * 
 * @author bmac055
 * 
 */
public class UserInterfaceCSVReader {

	private Map<String, RExpression> subgroupsToOptions;

	private Map<String, Map<String, String>> tableBuilderData;
	private Map<String, String> meansToVariables;
	private Map<String, String> quintilesToVariables;
	private Map<String, String> frequenciesToVariables;
	private Map<String, String> subgroupsToExpressions;

	private BufferedReader CSVFile;
	private String fileName;

	/**
	 * Creates a {@link UserInterfaceCSVReader} to read into the Maps used for
	 * the Scenario Weightings ({@link NewPanelView}) Interface.
	 * 
	 * @param subgroupsToOptions
	 *            Maps subgroup variable name Strings to corresponding
	 *            {@link RExpression}s that contain the R expression string
	 *            corresponding to the subgroup variable as well as the options
	 *            corresponding to that variable and the R expressions
	 *            corresponding to those options
	 */
	public UserInterfaceCSVReader(Map<String, RExpression> subgroupsToOptions) {
		this.subgroupsToOptions = subgroupsToOptions;
		subgroupsToOptions.put("None", new SubgroupRExpressionProvider("",
				new String[] {}, new String[] {}));
	}

	/**
	 * Creates a {@link UserInterfaceCSVReader} to read into the Maps used for
	 * the Table Builder ({@link TableBuiler}) Interface.
	 * 
	 * @param tableBuilderData
	 *            A Map of Strings to maps. Contains maps for summary measure
	 *            Strings (frequencies, means, quintiles) to appropriate
	 *            variable Strings (continuous, categorical etc.) and their
	 *            corresponding R expression Strings, as well as a map of
	 *            subgroup variable Strings and their corresponding subgroup
	 *            expression Strings.
	 * @param meansAndQuintilesToVariables
	 *            Map of mean and quintile Strings to appropriate variable
	 *            Strings and their corresponding R expression Strings
	 * @param frequenciesToVariables
	 *            Map of frequency Strings to appropriate variable Strings and
	 *            their corresponding R expression Strings
	 * @param subgroupsToExpressions
	 *            Map of subgroup variable Strings and their corresponding
	 *            subgroup expression Strings
	 */
	public UserInterfaceCSVReader(
			Map<String, Map<String, String>> tableBuilderData,
			Map<String, String> meansToVariables,
			Map<String, String> quintilesToVariables,
			Map<String, String> frequenciesToVariables,
			Map<String, String> subgroupsToExpressions) {

		this.tableBuilderData = tableBuilderData;
		this.meansToVariables = meansToVariables;
		this.quintilesToVariables = quintilesToVariables;
		this.frequenciesToVariables = frequenciesToVariables;
		this.subgroupsToExpressions = subgroupsToExpressions;
		subgroupsToExpressions.put("None", "");
	}

	/**
	 * Reads from the given CSV file into the map used in the Scenario
	 * Weightings ({@link NewPanelView}) Interface
	 * 
	 * @param file
	 *            The CSV file from which to read
	 * @return Map of subgroup variable Strings to {@link RExpression}s that
	 *         contain the R expression string corresponding to the subgroup
	 *         variable as well as the options corresponding to that variable
	 *         and the R expressions corresponding to those options
	 */
	public Map<String, RExpression> readSubgroupsToOptionsCSVFile(String file) {

		this.fileName = file;

		try {
			CSVFile = new BufferedReader(new FileReader(file));

			try {
				String dataRow = CSVFile.readLine();
				dataRow = CSVFile.readLine();

				while (dataRow != null) {

					String[] dataArray = dataRow
							.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); // ignore
																		// commas
																		// within
																		// quote
																		// marks

					if (dataArray.length > 4) {

						String varname = dataArray[0];
						String description = dataArray[1];
						String descriptiondetails = dataArray[3];
						description = description + " " + descriptiondetails;
						String[] options = dataArray[4].split("/");
						String[] optioncodes = dataArray[5].split("/");

						if (options.length > 0 && !options[0].equals("")) {
							subgroupsToOptions.put(description,
									new SubgroupRExpressionProvider(varname,
											options, optioncodes));
						}
					}
					dataRow = CSVFile.readLine();
				}
			} catch (IOException e) {
				System.out.println("IOException");
			}

		} catch (FileNotFoundException e) {
			System.out.println("file not found");
		}

		return subgroupsToOptions;

	}

	/**
	 * Reads from the given CSV file into the maps used in the
	 * {@link TableBuilder} Interface.
	 * 
	 * @param fileName
	 *            The CSV file from which to read.
	 * @return A Map of Strings to maps. Contains maps for summary measure
	 *         Strings (frequencies, means, quintiles) to appropriate variable
	 *         Strings (continuous, categorical etc.) and their corresponding R
	 *         expression Strings, as well as a map of subgroup variable Strings
	 *         and their corresponding subgroup expression Strings.
	 */
	public Map<String, Map<String, String>> readTableBuilderDataCSVFile(
			String fileName) {
		this.fileName = fileName;

		try {
			CSVFile = new BufferedReader(new FileReader(fileName));

			try {
				String dataRow = CSVFile.readLine();
				dataRow = CSVFile.readLine();

				while (dataRow != null) {

					String[] dataArray = dataRow
							.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); // ignore
																		// commas
																		// within
																		// quote
																		// marks

					if (dataArray.length > 3) {

						String varname = dataArray[0];
						String description = dataArray[1];

						subgroupsToExpressions.put(description, varname);

						if (dataArray.length > 6) {
							varname = dataArray[0];
							description = dataArray[1];
							String[] summaryMeasures = dataArray[6].split("/");

							for (int i = 0; i < summaryMeasures.length; i++) {
								if (summaryMeasures[i].equals("frequencies")) {
									frequenciesToVariables.put(description,
											varname);
								} else if (summaryMeasures[i].equals("means")) {
									meansToVariables.put(description, varname);
								} else if (summaryMeasures[i]
										.equals("quintiles")) {
									quintilesToVariables.put(description,
											varname);
								}
							}

						}
					}
					dataRow = CSVFile.readLine();
				}

				tableBuilderData.put("Frequencies", frequenciesToVariables);
				tableBuilderData.put("Means", meansToVariables);
				tableBuilderData.put("Quintiles", quintilesToVariables);
				tableBuilderData.put("Subgroups", subgroupsToExpressions);

			} catch (IOException e) {
				System.out.println("IOException");
			}

		} catch (FileNotFoundException e) {
			System.out.println("file not found");
		}

		return tableBuilderData;
	}
}
