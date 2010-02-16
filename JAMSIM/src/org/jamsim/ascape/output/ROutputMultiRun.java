package org.jamsim.ascape.output;

import org.jamsim.ascape.r.ScapeRInterface;
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
	 * @param valueNameCommand
	 *            evaluate this command in R to return a String vector that
	 *            specifies the value names
	 * @param scapeR
	 *            r interface for running the command.
	 * @param rCommand
	 *            r command to run on the dataframe at then end of every scape
	 *            iteration. Where the string "DATAFRAME" appears, this will be
	 *            substituted with {@code dataFrameName + run number}.
	 */
	public ROutputMultiRun(String shortName, String name,
			String columnHeading, String valueNameCommand,
			ScapeRInterface scapeR, String rCommand) {
		this(shortName, name, columnHeading, scapeR
				.evalReturnString(valueNameCommand), scapeR, rCommand);
	}

	@Override
	public double[] getValues(int run) {
		String cmd = "";

		try {
			// where the string "DATAFRAME" appears,
			// substitute with {@code dataFrameName + run number}.
			cmd = scapeR.rcmdReplace(rCommand, run);

			//System.out.println("Debug R:" + cmd);
			
			REXP rexp = scapeR.eval(cmd);

			// r command must return a REXPDouble
			if (!(rexp instanceof REXPDouble)) {
				throw new IllegalArgumentException(cmd + " returned "
						+ rexp.getClass().getCanonicalName());
			}

			return rexp.asDoubles();

		} catch (RInterfaceException e) {
			throw new RuntimeException(e.getMessage() + " [" + cmd + "]", e);
		} catch (REXPMismatchException e) {
			throw new RuntimeException(e.getMessage() + " [" + cmd + "]", e);
		}
	}

	@Override
	public String[] getValueNames() {
		return valueNames;
	}

}
