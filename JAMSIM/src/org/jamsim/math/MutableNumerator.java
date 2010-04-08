package org.jamsim.math;


public class MutableNumerator extends Number  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5582099331306228081L;

	private double numerator;
	private double denominator;
	private double fraction;

	public MutableNumerator(double denominator) {
		this(0, denominator);
	}

	public MutableNumerator(double numerator, double denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
		calcFraction();
	}

	@Override
	public double doubleValue() {
		return numerator;
	}

	@Override
	public float floatValue() {
		return (float) numerator;
	}

	@Override
	public int intValue() {
		return (int) numerator;
	}

	@Override
	public long longValue() {
		return (long) numerator;
	}

	public Double getValue() {
		return new Double(this.numerator);
	}

	public void setNumerator(double numerator) {
		this.numerator = numerator;
		calcFraction();
	}

	private void calcFraction() {
		fraction = numerator / denominator;
	}

	public double getFraction() {
		return fraction;
	}
	
    public String toString() {
        return String.valueOf(numerator);
    }

	public double getDenominator() {
		return denominator;
	}

}