package org.jamsim.ascape.output;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.r.RInterfaceException;

/**
 * Executes
 * {@link ScapeRInterface#meanOfRuns(CDataCacheContainer, String, String)} on a
 * {@link MultiRunOutputDatasetProvider}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class MeansOnMultiRunDataset implements MultiRunOutputDatasetProvider {

	private final MultiRunOutputDatasetProvider mrDataset;
	private final ScapeRInterface scapeR;

	/**
	 * Construct {@link MultiRunOutputDatasetProvider} wrapper.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param mrDataset
	 *            multi run output dataset
	 */
	public MeansOnMultiRunDataset(ScapeRInterface scapeR,
			MultiRunOutputDatasetProvider mrDataset) {
		this.mrDataset = mrDataset;
		this.scapeR = scapeR;
	}

	@Override
	public CDataCacheContainer getMultiRunDataset() throws CDataGridException {
		CDataCacheContainer allRuns = mrDataset.getMultiRunDataset();
		CDataCacheContainer allRunsWithMean;

		try {
			allRunsWithMean =
					scapeR.meanOfRuns(allRuns, getShortName(), getName());
			return allRunsWithMean;
		} catch (RInterfaceException e) {
			throw new CDataGridException(e);
		}

	}

	@Override
	public String getName() {
		return mrDataset.getName();
	}

	@Override
	public CDataCacheContainer getOutputDataset(int run)
			throws CDataGridException {
		return mrDataset.getOutputDataset(run);
	}

	@Override
	public String getShortName() {
		return mrDataset.getShortName();
	}

}
