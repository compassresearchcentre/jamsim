package org.jamsim.casper;

import java.io.File;
import java.io.IOException;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.data.model.CDataRowSet;
import net.casper.io.file.CDataFile;

import org.jamsim.math.IntervalsIntMap;

/**
 * Provides an IntervalsIntMap from the columns of a {@link #cDataFile}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class CDataFileDefIntervals implements CDataFile {

	private final CDataFile cDataFile;

	private final String probColumn;

	private final String valueColumn;

	/**
	 * Construct a casper dataset file definition that returns a map.
	 * 
	 * @param cDataFile
	 *            dataset file definition
	 * @param probabilitiesColumn
	 *            The dataset column of {@code cDataFile} that specifies the
	 *            probabilities. Must be of type Double.
	 * @param mappedValuesColumn
	 *            The dataset column of {@code cDataFile} that specifies the
	 *            mapped values for probabilities. Must be of type Integer.
	 */
	public CDataFileDefIntervals(CDataFile cDataFile,
			String probabilitiesColumn, String mappedValuesColumn) {
		this.cDataFile = cDataFile;
		this.probColumn = probabilitiesColumn;
		this.valueColumn = mappedValuesColumn;

	}

	/**
	 * Get the map from the dataset. Must be called after
	 * {@link #loadDataset(java.io.File)} has been called.
	 * 
	 * @return the map
	 * @throws CDataGridException
	 *             if problem reading the dataset
	 */
	public IntervalsIntMap getMap() throws CDataGridException {
		CDataCacheContainer source = getContainer();

		if (source == null) {
			throw new IllegalStateException("Dataset has not been loaded. "
					+ "Call loadDataset(file) before getMap().");
		}

		CDataRowSet rowset = source.getAll();
		double[] probabilities = new double[rowset.size()];
		int[] values = new int[rowset.size()];
		int i = 0; // NOPMD

		while (rowset.next()) {
			probabilities[i] = rowset.getDouble(probColumn);
			values[i] = rowset.getInt(valueColumn);
			i++;
		}

		return IntervalsIntMap.newInstanceFromProbabilities(probabilities,
				values);
	}

	@Override
	public CDataCacheContainer getContainer() {
		return cDataFile.getContainer();
	}

	@Override
	public String getName() {
		return cDataFile.getName();
	}

	@Override
	public CDataCacheContainer loadDataset(File file) throws IOException {
		return cDataFile.loadDataset(file);
	}

}