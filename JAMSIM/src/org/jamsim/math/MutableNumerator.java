package org.jamsim.math;

import org.apache.commons.lang.math.NumberUtils;

/**
 * Represents the numerator of a fraction.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class MutableNumerator extends Number implements
		Comparable<MutableNumerator> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5582099331306228081L;

	private double numerator;
	private final double originalNumerator;
	private final double denominator;
	private double fraction;

	private final String name;


	/**
	 * Construct {@link MutableNumerator} with the name "untitled" and a
	 * numerator of 0.
	 * 
	 * @param denominator
	 *            denominator
	 */
	public MutableNumerator(double denominator) {
		this("untitled", denominator);
	}

	/**
	 * Construct {@link MutableNumerator} with a numerator of 0.
	 * 
	 * @param name
	 *            name
	 * @param denominator
	 *            denominator
	 */
	public MutableNumerator(String name, double denominator) {
		this(name, denominator, 0);
	}

	/**
	 * Master constructor.
	 * 
	 * @param name
	 *            name
	 * @param denominator
	 *            denominator
	 * @param numerator
	 *            numerator
	 */
	public MutableNumerator(String name, double denominator, double numerator) {
		this.name = name;
		this.originalNumerator = numerator;
		this.numerator = numerator;
		this.denominator = denominator;
		calcFraction();
	}

	/**
	 * Return the numerator as a double value.
	 * 
	 * @return numerator as double.
	 */
	@Override
	public double doubleValue() {
		return numerator;
	}

	/**
	 * Get the original numerator, ie: the numerator when this was constructed. 
	 *
	 * @return original numerator
	 */
	public double getOriginalValue() {
		return originalNumerator;
	}
	
	/**
	 * Return the numerator as a double value.
	 * 
	 * @return numerator as double.
	 */
	@Override
	public float floatValue() {
		return (float) numerator;
	}

	/**
	 * Return the numerator as a int value.
	 * 
	 * @return numerator as int.
	 */
	@Override
	public int intValue() {
		return (int) numerator;
	}

	/**
	 * Return the numerator as a long value.
	 * 
	 * @return numerator as long.
	 */
	@Override
	public long longValue() {
		return (long) numerator;
	}

	/**
	 * Set numerator.
	 * 
	 * @param numerator
	 *            numerator.
	 */
	public void setNumerator(double numerator) {
		this.numerator = numerator;
		calcFraction();
	}

	private void calcFraction() {
		fraction = numerator / denominator;
	}

	/**
	 * Get the fraction (ie: numerator / denominator).
	 * 
	 * @return fraction
	 */
	public double getFraction() {
		return fraction;
	}

	@Override
	public String toString() {
		return name + ": " + numerator;
	}

	/**
	 * Get denominator of the fraction.
	 * 
	 * @return denominator
	 */
	public double getDenominator() {
		return denominator;
	}

	/**
	 * Get name.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Compares this object against the specified object. The result is
	 * <code>true</code> if and only if the argument is not <code>null</code>
	 * and is a <code>Double</code> object that represents a double that has the
	 * identical bit pattern to the bit pattern of the double represented by
	 * this object. For this purpose, two <code>double</code> values are
	 * considered to be the same if and only if the method
	 * {@link Double#doubleToLongBits(double)}returns the same long value when
	 * applied to each.
	 * <p>
	 * Note that in most cases, for two instances of class <code>Double</code>,
	 * <code>d1</code> and <code>d2</code>, the value of
	 * <code>d1.equals(d2)</code> is <code>true</code> if and only if
	 * <blockquote>
	 * 
	 * <pre>
	 * d1.doubleValue() == d2.doubleValue()
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * also has the value <code>true</code>. However, there are two exceptions:
	 * <ul>
	 * <li>If <code>d1</code> and <code>d2</code> both represent
	 * <code>Double.NaN</code>, then the <code>equals</code> method returns
	 * <code>true</code>, even though <code>Double.NaN==Double.NaN</code> has
	 * the value <code>false</code>.
	 * <li>If <code>d1</code> represents <code>+0.0</code> while <code>d2</code>
	 * represents <code>-0.0</code>, or vice versa, the <code>equal</code> test
	 * has the value <code>false</code>, even though <code>+0.0==-0.0</code> has
	 * the value <code>true</code>. This allows hashtables to operate properly.
	 * </ul>
	 * 
	 * @param obj
	 *            the object to compare with.
	 * @return <code>true</code> if the objects are the same; <code>false</code>
	 *         otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof MutableNumerator)
				&& (Double.doubleToLongBits(((MutableNumerator) obj)
						.doubleValue()) == Double
						.doubleToLongBits(doubleValue()));
	}

	/**
	 * Returns a suitable hashcode for this mutable.
	 * 
	 * @return a suitable hashcode
	 */
	@Override
	public int hashCode() {
		long bits = Double.doubleToLongBits(doubleValue());
		return (int) (bits ^ (bits >>> 32));
	}

	/**
	 * Compares this mutable to another in ascending order.
	 * 
	 * @param obj
	 *            the mutable to compare to
	 * @return negative if this is less, zero if equal, positive if greater
	 * @throws ClassCastException
	 *             if the argument is not a MutableDouble
	 */
	@Override
	public int compareTo(MutableNumerator obj) {
		double anotherVal = obj.doubleValue();
		return NumberUtils.compare(fraction, anotherVal);
	}

}