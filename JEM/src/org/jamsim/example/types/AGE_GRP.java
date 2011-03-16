package org.jamsim.example.types;

import java.util.Map;

import org.jamsim.data.ValueProvider;
import org.jamsim.data.ValueProviderUtil;

public enum AGE_GRP implements ValueProvider<Integer> {
	GRP1(1), // 0 - 59
	GRP2(2), // 60 - 79
	GRP3(3); // 80 - 99

	private static final Map<Integer, ValueProvider<Integer>> lookup =
			ValueProviderUtil.mapValues(AGE_GRP.values());

	private int value;

	private AGE_GRP(int value) {
		this.value = value;
	}

	@Override
	public Integer getValue() {
		return value;
	}

	public static AGE_GRP get(int value) {
		return (AGE_GRP) lookup.get(value);
	}

	public static AGE_GRP getFromAge(int age) {
		if (0 <= age && age < 60) {
			return GRP1;
		} else if (60 <= age && age < 80) {
			return GRP2;
		} else if (80 <= age && age <= 100) {
			return GRP3;
		} else {
			throw new IllegalArgumentException(
					"Age must be >= 0 and <= 100");
		}
	}

}