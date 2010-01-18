package org.jamsim.ascape.output;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

public interface MultiRunOutputDatasetProvider extends OutputDatasetProvider {

	CDataCacheContainer getMultiRunDataset() throws CDataGridException;
	
}
