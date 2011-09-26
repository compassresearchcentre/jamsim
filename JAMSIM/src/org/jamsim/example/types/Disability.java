package org.jamsim.example.types;

import java.util.Map;

import org.jamsim.data.ValueProvider;
import org.jamsim.data.ValueProviderUtil;

/**
 * Disability state.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public enum Disability implements ValueProvider<Integer> {

	/**
	 * No disability.
	 */
	NO_DIS(1),

	/**
	 * Mild disability.
	 */
	MILD_DIS(2),

	/**
	 * Moderate disability.
	 */
	MODERATE_DIS(3),

	/**
	 * Severe disability.
	 */
	SEVERE_DIS(4);

	/**
	 * int array of all disability state values (ie: 1,2,3,4).
	 */
	public static final int[] ALL_VALUES = ValueProviderUtil
			.allIntValues(Disability.values());

	private static final Map<Integer, ValueProvider<Integer>> LOOKUP =
			ValueProviderUtil.mapValues(Disability.values());

	private int value;

	private Disability(int value) {
		this.value = value;
	}

	@Override
	public Integer getValue() {
		return value;
	}

	/**
	 * For a given int value, return the Disability.
	 * 
	 * @param value
	 *            int value, i.e. one of 1,2,3,4
	 * @return Disability
	 */
	public static Disability get(int value) {
		return (Disability) LOOKUP.get(value);
	}

}