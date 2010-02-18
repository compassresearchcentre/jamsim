package org.jamsim.ascape.output;

import net.casper.data.model.CBuilder;
import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

import org.apache.commons.lang.NotImplementedException;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.r.RDataFrame;
import org.jamsim.r.RInterfaceException;
import org.jamsim.r.RMatrix;
import org.jamsim.r.RUtil;
import org.jamsim.r.UnsupportedTypeException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

/**
 * A single run output dataset produced by running commands on the scape
 * dataframe. Runs the R command when the iteration finishes and returns a
 * casper dataset. Currently supports R commands that return a table or a
 * dataset.
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

		try {
			// where the string "DATAFRAME" appears,
			// substitute with {@code dataFrameName + run number}.
			String cmd =
					rCommand.replace("DATAFRAME", scapeR
							.getScapeDFRunName(run));

			rexp = scapeR.eval(cmd);

			CBuilder builder;

			if (RMatrix.isMatrix(rexp)) {
				builder = new RMatrix(getName(), rexp);
			} else if (RDataFrame.isDataFrame(rexp)) {
				builder = new RDataFrame(getName(), rexp);
			} else {
				throw new NotImplementedException(cmd
						+ " returned rexp of class "
						+ RUtil.getClassAttribute(rexp)
						+ ".\nConversion of this class to "
						+ "dataset not yet implemented.");
			}

			return new CDataCacheContainer(builder);

		} catch (RInterfaceException e) {
			throw new CDataGridException(e);
		} catch (REXPMismatchException e) {
			throw new CDataGridException(e);
		} catch (UnsupportedTypeException e) {
			throw new CDataGridException(e);
		}
	}

}
