package org.jamsim.casper;

import java.io.File;
import java.io.IOException;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.data.model.CDataRowSet;
import net.casper.io.file.def.CDataFile;

import org.jamsim.math.IntervalsIntMap;

/**
 * Provides an {@link IntervalsIntMap} from the columns of a {@link CDataFile}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class CDataFileIntervalsMap implements CDataFile {

	private final CDataFile cDataFile;

	private final String probColumn;

	private final String valueColumn;

	private IntervalsIntMap intervalsMap = null;

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
	public CDataFileIntervalsMap(CDataFile cDataFile,
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
	 */
	public IntervalsIntMap getIntervalsMap() {
		if (intervalsMap == null) {
			throw new IllegalStateException(
					"Intervals map has not been loaded. "
							+ "Has loadDataset(file) been called?");
		}

		return intervalsMap;
	}

	/** 
	 * Generate the intervals map from a casper container.
	 * 
	 * @param source container
	 * @return intervals map
	 * @throws CDataGridException if problem reading container columns
	 */
	private IntervalsIntMap createIntervalsMap(CDataCacheContainer source)
			throws CDataGridException {
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
		CDataCacheContainer source = cDataFile.loadDataset(file);

		try {
			intervalsMap = createIntervalsMap(source);
		} catch (CDataGridException e) {
			throw new IOException("Problem loading intervals from "
					+ file.getCanonicalPath(), e);
		}

		return source;
	}

}