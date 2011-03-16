package org.jamsim.data;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ValueProvider} utility functions.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class ValueProviderUtil {

	private ValueProviderUtil() {
		// no instantiation
	}

	/**
	 * Return array of int from the array of Integer {@link ValueProvider}s.
	 * 
	 * @param vps
	 *            Integer value providers
	 * @return array of int
	 */
	public static int[] allIntValues(ValueProvider<Integer>[] vps) {
		int[] allValues = new int[vps.length];
		for (int i = 0; i < vps.length; i++) {
			allValues[i] = vps[i].getValue();
		}
		return allValues;
	}

	/**
	 * Return map of values to {@link ValueProvider}s.
	 * 
	 * @param <V>
	 *            value provider type
	 * @param vps
	 *            value providers array
	 * @return map
	 */
	public static <V> Map<V, ValueProvider<V>> mapValues(
			ValueProvider<V>[] vps) {

		Map<V, ValueProvider<V>> vpmap = new HashMap<V, ValueProvider<V>>();

		for (ValueProvider<V> vp : vps) {
			vpmap.put(vp.getValue(), vp);
		}

		return vpmap;
	}

}
