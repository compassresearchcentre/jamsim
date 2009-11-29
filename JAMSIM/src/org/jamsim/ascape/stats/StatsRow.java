package org.jamsim.ascape.stats;

import net.sf.functionalj.Function1;

public class StatsRow<T> {

	private final String name;
	private final Function1<Boolean, T> predicate;
	private double denominator;
	private final Function1<Double, T> denominatorFunction;

	public StatsRow(String name, Function1<Boolean, T> predicate,
			Function1<Double, T> denominatorFunction) {
		this.name = name;
		this.predicate = predicate;
		this.denominatorFunction = denominatorFunction;
	}

	public String getName() {
		return name;
	}

	public Function1<Boolean, T> getPredicate() {
		return predicate;
	}

	public double getDenominatorValue() {
		return denominator;
	}

	public void incDenominatorValue(T scapeMember) {
		if (predicate == null || predicate.call(scapeMember)) {
			denominator = denominator + denominatorFunction.call(scapeMember);
		}
	}
}
