package org.jamsim.data;

/**
 * Provides a value. Used by model specific enums.
 * 
 * @param <V> value type
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface ValueProvider<V> {

	/**
	 * Return value.
	 * 
	 * @return value
	 */
	V getValue();
	
}
