package org.jamsim.ascape.stats;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

/**
 * Provider of a simulation output dataset for a given run number. 
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface OutputDatasetProvider {

	/**
	 * Output dataset name. 
	 * 
	 * @return name
	 */
	String getName();
	
	/**
	 * Output dataset for the given run number.
	 * 
	 * @param run simulation run number
	 * @return output dataset
	 * @throws CDataGridException if problem creating dataset
	 */
	CDataCacheContainer getOutputDataset(int run) throws CDataGridException;
	
}
