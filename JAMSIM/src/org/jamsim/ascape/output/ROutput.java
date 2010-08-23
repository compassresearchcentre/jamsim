package org.jamsim.ascape.output;

import net.casper.data.model.CBuilder;
import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

import org.apache.commons.lang.NotImplementedException;
import org.jamsim.ascape.r.ScapeRInterface;
import org.omancode.r.RDataFrame;
import org.omancode.r.RInterfaceException;
import org.omancode.r.RMatrix;
import org.omancode.r.RUtil;
import org.omancode.r.RVector;
import org.omancode.r.UnsupportedTypeException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPVector;

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
	private final String shortName;
	private final String rCommand;

	/**
	 * Default constructor.
	 * 
	 * @param scapeR
	 *            r interface for running the command.
	 * @param shortName
	 *            short name
	 * @param name
	 *            name
	 * @param rCommand
	 *            r command to run on the dataframe at then end of every scape
	 *            iteration. Where the string "DATAFRAME" appears, this will be
	 *            substituted with {@code dataFrameName + run number}.
	 */
	public ROutput(String shortName, String name, ScapeRInterface scapeR,
			String rCommand) {
		this.scapeR = scapeR;
		this.shortName = shortName;
		this.name = name;
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
		// where the string "DATAFRAME" appears,
		// substitute with {@code dataFrameName + run number}.
		String cmd = scapeR.rcmdReplace(rCommand, run);

		try {

			System.out.println("Routput: " + cmd);
			rexp = scapeR.parseEvalTry(cmd);

			CBuilder builder;

			if (RMatrix.isMatrix(rexp)) {
				builder = new RMatrix(getName(), rexp);
			} else if (RDataFrame.isDataFrame(rexp)) {
				builder = new RDataFrame(getName(), rexp);
			} else if (rexp instanceof REXPVector) {
				builder = new RVector(getName(), (REXPVector) rexp);
			} else {
				throw new NotImplementedException(cmd
						+ " returned rexp of class "
						+ RUtil.getClassAttribute(rexp)
						+ ".\nConversion of this class to "
						+ "dataset not yet implemented.");
			}

			return new CDataCacheContainer(builder);

		} catch (RInterfaceException e) {
			throw new CDataGridException(cmd + ": " + e.getMessage(), e);
		} catch (UnsupportedTypeException e) {
			throw new CDataGridException(cmd + ": " + e.getMessage(), e);
		}
	}

}
