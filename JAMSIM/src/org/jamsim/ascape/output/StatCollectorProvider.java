package org.jamsim.ascape.output;

import java.util.Collection;

import org.ascape.util.data.StatCollector;

/**
 * Provider of a collection of {@link StatCollector}s.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface StatCollectorProvider {

	/**
	 * Get this group of stat collectors.
	 * 
	 * @return stat collectors
	 */
	Collection<? extends StatCollector> getStatCollectors();


}
