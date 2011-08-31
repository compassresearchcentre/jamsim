package org.jamsim.ascape.weights;

import java.io.IOException;

import org.omancode.math.LastPosRemainder;

import net.casper.data.model.CDataCacheContainer;
import net.casper.ext.swing.CDatasetTableModel;

/**
 * A casper container table model with but the first column editable, and auto
 * adjustment for each row.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class CategoricalVarAdjTableModel extends CDatasetTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -497736236642003604L;

	/**
	 * Used to calculate the remainder for the last unedited value in each row.
	 */
	private final LastPosRemainder[] posRemainders;

	/**
	 * Construct.
	 * 
	 * @param cache
	 *            cache container containing categorical adjustments
	 * @param autoAdjustTotal
	 *            total to adjust the last unedited value in a column. The
	 *            column will sum to this value.
	 * @throws IOException
	 *             if problem accessing cache
	 */
	public CategoricalVarAdjTableModel(CDataCacheContainer cache,
			double autoAdjustTotal) throws IOException {
		super(cache, true, true, true);

		int numCols = getColumnCount() - 1;
		int numRows = getRowCount();

		posRemainders = new LastPosRemainder[numRows];
		for (int i = 0; i < numRows; i++) {
			posRemainders[i] = new LastPosRemainder(numCols, autoAdjustTotal);
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		// all columns except first column are editable.
		return !(col == 0);
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		double dvalue = (value == null) ? 0 : ((Double) value).doubleValue();

		super.setValueAt(dvalue, row, col);

		int lastPos = posRemainders[row].fill(col - 1, dvalue);

		if (lastPos != -1) {
			super.setValueAt(posRemainders[row].getRemainder(), row,
					lastPos + 1);
		}

	}

}
