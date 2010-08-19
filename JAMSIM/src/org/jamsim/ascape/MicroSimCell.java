package org.jamsim.ascape;

import java.util.Observable;
import java.util.Observer;

import net.casper.data.model.CDataGridException;
import net.casper.io.beans.CMarkedUpRow;
import net.casper.io.beans.CMarkedUpRowBean;

import org.ascape.model.Cell;
import org.jamsim.ascape.weights.WeightCalculator;

/**
 * {@link MicroSimCell} handles global data for all derived agents.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * @param <D>
 *            a scape data class that defines data external to the scape for use
 *            by agents, and for loading agents.
 */
public abstract class MicroSimCell<D extends ScapeData> extends Cell
		implements CMarkedUpRowBean, Observer {

	private static ScapeData data;

	/**
	 * 
	 */
	private static final long serialVersionUID = 4987400368886624377L;

	/**
	 * Get scape data.
	 * 
	 * @return scape data
	 */
	@SuppressWarnings("unchecked")
	public D getScapeData() {
		return (D) data;
	}

	/**
	 * Set the {@link ScapeData} data globals common to all {@link MicroSimCell}
	 * s.
	 * 
	 * @param inData
	 *            data
	 */
	public static void setData(ScapeData inData) {
		data = inData;
	}

	@Override
	public abstract void setMarkedUpRow(CMarkedUpRow row)
			throws CDataGridException;

	/**
	 * Set the weight on this cell. Called when {@link WeightCalculator} is
	 * changed.
	 * 
	 * @param o
	 *            {@link WeightCalculator} object
	 * @param arg
	 *            not used
	 */
	@Override
	public void update(Observable o, Object arg) {

		if (!(o instanceof WeightCalculator)) {
			throw new IllegalArgumentException("Observable must be of type "
					+ WeightCalculator.class.getSimpleName() + " not "
					+ o.getClass().getSimpleName());
		}

		setWeight((WeightCalculator) o);
	}

	/**
	 * Set the weight on this cell.
	 * 
	 * @param weightings
	 *            weightings
	 */
	public abstract void setWeight(WeightCalculator weightings);

}
