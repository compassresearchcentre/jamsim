package org.jamsim.ascape.output;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

import org.jamsim.r.RInterfaceException;
import org.jamsim.r.RInterfaceHL;
import org.jamsim.r.RTable;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

/**
 * A single run output dataset produced by running commands on the scape
 * dataframe. Runs the R command when the iteration finishes and returns a
 * casper dataset.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 */
public class ROutputData implements OutputDatasetProvider {

	private final RInterfaceHL rInterface;
	private final String name;
	private final String shortName;
	private final String dataframeName;
	private final String rCommand;

	/**
	 * Default constructor.
	 * 
	 * @param rInterface
	 *            r interface for running the command.
	 * @param shortName
	 *            short name
	 * @param name
	 *            name
	 * @param dataFrameName
	 *            name of the dataframe produced at the end of every scape
	 *            iteration. Do not include the run number suffix.
	 * @param rCommand
	 *            r command to run on the dataframe at then end of every scape
	 *            iteration. Where the string "DATAFRAME" appears, this will be
	 *            substituted with {@code dataFrameName + run number}.
	 */
	public ROutputData(RInterfaceHL rInterface, String shortName,
			String name, String dataFrameName, String rCommand) {
		this.rInterface = rInterface;
		this.shortName = shortName;
		this.name = name;
		this.dataframeName = dataFrameName;
		this.rCommand = rCommand;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CDataCacheContainer getOutputDataset(int run)
			throws CDataGridException {

		REXP rexp;

		try {
			// where the string "DATAFRAME" appears,
			// substitute with {@code dataFrameName + run number}.
			String cmd = rCommand.replace("DATAFRAME", dataframeName + run);

			rexp = rInterface.parseAndEval(cmd);

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

}
