package org.jamsim.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jamsim.ascape.ui.RExpression;
import org.jamsim.ascape.ui.SubgroupRExpressionProvider;

public class UserInterfaceCSVReader {
	
	private Map<String, RExpression> subgroupsToOptions;
	
	private Map<String, Map<String, String>> tableBuilderData;
	private Map<String, String> meansAndQuintilesToVariables;
	private Map<String, String> frequenciesToVariables;
	private Map<String, String> subgroupsToVariables;
	
	private BufferedReader CSVFile;
	private String fileName;
	
	public UserInterfaceCSVReader(Map<String, RExpression> subgroupsToOptions){
		this.subgroupsToOptions = subgroupsToOptions;
		subgroupsToOptions.put("None", new SubgroupRExpressionProvider("", new String[]{}, new String[]{}));
	}
	
	public UserInterfaceCSVReader(Map<String, Map<String, String>> tableBuilderData,
								  Map<String, String> meansAndQuintilesToVariables,
								  Map<String, String> frequenciesToVariables,
								  Map<String, String> subgroupsToVariables){
		
		this.tableBuilderData = tableBuilderData;
		this.meansAndQuintilesToVariables = meansAndQuintilesToVariables;
		this.frequenciesToVariables = frequenciesToVariables;
		this.subgroupsToVariables = subgroupsToVariables;
		subgroupsToVariables.put("None", "");
	}
	
	public Map<String, RExpression> readSubgroupsToOptionsCSVFile(String file){
		
		this.fileName = file;
		
		try{
			CSVFile = new BufferedReader(new FileReader(file));	

			try{
				String dataRow = CSVFile.readLine();
				dataRow = CSVFile.readLine();
		
				while(dataRow != null){
					
					String[] dataArray = dataRow.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");  //ignore commas within quote marks
					
					if(dataArray.length > 4 && dataArray.length < 7){
						
						String varname = dataArray[0];
						String description = dataArray[1];
						String descriptiondetails = dataArray[3];
						description = description + " " + descriptiondetails;
						String[] options = dataArray[4].split("/");
						String[] optioncodes = dataArray[5].split("/");
						
						subgroupsToOptions.put(description, new SubgroupRExpressionProvider(varname, options, optioncodes));
					}
					dataRow = CSVFile.readLine();
				}
			}catch(IOException e){System.out.println("IOException");} 	
		
		}catch(FileNotFoundException e){System.out.println("file not found");}	
		
		return subgroupsToOptions;
		
	}	
	
	public Map<String, Map<String, String>> readTableBuilderDataCSVFile(String fileName){
		this.fileName = fileName;
		
		try{
			CSVFile = new BufferedReader(new FileReader(fileName));	

			try{
				String dataRow = CSVFile.readLine();
				dataRow = CSVFile.readLine();
		
				while(dataRow != null){
					
					String[] dataArray = dataRow.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");  //ignore commas within quote marks
					
					if(dataArray.length > 3){
						
						String varname = dataArray[0];
						String description = dataArray[1];
						
						subgroupsToVariables.put(description, varname);
						
						if(dataArray.length > 6){
							varname = dataArray[0];
							description = dataArray[1];
							String summaryMeasure = dataArray[6];
							
							if(summaryMeasure.equals("frequencies")){
								frequenciesToVariables.put(description, varname);
							}else if(summaryMeasure.equals("means") | summaryMeasure.equals("quintiles")){
								meansAndQuintilesToVariables.put(description, varname);
							}
						}
					}
					dataRow = CSVFile.readLine();
				}
				
				tableBuilderData.put("Frequencies", frequenciesToVariables);
				tableBuilderData.put("Means", meansAndQuintilesToVariables);
				tableBuilderData.put("Quintiles", meansAndQuintilesToVariables);
				tableBuilderData.put("Subgroups", subgroupsToVariables);
				
			}catch(IOException e){System.out.println("IOException");} 	
		
		}catch(FileNotFoundException e){System.out.println("file not found");}	

		return tableBuilderData;		
	}	
}
