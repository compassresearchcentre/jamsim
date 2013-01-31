package org.jamsim.ascape.ui;

import java.util.Map;

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
