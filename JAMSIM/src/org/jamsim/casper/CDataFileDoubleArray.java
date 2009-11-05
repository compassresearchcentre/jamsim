package org.jamsim.casper;

import java.io.File;
import java.io.IOException;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.ext.CasperUtil;
import net.casper.io.file.CDataFile;

/**
 * Provides a double array from the column of a {@link CDataFile}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class CDataFileDoubleArray implements CDataFile {

	private final CDataFile cDataFile;

	private final String doublesColumn;
	
	private double[] array = null;

	/**
	 * Construct a casper dataset file definition that returns a double array.
	 * 
	 * @param cDataFile
	 *            dataset file definition
	 * @param doublesColumn
	 *            The dataset column of {@code cDataFile} that contains doubles.
	 */
	public CDataFileDoubleArray(CDataFile cDataFile, String doublesColumn) {
		this.cDataFile = cDataFile;
		this.doublesColumn = doublesColumn;

	}

	/**
	 * Return the array of doubles loaded from the column.
	 * 
	 * @return double array
	 */
	public double[] getDoublesArray() {
		if (array == null) {
			throw new IllegalStateException(
					"Array not been loaded. "
							+ "Has loadDataset(file) been called?");
		}

		return array;
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
			array = CasperUtil.loadDoubleArray(source, doublesColumn);
		} catch (CDataGridException e) {
			throw new IOException("Problem loading double array from "
					+ file.getCanonicalPath(), e);
		}

		return source;
	}

}
