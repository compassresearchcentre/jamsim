package org.jamsim.casper;

import java.util.Map;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.data.model.CDataRowSet;
import net.casper.ext.cellreader.CellReader;
import net.casper.io.file.CDataFileDef;

/**
 * Extends CDataFileDef to provide a map of a set of columns.
 * 
 * @param <M>
 *            type of map returned by this definition.
 * @author Oliver Mannion
 * @version $Revision$
 */
public class CDataFileDefWithMap<M extends Map<Object, Object>> extends
		CDataFileDef {

	private final String keyColumn;

	private final String valueColumn;

	private final M map;

	/**
	 * Construct a casper dataset file defintion that returns a map.
	 * 
	 * @param name
	 *            casper container name.
	 * @param columns
	 *            Names of the columns in the dataset, separated by commas. eg:
	 *            "firstname,lastname" (NB: do not include a space after the
	 *            comma).
	 * @param cellReaders
	 *            Cell readers for each column.
	 * @param primaryKey
	 *            The names of the columns that form the primary key, separated
	 *            by commas, eg: "firstname,lastname" (NB: do not include a
	 *            space after the comma).
	 * @param map
	 *            The map that is filled from the dataset.
	 * @param mapKeyColumn
	 *            The dataset column that specifies the keys for the map.
	 * @param mapValueColumn
	 *            The dataset column that specifies the values for the map.
	 */
	public CDataFileDefWithMap(String name, String columns,
			CellReader<?>[] cellReaders, String primaryKey, M map,
			String mapKeyColumn, String mapValueColumn) {
		super(name, columns, cellReaders, primaryKey);

		this.keyColumn = mapKeyColumn;
		this.valueColumn = mapValueColumn;

		this.map = map;
	}

	/**
	 * Get the map from the dataset. Must be called after
	 * {@link #loadDataset(java.io.File)} has been called.
	 * 
	 * @return the map
	 * @throws CDataGridException
	 *             if problem reading the dataset
	 */
	public M getMap() throws CDataGridException {
		CDataCacheContainer source = getContainer();

		if (source == null) {
			throw new IllegalStateException("Dataset has not been loaded. "
					+ "Call loadDataset(file) before getMap().");
		}

		CDataRowSet rowset = source.getAll();

		while (rowset.next()) {
			map.put(rowset.getObject(keyColumn), rowset
					.getObject(valueColumn));
		}

		return map;
	}

}
