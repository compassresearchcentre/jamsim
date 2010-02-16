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
	 * Dataset returned at the end of all runs.
	 * 
	 * @return casper dataset
	 * @throws CDataGridException if problem creating dataset
	 */
	CDataCacheContainer getMultiRunDataset() throws CDataGridException;

}
