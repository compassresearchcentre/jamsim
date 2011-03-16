package org.jamsim.example.types;

import java.util.Map;

import org.jamsim.data.ValueProvider;
import org.jamsim.data.ValueProviderUtil;

public enum SEX implements ValueProvider<Character> {
	FEMALE('F'), MALE('M');

	private static final Map<Character, ValueProvider<Character>> lookup =
			ValueProviderUtil.mapValues(SEX.values());

	private char value;

	private SEX(char value) {
		this.value = value;
	}

	@Override
	public Character getValue() {
		return value;
	}

	public static SEX get(char value) {
		return (SEX) lookup.get(value);
	}

}