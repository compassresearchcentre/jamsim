package org.jamsim.ascape.r;

import org.ascape.model.event.DefaultScapeListener;
import org.ascape.model.event.ScapeEvent;
import org.omancode.r.RFaceException;

/**
 * R related operations performed on scape events.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ScapeRListener extends DefaultScapeListener {

	/**
	 * Replacement string used to insert the current iteration number into the R
	 * iteration end command.
	 */
	public static final String ITER_REPLACEMENT_STR = "ITERATION_NBR";

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = -5105471052036807288L;

	private final ScapeRInterface scapeR;

	private final String rIterationEndCmd;

	private final String rRunBeginCmd;

	private final String rRunEndCmd;

	private final String rSimEndCmd;

	/**
	 * Counts number of times the scape is closed.
	 */
	private int scapeClosingCount = 0;

	private int runNumber = 0;

	/**
	 * Default constructor.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param rIterationEndCommand
	 *            R command to run at the end of each iteration, or {@code null}
	 *            .
	 * @param rRunBeginCommand
	 *            R command to run at the beginning of each run, or {@code null}
	 *            .
	 * @param rRunEndCommand
	 *            R command to run at the end of each run, or {@code null}.
	 * @param rSimBeginCommand
	 *            R command to run at the beginning of the simulation (ie:
	 *            during construction of this {@link ScapeRListener}), or
	 *            {@code null}.
	 * @param rSimEndCommand
	 *            R command to run at the end of the simulation (ie: end of all
	 *            runs), or {@code null}.
	 * @throws RFaceException
	 *             if problem evaluating initialisation commands
	 */
	public ScapeRListener(ScapeRInterface scapeR,
			String rIterationEndCommand, String rRunBeginCommand,
			String rRunEndCommand, String rSimBeginCommand,
			String rSimEndCommand) throws RFaceException {
		super("R Scape Interface");
		this.scapeR = scapeR;
		this.rRunBeginCmd = rRunBeginCommand;
		this.rRunEndCmd = rRunEndCommand;
		this.rIterationEndCmd = rIterationEndCommand;
		this.rSimEndCmd = rSimEndCommand;
		
		if (rSimBeginCommand != null) {
			executeRCommand(rSimBeginCommand);
		}
	}

	/**
	 * Run iteration end R command, if any.
	 */
	@Override
	public void scapeIterated(ScapeEvent scapeEvent) {

		if (rIterationEndCmd != null) {
			try {
				scapeR.assignScapeDataFrame(runNumber);
			} catch (RFaceException e) {
				e.printStackTrace();
				throw new RuntimeException(e); // NOPMD
			}

			// replace the iteration replacement string with the
			// current iteration number
			String rCmd =
					rIterationEndCmd.replace(ITER_REPLACEMENT_STR, Integer
							.toString(scape.getIteration()));
			executeRCommand(rCmd);
		}

	}

	/**
	 * At the beginning of all runs, print a blank line to the R console. If R
	 * begin command is specified, execute that.
	 * 
	 * @param scapeEvent
	 *            the scape event
	 */
	public void scapeInitialized(ScapeEvent scapeEvent) {
		runNumber++;

		if (runNumber == 1) {
			scapeR.printlnToConsole("");
		}

		if (rRunBeginCmd != null) {
			try {
				scapeR.assignScapeDataFrame(runNumber);
			} catch (RFaceException e) {
				e.printStackTrace();
				throw new RuntimeException(e); // NOPMD
			}
			executeRCommand(rRunBeginCmd);
		}
	}

	/**
	 * When simulation stops, write out the scape as dataframe to R. Does
	 * nothing if R has not been loaded.
	 * 
	 * @param scapeEvent
	 *            the scape event
	 */
	@Override
	public void scapeStopped(ScapeEvent scapeEvent) {

		try {

			// create dataframe from scape
			scapeR.assignScapeDataFrame(runNumber);

			if (rRunEndCmd != null) {
				executeRCommand(rRunEndCmd);
			}

		} catch (RFaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e); // NOPMD
		}
	}

	private void executeRCommand(String rCommand) {
		scapeR.parseEvalPrintLogReplace(rCommand, runNumber);
	}

	/**
	 * Prompt r console prompt when scape closes (ie: end of simulation).
	 * 
	 * @param scapeEvent
	 *            scape event
	 */
	@Override
	public void scapeClosing(ScapeEvent scapeEvent) {
		scapeClosingCount++;

		// scapeClosing gets called 3 times.
		// Twice when the scape closes at the end of a
		// simulation run, and once when the application closes
		// or the model is reloaded
		if (scapeClosingCount == 2) {
			// execute rSimEndCmd on the second scapeClosing
			// this occurs after the multi-run datasets
			// have been created.

			if (rSimEndCmd != null) {
				executeRCommand(rSimEndCmd);
			}

			scapeR.printPrompt();
		}
	}

}