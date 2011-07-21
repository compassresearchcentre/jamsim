package org.jamsim.ascape.weights;

import java.io.IOException;

import net.casper.data.model.CDataCacheContainer;
import net.casper.ext.swing.CDatasetTableModel;

public class CategoricalVarAdjTableModel extends CDatasetTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -497736236642003604L;

	public CategoricalVarAdjTableModel(CDataCacheContainer cache)
			throws IOException {
		super(cache, true, true, true);
	}

	/**
	 * All columns except first column are editable.
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return !(col == 0);
	}

}
