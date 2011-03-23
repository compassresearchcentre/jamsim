package org.jamsim.example.types;

import java.util.Map;

import org.jamsim.data.ValueProvider;
import org.jamsim.data.ValueProviderUtil;

/**
 * Disability.
 * 
 * NO_DIS(1) // No Disability
 * MILD_DIS(2) // Mild Disability
 * MODERATE_DIS(3) // Moderate Disability
 * SEVERE_DIS(4) // Severe Disability
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public enum Disability implements ValueProvider<Integer> {
	NO_DIS(1), // No Disability
	MILD_DIS(2), // Mild Disability
	MODERATE_DIS(3), // Moderate Disability
	SEVERE_DIS(4); // Severe Disability

	public static final int[] allValues = ValueProviderUtil
			.allIntValues(Disability.values());

	private static final Map<Integer, ValueProvider<Integer>> lookup =
			ValueProviderUtil.mapValues(Disability.values());

	private int value;

	private Disability(int value) {
		this.value = value;
	}

	@Override
	public Integer getValue() {
		return value;
	}

	public static Disability get(int value) {
		return (Disability) lookup.get(value);
	}

}