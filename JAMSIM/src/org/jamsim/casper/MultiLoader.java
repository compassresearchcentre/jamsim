package org.jamsim.casper;

import java.io.IOException;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.io.file.CDataFile;

import org.jamsim.io.DatasetFileLoader;
import org.jamsim.math.IntervalsIntMap;
import org.jamsim.matrix.IndexedDenseDoubleMatrix2D;

/**
 * A convenience class that provides methods for loading objects from
 * {@link CDataFile}, {@link CDataFileIntervalsMap} and
 * {@link CDataFileDoubleArray} instances.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class MultiLoader implements DatasetFileLoader {

	private final DatasetFileLoader dsfLoader;

	/**
	 * Construct a {@link MultiLoader} wrapped around {@code dsfLoader}.
	 * 
	 * @param dsfLoader
	 *            dataset file loader
	 */
	public MultiLoader(DatasetFileLoader dsfLoader) {
		this.dsfLoader = dsfLoader;
	}

	/**
	 * Convenience method for loading a casper dataset into a
	 * {@link IndexedDenseDoubleMatrix2D}. Dataset must be made up of all
	 * doubles.
	 * 
	 * @param cdef
	 *            dataset definition
	 * @return matrix
	 * @throws IOException
	 *             problem reading dataset, or dataset doesn't contain doubles.
	 */
	public IndexedDenseDoubleMatrix2D loadMatrix(CDataFile cdef)
			throws IOException {

		try {
			return new IndexedDenseDoubleMatrix2D(loadDataset(cdef));
		} catch (CDataGridException e) {
			throw new IOException(e);
		}

	}

	/**
	 * Convenience method for loading a casper dataset into a
	 * {@link IntervalsIntMap}.
	 * 
	 * @param cdefmap
	 *            dataset definition
	 * @return matrix
	 * @throws IOException
	 *             problem reading dataset, or dataset columns of the wrong
	 *             type.
	 */
	public IntervalsIntMap loadIntervalsMap(CDataFileIntervalsMap cdefmap)
			throws IOException {
		loadDataset(cdefmap);
		return cdefmap.getIntervalsMap();

	}

	/**
	 * Convenience method for loading a casper dataset into a primitive double
	 * array.
	 * 
	 * @param cdefdouble
	 *            dataset definition
	 * @return matrix
	 * @throws IOException
	 *             problem reading dataset, or dataset column is not of type
	 *             double.
	 */
	public double[] loadDoublesArray(CDataFileDoubleArray cdefdouble)
			throws IOException {
		loadDataset(cdefdouble);
		return cdefdouble.getDoublesArray();
	}

	@Override
	public CDataCacheContainer loadDataset(CDataFile cdef) throws IOException {
		return dsfLoader.loadDataset(cdef);
	}

}