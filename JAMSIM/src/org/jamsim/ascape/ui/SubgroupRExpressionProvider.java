package org.jamsim.ascape.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides an RExpression for the subgroups and options combo boxes in NewPanelView.
 * @author bmac055
 *
 */
public class SubgroupRExpressionProvider extends RExpression{
	
	/**
	 * Calls the RExpression constructor to set up the initial RExpression containing an R expression String corresponding
	 * to a particular subgroup. Uses the String[]s to set up the sub R expression Maps that map the required options for 
	 * the subgroup to further RExpressions, each of which has an option R expression String and leaves the 
	 * Map<String, RExpression> null.
	 * 
	 * @param subgroupRExpression
	 * @param optionDescriptions
	 * @param optionRExpressions
	 */
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
