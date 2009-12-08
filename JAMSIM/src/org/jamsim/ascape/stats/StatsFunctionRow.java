package org.jamsim.ascape.stats;

import org.apache.commons.lang.mutable.MutableDouble;


public class StatsFunctionRow<T> {

	private final String name;
	private final StatsFunction<T> function;
	private MutableDouble  denominator;
	private final StatsFunction<T> denominatorFunction;

	
	public StatsFunctionRow(StatsFunction<T> function,
			StatsFunction<T> denominatorFunction) {
		this(function.getName(), function, denominatorFunction, new MutableDouble(0));
	}

	public StatsFunctionRow(StatsFunction<T> function,
			StatsFunction<T> denominatorFunction, MutableDouble  denominator) {
		this(function.getName(), function, denominatorFunction, denominator);
	}

	public StatsFunctionRow(String name, StatsFunction<T> function,
			StatsFunction<T> denominatorFunction, MutableDouble  denominator) {
		this.name = name;
		this.function = function;
		this.denominatorFunction = denominatorFunction;
		this.denominator = denominator;
	}

	public String getName() {
		return name;
	}

	public StatsFunction<T> getFunction() {
		return function;
	}

	public String getDenominatorName() {
		return denominatorFunction.getName();
	}
	
	public MutableDouble getDenominator() {
		return denominator;
	}

	public void setDenominatorValue(double value) {
		this.denominator.setValue(value);
	}
	
/*	public void incDenominatorValue(T scapeMember) {
		if (function == null || function.getValue(scapeMember)) {
			denominator = denominator + denominatorFunction.getValue(scapeMember);
		}
	}
*/}
