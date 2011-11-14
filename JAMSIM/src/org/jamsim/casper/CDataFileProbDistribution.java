package org.jamsim.casper;

import java.io.File;
import java.io.IOException;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.data.model.CDataRowSet;
import net.casper.io.file.def.CDataFile;
import net.casper.io.file.def.CDataFileDef;

import org.jamsim.math.IntervalsIntMap;
import org.omancode.rmt.cellreader.CellReader;
import org.omancode.rmt.cellreader.CellReaders;

/**
 * Provides an {@link IntervalsIntMap} by accumulation from the columns of a
 * {@link CDataFile}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class CDataFileProbDistribution implements CDataFile {

	private final CDataFile cDataFile;

	private final String probColumn;

	private final String valueColumn;

	private IntervalsIntMap intervalsMap = null;

	/**
	 * From base components, construct a casper dataset file definition that
	 * returns a {@link IntervalsIntMap}.
	 * 
	 * @param name
	 *            casper container name.
	 * @param mappedValuesColumn
	 *            The dataset column of {@code cDataFile} that specifies the
	 *            mapped values for probabilities. Must be of type Integer.
	 * @param probabilitiesColumn
	 *            The dataset column of {@code cDataFile} that specifies the
	 *            probabilities. Must be of type Double.
	 */
	public CDataFileProbDistribution(String name, String mappedValuesColumn,
			String probabilitiesColumn) {
		this(
				new CDataFileDef(name, mappedValuesColumn + ","
						+ probabilitiesColumn, new CellReader<?>[] {
						CellReaders.INTEGER, CellReaders.DOUBLE },
						mappedValuesColumn), mappedValuesColumn,
				probabilitiesColumn);
	}

	/**
	 * From a {@link CDataFile}, construct a casper dataset file definition that
	 * returns a {@link IntervalsIntMap}.
	 * 
	 * @param cDataFile
	 *            dataset file definition
	 * @param mappedValuesColumn
	 *            The dataset column of {@code cDataFile} that specifies the
	 *            mapped values for probabilities. Must be of type Integer.
	 * @param probabilitiesColumn
	 *            The dataset column of {@code cDataFile} that specifies the
	 *            probabilities. Must be of type Double.
	 */
	public CDataFileProbDistribution(CDataFile cDataFile,
			String mappedValuesColumn, String probabilitiesColumn) {
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
	 * @param source
	 *            container
	 * @return intervals map
	 * @throws CDataGridException
	 *             if problem reading container columns
	 */
	private IntervalsIntMap createCumulativeDistribution(
			CDataCacheContainer source) throws CDataGridException {
		CDataRowSet rowset = source.getAll();
		double[] probabilities = new double[rowset.size()];
		int[] values = new int[rowset.size()];
		int i = 0; // NOPMD

		while (rowset.next()) {
			probabilities[i] = rowset.getDouble(probColumn);
			values[i] = rowset.getInt(valueColumn);
			i++;
		}

		return IntervalsIntMap.newProbabilityDistribution(probabilities,
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
			intervalsMap = createCumulativeDistribution(source);
		} catch (CDataGridException e) {
			throw new IOException("Problem loading intervals from "
					+ file.getCanonicalPath(), e);
		}

		return source;
	}

}