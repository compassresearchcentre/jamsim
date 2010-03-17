package org.jamsim.ascape.r;

import org.ascape.model.event.DefaultScapeListener;
import org.ascape.model.event.ScapeEvent;
import org.jamsim.r.RInterfaceException;
import org.omancode.util.ExecutionTimer;

/**
 * R related operations performed on scape events.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ScapeRListener extends DefaultScapeListener {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = -5105471052036807288L;

	private final String rRunEndCommand;

	/**
	 * Flag set after first time scape is closed.
	 */
	private boolean firstCloseExecuted = false;

	private int runNumber = 0;

	private final ExecutionTimer timer = new ExecutionTimer();

	private final ScapeRInterface scapeR;

	/**
	 * Default constructor.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param rRunEndCommand
	 *            R command to run at the end of each run, or {@code null}.
	 * @throws RInterfaceException
	 *             if problem evaluating initialisation commands
	 */
	public ScapeRListener(ScapeRInterface scapeR, String rRunEndCommand)
			throws RInterfaceException {
		super("R Scape Interface");
		this.scapeR = scapeR;
		this.rRunEndCommand = rRunEndCommand;

		// create initial dataframe from scape
		scapeR.assignScapeDataFrame(runNumber);

	}

	/**
	 * At the beginning of all runs, print a blank line to the R console.
	 * 
	 * @param scapeEvent
	 *            the scape event
	 */
	public void scapeInitialized(ScapeEvent scapeEvent) {
		if (runNumber == 0) {
			scapeR.printlnToConsole("");
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
		runNumber++;

		try {

			// create dataframe from scape
			scapeR.assignScapeDataFrame(runNumber);

			if (rRunEndCommand != null) {

				timer.start();

				String rcmd = scapeR.rcmdReplace(rRunEndCommand, runNumber);

				scapeR.parseEvalPrint(rcmd);

				timer.stop();

				System.out.println("Executed " + rcmd + " ("
						+ timer.duration() + " ms)");
			}

		} catch (RInterfaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e); // NOPMD
		}
	}

	/**
	 * Prompt r console prompt when scape closes (ie: end of simulation).
	 * 
	 * @param scapeEvent
	 *            scape event
	 */
	@Override
	public void scapeClosing(ScapeEvent scapeEvent) {

		// scapeClosing gets called twice when the scape closes
		// so we need a flag (firstCloseExecuted) to make sure
		// it doesn't get called twice
		if (firstCloseExecuted) {
			scapeR.printPrompt();
		} else {
			firstCloseExecuted = true;
		}
	}

}