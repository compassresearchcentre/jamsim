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
	 * Output dataset short name. Can be used as an ID.
	 * 
	 * @return short name
	 */
	String getShortName();
	
	/**
	 * Output dataset name.
	 * 
	 * @return name
	 */
	String getName();

	/**
	 * Output dataset for the given run number. This is a dataset wrapped around
	 * {@link #getValues(int)}.
	 * 
	 * @param run
	 *            simulation run number
	 * @return output dataset
	 * @throws CDataGridException
	 *             if problem creating dataset
	 */
	CDataCacheContainer getOutputDataset(int run) throws CDataGridException;

	/**
	 * Output data values for the given run number.
	 * 
	 * @param run
	 *            simulation run number
	 * @return output data values.
	 */
	double[] getValues(int run);

}
