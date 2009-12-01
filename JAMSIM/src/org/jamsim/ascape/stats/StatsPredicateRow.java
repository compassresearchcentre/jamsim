package org.jamsim.ascape.stats;


public class StatsPredicateRow<T> {

	private final String name;
	private final StatsPredicate<T> predicate;
	private double denominator;
	private final StatsFunction<T> denominatorFunction;

	
	public StatsPredicateRow(StatsPredicate<T> predicate,
			StatsFunction<T> denominatorFunction) {
		this(predicate.getName(), predicate, denominatorFunction);
	}
	
	public StatsPredicateRow(String name, StatsPredicate<T> predicate,
			StatsFunction<T> denominatorFunction) {
		this.name = name;
		this.predicate = predicate;
		this.denominatorFunction = denominatorFunction;
	}

	public String getName() {
		return name;
	}

	public StatsPredicate<T> getPredicate() {
		return predicate;
	}

	public String getDenominatorName() {
		return denominatorFunction.getName();
	}
	
	public double getDenominatorValue() {
		return denominator;
	}

	public void incDenominatorValue(T scapeMember) {
		if (predicate == null || predicate.getValue(scapeMember)) {
			denominator = denominator + denominatorFunction.getValue(scapeMember);
		}
	}
}
