package org.jamsim.ascape;

import java.awt.Font;
import java.util.TooManyListenersException;

import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.ascape.model.event.DefaultScapeListener;
import org.ascape.model.event.ScapeEvent;
import org.ascape.runtime.RuntimeEnvironment;
import org.ascape.runtime.swing.DesktopEnvironment;
import org.ascape.runtime.swing.UserFrame;
import org.jamsim.r.RInterfaceException;
import org.jamsim.r.RInterfaceHL;
import org.jamsim.r.RSwingConsole;

/**
 * Connects an Ascape scape to R. When this listener is added to a scape, it
 * will create an RConsole. When the simulation stops, a dataframe of the scape
 * will be created in R.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ScapeRInterface extends DefaultScapeListener {
	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = -5105471052036807288L;

	/**
	 * R Console tab title.
	 */
	private static final String RCONSOLE_TAB_TITLE = "R Console";

	/**
	 * R interface.
	 */
	private transient RInterfaceHL rInterface = null;

	/**
	 * Current simulation run.
	 */
	private int runNumber = 1;

	/**
	 * Default constructor.
	 */
	public ScapeRInterface() {
		super("R Scape Interface");
	}

	/**
	 * Add the view to the scape, registering it as a listener, and ensuring
	 * that it hasn't been added to any other scapes.
	 * 
	 * @param scapeEvent
	 *            the event for this scape to make this view the observer of
	 * @throws TooManyListenersException
	 *             the too many listeners exception
	 * @exception TooManyListenersException
	 *                on attempt to add a scape when one is allready added
	 */
	@Override
	public void scapeAdded(ScapeEvent scapeEvent)
			throws TooManyListenersException {
		super.scapeAdded(scapeEvent);
		initializeListener();
	}

	/**
	 * Called once at the beginning after the listener has been added to the
	 * scape. At this point the scape instance variable will have been set.
	 */
	private void initializeListener() {
		// if running a desktop environment (ie: GUI)
		// add the R console
		RuntimeEnvironment runtime = scape.getRunner().getEnvironment();
		if (runtime instanceof DesktopEnvironment) {
			createRConsole(((DesktopEnvironment) runtime).getUserFrame());
		}
	}

	/**
	 * Loads R and creates the R Console as a tab on the console pane. If the R
	 * console has already been created, the method exists silently. If R cannot
	 * be loaded, the error message is written to the Ascape log tab.
	 * 
	 * @param gui
	 *            main frame of the GUI
	 */
	private void createRConsole(UserFrame gui) {
		final JSplitPane consoleSplit = gui.getConsoleSplit();
		final JTabbedPane consolePane = gui.getConsolePane();

		// if a tab with the R console doesn't exist
		if (consolePane.indexOfTab(RCONSOLE_TAB_TITLE) == -1) {

			final RSwingConsole rConsole = new RSwingConsole();
			rConsole.setFont(new Font("Monospaced", Font.PLAIN, 12));

			// display message on ascape log tab
			System.out.print("Starting R....");

			try {
				// load R
				rInterface = RInterfaceHL.getInstance(rConsole);

				// load
				rInterface.loadPackage("rJava");
				rInterface.loadPackage("JavaGD");

				// R loaded, so show R console tab
				Runnable doWorkRunnable = new Runnable() {
					public void run() {

						// move the console pane at the bottom up
						// by changing the split divider location
						consoleSplit.setDividerLocation(500);

						// add R console tab
						consolePane.addTab(RCONSOLE_TAB_TITLE, rConsole);

						// switch to R console tab
						consolePane.setSelectedComponent(rConsole);
					}
				};
				SwingUtilities.invokeLater(doWorkRunnable);

			} catch (RInterfaceException e) {

				// output stack trace to stderr
				e.printStackTrace();

				// output exception message to ascape log tab
				System.out.print(e.getMessage());
			}

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
			if (rInterface != null) {
				String dataframeName =
						scape.getName().toLowerCase() + runNumber++;
				rInterface.assignDataFrame(dataframeName, scape,
						org.ascape.model.Cell.class);
				rInterface.printlnToConsole("Created dataframe "
						+ dataframeName);
			}

		} catch (RInterfaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e); // NOPMD
		}
	}

}