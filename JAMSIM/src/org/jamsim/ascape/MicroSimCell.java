package org.jamsim.ascape;

import org.ascape.model.Cell;
import org.ascape.model.Scape;
import org.jamsim.math.RNG;

/**
 * {@link MicroSimCell} handles global data for all derived agents.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * @param <D>
 *            a scape data class that defines data external to the scape for use
 *            by agents, and for loading agents.
 */
public class MicroSimCell<D extends ScapeData> extends Cell {

	private static ScapeData data;
	private static RNG random;

	/**
	 * 
	 */
	private static final long serialVersionUID = 4987400368886624377L;

	/**
	 * Get random number generator.
	 * 
	 * @return random number generator.
	 */
	public RNG getRNG() {
		return random;
	}

	/**
	 * Get scape data.
	 * 
	 * @return scape data
	 */
	public D getScapeData() {
		return (D) data;
	}

	/**
	 * Called once when the scape is created and the prototype agent is set.
	 * Sets up global static variables.
	 * 
	 * @param scape
	 *            scape
	 */
	@Override
	public void setScape(Scape scape) {
		super.setScape(scape);

		if (data == null) {
			MicroSimScape<ScapeData> msScape =
					(MicroSimScape<ScapeData>) scape;

			data = msScape.getScapeData();
		}

		if (random == null) {
			random = new RNG() {

				@Override
				public double next() {
					return randomInRange(0.0, 1.0);
				}

			};
		}
	}

	/**
	 * Set the {@link ScapeData} data globals common to all patients. Exposed
	 * for testing purposes.
	 * 
	 * @param inData
	 *            data
	 */
	public void setData(ScapeData inData) {
		data = inData;
	}

}
