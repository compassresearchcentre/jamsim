package org.jamsim.ascape.stats;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

public interface OutputDatasetProvider {

	String getName();
	
	CDataCacheContainer getOutputDataset(int run) throws CDataGridException;
	
}
