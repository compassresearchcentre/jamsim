package org.jamsim.ascape.stats;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

import org.jamsim.ascape.stats.OutputDatasetProvider;
import org.jamsim.r.RInterfaceException;
import org.jamsim.r.RInterfaceHL;
import org.jamsim.r.RTable;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

/**
 * Number of visits distribution by MIC. Results are obtained from the table
 * produced by the {@code vbmic(patients)} R command. This command is run in R
 * when the scape stops and returned as a casper dataset.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 */
public class ROutputData implements MultiRunOutputDatasetProvider {

	private final RInterfaceHL rInterface;

	/**
	 * Default constructor.
	 * 
	 * @param rInterface
	 *            r interface for running the command.
	 */
	public ROutputData(RInterfaceHL rInterface) {
		this.rInterface = rInterface;
	}

	@Override
	public String getShortName() {
		return "visitsByMIC";
	}

	@Override
	public String getName() {
		return "8 Number of visits by MIC";
	}

	@Override
	public CDataCacheContainer getOutputDataset(int run)
			throws CDataGridException {

		REXP rexp;

		try {
			String dataframe = "patients" + run;

			rexp = rInterface.parseAndEval("vbmic(" + dataframe + ")");
			RTable tbl = new RTable(getName(), rexp);

			return new CDataCacheContainer(tbl);

		} catch (RInterfaceException e) {
			throw new CDataGridException(e);
		} catch (REXPMismatchException e) {
			throw new CDataGridException(e);
		}
	}

	@Override
	public double[] getValues(int run) {
		return new double[0];
	}

	@Override
	public CDataCacheContainer getMultiRunDataset() throws CDataGridException {
		// TODO Auto-generated method stub
		return null;
	}

}
