package org.jamsim.ascape.stats;


public class StatsFunctionRow<T> {

	private final String name;
	private final StatsFunction<T> function;
	private double denominator;
	private final StatsFunction<T> denominatorFunction;

	
	public StatsFunctionRow(StatsFunction<T> function,
			StatsFunction<T> denominatorFunction) {
		this(function.getName(), function, denominatorFunction, 0);
	}

	public StatsFunctionRow(StatsFunction<T> function,
			StatsFunction<T> denominatorFunction, double denominator) {
		this(function.getName(), function, denominatorFunction, denominator);
	}

	public StatsFunctionRow(String name, StatsFunction<T> function,
			StatsFunction<T> denominatorFunction, double denominator) {
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
	
	public double getDenominatorValue() {
		return denominator;
	}

	public void setDenominator(double denominator) {
		this.denominator = denominator;
	}
	
/*	public void incDenominatorValue(T scapeMember) {
		if (function == null || function.getValue(scapeMember)) {
			denominator = denominator + denominatorFunction.getValue(scapeMember);
		}
	}
*/}
