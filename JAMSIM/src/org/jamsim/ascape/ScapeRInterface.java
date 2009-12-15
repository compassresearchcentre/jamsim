package org.jamsim.ascape;

import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
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

	private static final int MAX_PRINT = 256;

	/**
	 * R interface.
	 */
	private transient RInterfaceHL rInterface = null;

	/**
	 * Current simulation run.
	 */
	private int runNumber = 1;

	/**
	 * File of R commands to load into R when it is started.
	 */
	private final File startUpFile;

	
	private final RSwingConsole rConsole;
	
	/**
	 * Default constructor.
	 */
	public ScapeRInterface() {
		this(null);
	}

	/**
	 * Construct {@link #ScapeRInterface()} with a file to load at R startup.
	 * 
	 * @param startUpFile
	 *            file of R commands to load into R when it is started
	 */
	public ScapeRInterface(File startUpFile) {
		super("R Scape Interface");
		this.startUpFile = startUpFile;
		
		rConsole = new RSwingConsole(false);
		rConsole.setFont(new Font("Monospaced", Font.PLAIN, 12));
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
		loadR();
	}

	/**
	 * Loads R, creates the R Console as a tab on the console pane, and does any
	 * initialisation of the R environment.
	 * 
	 * Called once at the beginning after the listener has been added to the
	 * scape. At this point the scape instance variable will have been set.
	 */
	private void loadR() {
		// if running a desktop environment (ie: GUI)
		RuntimeEnvironment runtime = scape.getRunner().getEnvironment();
		if (runtime instanceof DesktopEnvironment) {

			// display message on ascape log tab
			System.out.print("Starting R....");


			// load R
			try {
				rInterface = RInterfaceHL.getInstance(rConsole);

				// if R loaded OK, display the R console
				displayRConsole(
						((DesktopEnvironment) runtime).getUserFrame(),
						rConsole);

				// do any initialisation of the R environment
				rInterface.loadPackage("rJava");
				rInterface.loadPackage("JavaGD");

				rInterface.printlnToConsole("Setting max.print = "
						+ MAX_PRINT);
				rInterface.parseAndEval("options(max.print=" + MAX_PRINT
						+ ")");

				if (startUpFile != null) {
					try {
						rInterface.printlnToConsole("Loading " + startUpFile.getCanonicalPath());
						rInterface.parseAndEval(startUpFile);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				
				// display prompt
				rConsole.setPromptVisible(true);
				rConsole.printPrompt();

			} catch (RInterfaceException e) {
				// output stack trace to stderr
				e.printStackTrace();

				// output exception message to ascape log tab
				System.out.print(e.getMessage());
			}

		}
	}

	public RInterfaceHL getRInterface() {
		return rInterface;
	}

	/**
	 * Load a file containing R code into the R environment.
	 * 
	 * @param file
	 *            text file of R code.
	 * @throws IOException
	 *             if problem loading the file, or evaluating its contents in R.
	 */
	public void loadFile(File file) throws IOException {
		checkInitialized();
		try {
			rInterface.parseAndEval(file);
		} catch (RInterfaceException e) {
			throw new IOException(e);
		}
	}

	public void showPrompt() {
		checkInitialized();
	}

	private void checkInitialized() {
		if (rInterface == null) {
			throw new IllegalStateException(
					"R interface has not been initialized. "
							+ "Have you added me to a scape?");
		}
	}

	/**
	 * Creates the R Console as a tab on the console pane. If the R console has
	 * already been created, the method exists silently.
	 * 
	 * @param gui
	 *            main frame of the GUI
	 * @param rConsole
	 *            R console to be added to the new R tab.
	 */
	private void displayRConsole(UserFrame gui, final Component rConsole) {
		final JSplitPane consoleSplit = gui.getConsoleSplit();
		final JTabbedPane consolePane = gui.getConsolePane();

		// if a tab with the R console doesn't exist
		if (consolePane.indexOfTab(RCONSOLE_TAB_TITLE) == -1) {

			// create R console tab
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
				rInterface.assignDataFrame(dataframeName, scape, scape
						.getPrototypeAgent().getClass().getSuperclass());
				rInterface.printlnToConsole("Created dataframe "
						+ dataframeName);
				rConsole.printPrompt();
			}

		} catch (RInterfaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e); // NOPMD
		}
	}

}