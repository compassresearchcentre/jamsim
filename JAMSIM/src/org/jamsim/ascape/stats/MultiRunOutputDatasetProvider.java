package org.jamsim.ascape.stats;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

public interface MultiRunOutputDatasetProvider extends OutputDatasetProvider {

	CDataCacheContainer getMultiRunDataset() throws CDataGridException;
	
}
