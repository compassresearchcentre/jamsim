package org.jamsim.ascape.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


public class SubgroupRExpressionProvider extends RExpression{
	
	public SubgroupRExpressionProvider(String subgroupRExpression, String[] optionDescriptions, String[] optionRExpressions){
		
		super(subgroupRExpression, new LinkedHashMap<String, RExpression>());
		
		setupOptionMap(optionDescriptions, optionRExpressions);
	}
	
	private void setupOptionMap(String[] descriptions, String[] rExpressions){
		for(int i = 0; i < descriptions.length; i++){
			subRExpressions.put(descriptions[i], new RExpression(rExpressions[i], null));
		}
	}
}
