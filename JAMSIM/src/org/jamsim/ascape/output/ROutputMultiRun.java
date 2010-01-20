package org.jamsim.ascape.output;

import org.jamsim.ascape.ScapeRInterface;
import org.jamsim.r.RInterfaceException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPMismatchException;

/**
 * A single run output dataset produced by running commands on the scape
 * dataframe in R. Runs the R command when the iteration finishes and returns a
 * casper dataset. The R command must return a numeric (double) vector.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 */
public class ROutputMultiRun extends AbstractMultiRunOutputDataset {

	private final ScapeRInterface scapeR;
	private final String rCommand;
	private final String[] valueNames;

	/**
	 * Default constructor.
	 * 
	 * @param shortName
	 *            short name. This will become the name of the dataframe created
	 *            in R to hold these results.
	 * @param name
	 *            name
	 * @param columnHeading
	 *            column heading of the dataset column that contains the value
	 *            names
	 * @param valueNames
	 *            name of each value returned in the double vector from R
	 * @param scapeR
	 *            r interface for running the command.
	 * @param rCommand
	 *            r command to run on the dataframe at then end of every scape
	 *            iteration. Where the string "DATAFRAME" appears, this will be
	 *            substituted with {@code dataFrameName + run number}.
	 */
	public ROutputMultiRun(String shortName, String name,
			String columnHeading, String[] valueNames,
			ScapeRInterface scapeR, String rCommand) {
		super(shortName, name, columnHeading);
		this.scapeR = scapeR;
		this.rCommand = rCommand;
		this.valueNames = valueNames;
	}

	@Override
	public double[] getValues(int run) {
		REXP rexp;

		try {
			// where the string "DATAFRAME" appears,
			// substitute with {@code dataFrameName + run number}.
			String cmd =
					rCommand.replace("DATAFRAME", scapeR
							.getScapeDFRunName(run));

			rexp = scapeR.parseAndEval(cmd);

			// r command must return a REXPDouble
			if (!(rexp instanceof REXPDouble)) {
				throw new IllegalArgumentException(cmd + " returned "
						+ rexp.getClass().getCanonicalName());
			}

			return rexp.asDoubles();

		} catch (RInterfaceException e) {
			throw new RuntimeException(e);
		} catch (REXPMismatchException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String[] getValueNames() {
		return valueNames;
	}

}
