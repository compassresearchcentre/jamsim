package org.jamsim.ascape;

import java.util.TooManyListenersException;

import org.ascape.model.Scape;
import org.ascape.model.event.DefaultScapeListener;
import org.ascape.model.event.ScapeEvent;

public class MultipleRunController extends DefaultScapeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1305278584691241416L;

	private final int numberRuns;
	private int currentRun = 0;
	private final boolean closeRunner;
	
	/**
	 * Default constructor.
	 * 
	 * @param numberRuns number of runs for the simulation
	 */
	public MultipleRunController(int numberRuns, boolean closeRunnerAtEnd) {
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

		// as scape will be controlled by us
		((Scape) scapeEvent.getSource()).setAutoRestart(false);
	}

	@Override
	public void scapeStopped(ScapeEvent scapeEvent) {
		currentRun++;
	
		if (currentRun < numberRuns) {
			scape.getRunner().requestRestart();
		} else {
			if (closeRunner) {
				scape.getRunner().close();
			}
		}
			
	}
}
