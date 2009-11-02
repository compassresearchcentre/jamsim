package org.jamsim.io;

import java.io.IOException;

import net.casper.data.model.CDataCacheContainer;
import net.casper.io.file.CDataFile;

/**
 * Interface describing the loading of a dataset from a file specified by a
 * {@link CDataFile}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface DatasetFileLoader {

	/**
	 * Supplies a File to a {@link CDataFile} and loads the dataset.
	 * 
	 * @param cdef
	 *            {@link CDataFile} object describing the dataset to load.
	 * @throws IOException
	 *             if there is a problem loading the file.
	 * @return a casper container
	 */
	CDataCacheContainer loadDataset(CDataFile cdef) throws IOException;
}
