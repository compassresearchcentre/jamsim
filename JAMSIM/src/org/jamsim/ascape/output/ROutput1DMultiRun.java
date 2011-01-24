package org.jamsim.ascape.output;

import org.jamsim.ascape.r.ScapeRInterface;
import org.omancode.r.RFaceException;
import org.omancode.r.types.REXPAttr;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPMismatchException;

/**
 * An output dataset produced by running commands on the scape dataframe in R.
 * Runs the R command when the iteration finishes and returns a casper dataset.
 * The R command must return a numeric (double) vector.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 */
public class ROutput1DMultiRun extends Abstract1DMultiRunOutputDataset {

	private final ScapeRInterface scapeR;
	private final String rCommand;
	private String[] valueNames;

	/**
	 * Master constructor. Value names are supplied as a string array.
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
	 *            name of each value returned in the double vector from R. If
	 *            {@code null} then they retrieved from the names attribute of
	 *            the R expression returned by {@code rCommand}.
	 * @param scapeR
	 *            r interface for running the command.
	 * @param rCommand
	 *            r command to run on the dataframe at then end of every scape
	 *            iteration. Where the data frame symbol appears this will be
	 *            substituted with {@code dataFrameName + run number} - see
	 *            {@link ScapeRInterface#rcmdReplace(String, int)}.
	 */
	public ROutput1DMultiRun(String shortName, String name,
			String columnHeading, String[] valueNames,
			ScapeRInterface scapeR, String rCommand) {
		super(shortName, name, columnHeading);
		this.scapeR = scapeR;
		this.rCommand = rCommand;
		this.valueNames = valueNames;
	}

	/**
	 * Constructor with value names not supplied. Instead they are retrieved
	 * from the names attribute of the R expression returned by
	 * {@link #rCommand}.
	 * 
	 * @param shortName
	 *            short name. This will become the name of the dataframe created
	 *            in R to hold these results. This dataframe is needed to
	 *            generate an multi-run average over all runs.
	 * @param name
	 *            name
	 * @param columnHeading
	 *            column heading of the dataset column that contains the value
	 *            names
	 * @param scapeR
	 *            r interface for running the command.
	 * @param rCommand
	 *            r command to run on the dataframe at then end of every scape
	 *            iteration. Where the data frame symbol appears this will be
	 *            substituted with {@code dataFrameName + run number} - see
	 *            {@link ScapeRInterface#rcmdReplace(String, int)}.
	 */
	public ROutput1DMultiRun(String shortName, String name,
			String columnHeading, ScapeRInterface scapeR, String rCommand) {
		this(shortName, name, columnHeading, null, scapeR, rCommand);
	}

	/**
	 * Static constructor that uses an R command to retrieve the value names.
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
	 *            iteration. Where the data frame symbol appears this will be
	 *            substituted with {@code dataFrameName + run number} - see
	 *            {@link ScapeRInterface#rcmdReplace(String, int)}.
	 * @return {@link ROutput1DMultiRun}
	 */
	public static ROutput1DMultiRun createNamesFromRCommand(String shortName,
			String name, String columnHeading, String valueNameCommand,
			ScapeRInterface scapeR, String rCommand) {
		return new ROutput1DMultiRun(shortName, name, columnHeading, scapeR
				.evalReturnStrings(valueNameCommand), scapeR, rCommand);
	}

	@Override
	public double[] getValues(int run) {
		String cmd = "";

		try {
			// where the data frame symbol appears,
			// substitute with {@code dataFrameName + run number}.
			cmd = scapeR.rcmdReplace(rCommand, run);

			// System.out.println("Debug R:" + cmd);

			REXP rexp = scapeR.parseEvalTry(cmd);

			// r command must return a REXPDouble
			if (!(rexp instanceof REXPDouble)) {
				throw new IllegalArgumentException(cmd + " returned "
						+ rexp.getClass().getCanonicalName());
			}

			// if value names unspecified during construction,
			// get them from the expression
			if (valueNames == null) {
				valueNames = REXPAttr.getNamesAttribute(rexp);
				if (valueNames == null) {
					throw new RFaceException(
							"Result does not supply names attribute.");
				}
			}

			return rexp.asDoubles();

		} catch (RFaceException e) {
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
