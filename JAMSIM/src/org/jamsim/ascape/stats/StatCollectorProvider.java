package org.jamsim.ascape.stats;

import java.util.Collection;

import org.ascape.util.data.StatCollector;

public interface StatCollectorProvider {

	/**
	 * Get this group of stat collectors.
	 * 
	 * @return stat collectors
	 */
	Collection<? extends StatCollector> getStatCollectors();


}
