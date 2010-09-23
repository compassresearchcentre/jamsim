package org.jamsim.ascape.output;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

/**
 * A {@link OutputDatasetProvider} that provides a multiple run dataset.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface MultiRunOutputDatasetProvider extends OutputDatasetProvider {

	/**
	 * Output dataset short name. Can be used as an ID.
	 * 
	 * @return short name
	 */
	String getShortName();
	
	/**
	 * Dataset returned at the end of all runs.
	 * 
	 * @return casper dataset
	 * @throws CDataGridException if problem creating dataset
	 */
	CDataCacheContainer getMultiRunDataset() throws CDataGridException;

}
