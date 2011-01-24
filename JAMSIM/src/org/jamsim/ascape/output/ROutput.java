package org.jamsim.ascape.output;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

import org.jamsim.ascape.r.ScapeRInterface;
import org.omancode.r.RFaceException;
import org.omancode.r.types.CBuildFromREXP;
import org.rosuda.REngine.REXP;

/**
 * A single run output dataset produced by running commands on the scape
 * dataframe. Runs the R command when the iteration finishes and returns a
 * casper dataset.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 */
public class ROutput implements OutputDatasetProvider {

	private final ScapeRInterface scapeR;
	private final String name;
	private final String rCommand;

	/**
	 * Default constructor.
	 * 
	 * @param scapeR
	 *            r interface for running the command.
	 * @param name
	 *            name
	 * @param rCommand
	 *            r command to run on the dataframe at then end of every scape
	 *            iteration. Where the string "DATAFRAME" appears, this will be
	 *            substituted with {@code dataFrameName + run number}.
	 */
	public ROutput(String name, ScapeRInterface scapeR,
			String rCommand) {
		this.scapeR = scapeR;
		this.name = name;
		this.rCommand = rCommand;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CDataCacheContainer getOutputDataset(int run)
			throws CDataGridException {

		REXP rexp;
		// where the string "DATAFRAME" appears,
		// substitute with {@code dataFrameName + run number}.
		String cmd = scapeR.rcmdReplace(rCommand, run);

		try {

			System.out.println("Routput: " + cmd);
			rexp = scapeR.parseEvalTry(cmd);
			return new CDataCacheContainer(new CBuildFromREXP(rexp, name));

		} catch (RFaceException e) {
			throw new CDataGridException(cmd + ": " + e.getMessage(), e);
		} 
	}

}
