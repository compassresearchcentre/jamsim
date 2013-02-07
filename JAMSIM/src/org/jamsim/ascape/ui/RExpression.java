package org.jamsim.ascape.ui;

import java.util.Map;

/**
 * Associates an R expression String of a variable with a Map<String, RExpression> that maps 
 * option Strings appropriate to that variable (e.g. for a categorical variable
 * option Strings might be "Category 1", "Category 2", etc.) To further RExpressions.
 *  
 * @author bmac055
 *
 */
public class RExpression {
	
	protected String rExpression;
	protected Map<String, RExpression> subRExpressions;
	
	public RExpression(String rExpression, Map<String, RExpression> subRExpressions){
		this.rExpression = rExpression;
		this.subRExpressions = subRExpressions;
	}
	
	public String getRExpression(){
		return rExpression;
	}
	
	public Map<String, RExpression> getSubExpressions(){
		return subRExpressions;
	}
}
