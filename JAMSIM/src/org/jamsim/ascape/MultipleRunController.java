package org.jamsim.ascape;

import java.util.TooManyListenersException;

import org.apache.commons.lang.mutable.MutableInt;
import org.ascape.model.Scape;
import org.ascape.model.event.DefaultScapeListener;
import org.ascape.model.event.ScapeEvent;

/**
 * Automatically rerun the simulation a specified number of times. Add this to a
 * scape to enable multi run functionality.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 */
public class MultipleRunController extends DefaultScapeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1305278584691241416L;

	private final MutableInt numberRuns;
	private int currentRun = 0;
	private final boolean closeRunner;

	/**
	 * Run scape for numberRuns, closing the runner at the end of the run.
	 * 
	 * @param numberRuns
	 *            number of runs for the simulation. This is a
	 *            {@link MutableInt} so it can be changed after construction.
	 */
	public MultipleRunController(MutableInt numberRuns) {
		this(numberRuns, true);
	}
	
	/**
	 * Run scape for numberRuns.
	 * 
	 * @param numberRuns
	 *            number of runs for the simulation. This is a
	 *            {@link MutableInt} so it can be changed after construction.
	 * @param closeRunnerAtEnd
	 *            whether to close the runner at the end of the runs
	 */
	public MultipleRunController(MutableInt numberRuns,
			boolean closeRunnerAtEnd) {
		this.numberRuns = numberRuns;
		this.closeRunner = closeRunnerAtEnd;
	}

	/**
	 * On scape add, set scape auto restart to false, as
	 * {@link MultipleRunController} sweep view will be handling scape control.
	 * 
	 * @param scapeEvent
	 *            the scape event
	 * @throws TooManyListenersException
	 *             the too many listeners exception
	 */
	@Override
	public void scapeAdded(ScapeEvent scapeEvent)
			throws TooManyListenersException {
		super.scapeAdded(scapeEvent);

		// as scape restarts will be controlled by us
		((Scape) scapeEvent.getSource()).setAutoRestart(false);
	}

	@Override
	public void scapeStopped(ScapeEvent scapeEvent) {
		currentRun++;

		if (currentRun < numberRuns.intValue()) {
			scape.getRunner().requestRestart();
		} else {
			if (closeRunner) {
				scape.getRunner().close();
			}
		}

	}
}
