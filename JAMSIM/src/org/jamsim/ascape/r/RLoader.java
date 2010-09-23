package org.jamsim.ascape.r;

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.ascape.runtime.swing.DesktopEnvironment;
import org.ascape.runtime.swing.UserFrame;
import org.ascape.util.swing.AscapeGUIUtil;
import org.omancode.r.RInterfaceException;
import org.omancode.r.RInterfaceHL;
import org.omancode.r.RSwingConsole;
import org.omancode.r.RUtil;

/**
 * Create an {@link RSwingConsole}, load and initialise R, and add the console
 * to the Ascape GUI.
 * 
 * Implemented as a singleton using the singleton enum pattern.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public enum RLoader {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/**
	 * R Console tab title.
	 */
	private static final String RCONSOLE_TAB_TITLE = "R Console";

	/**
	 * Support file containing R functions to load into R environment on
	 * startup.
	 */
	public static final String SUPPORT_FILE = "Ascape.r";

	/**
	 * Max number of lines printed to the R console during evaluation of a
	 * command.
	 */
	public static final int MAX_PRINT = 256;

	/**
	 * R interface.
	 */
	private final transient RInterfaceHL rInterface;

	/**
	 * R console.
	 */
	private final RSwingConsole rConsole;

	/**
	 * Load and initialise R without a startup file and adds the R console to
	 * the Ascape GUI.
	 * 
	 * @throws ExceptionInInitializerError
	 *             if problem loading or initialising R
	 */
	private RLoader() {
		// display message on ascape log tab
		System.out.print("Starting R....");

		// create console but don't show the prompt
		rConsole = new RSwingConsole(false);

		// load R (if not already loaded)
		try {
			rInterface = RInterfaceHL.getInstance(rConsole);

			// display the R console on the console pane
			displayRConsole(rConsole);

			// initialise. Load packages and support functions.
			initR();

		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}

	}

	/**
	 * Installs the R Console as a tab on the console pane. If the R console has
	 * already been installed, the method exists silently.
	 * 
	 * @param gui
	 *            main frame of the GUI
	 * @param rConsole
	 *            R console to be added to the new R tab.
	 */
	private void displayRConsole(final Component rConsole) {
		// if running a desktop environment (ie: GUI)
		DesktopEnvironment desktop = AscapeGUIUtil.getDesktopEnvironment();
		if (desktop == null) {
			throw new IllegalStateException(
					"Not running in desktop environment, "
							+ "or desktop environment not yet loaded.");
		}

		UserFrame gui = desktop.getUserFrame();

		// final JSplitPane consoleSplit = gui.getConsoleSplit();
		final JTabbedPane consolePane = gui.getConsolePane();

		// if a tab with the R console doesn't exist
		if (consolePane.indexOfTab(RCONSOLE_TAB_TITLE) == -1) {

			// create R console tab
			Runnable doWorkRunnable = new Runnable() {
				public void run() {

					// move the console pane at the bottom up
					// by changing the split divider location
					// consoleSplit.setDividerLocation(800);

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
	 * Get R interface.
	 * 
	 * @return r interface.
	 */
	public RInterfaceHL getRInterface() {
		return rInterface;
	}

	/**
	 * Get R console.
	 * 
	 * @return r console.
	 */
	public RSwingConsole getRConsole() {
		return rConsole;
	}

	/**
	 * Initialises the (already loaded) R environment. Loads required packages
	 * and support functions and sets options.
	 * 
	 * @throws RInterfaceException
	 *             if problem loading/evaluating initialisation commands
	 */
	private void initR() throws IOException {
		rInterface.loadRSupportFunctions();
		rInterface.loadPackage("rJava");
		rInterface.loadPackage("JavaGD");

		rInterface.printlnToConsole("Setting max.print = " + MAX_PRINT);
		rInterface.eval("options(max.print=" + MAX_PRINT + ")");

		loadAscapeRSupportFunctions();
	}

	/**
	 * Load the support functions.
	 * 
	 * @throws IOException
	 *             if problem loading or evaluating support function file.
	 */
	private void loadAscapeRSupportFunctions() throws IOException {
		rInterface.printlnToConsole("Loading " + SUPPORT_FILE);

		InputStream ins = getClass().getResourceAsStream(SUPPORT_FILE);
		rInterface.parseEvalPrint(RUtil.readRStream(ins));
	}

	/**
	 * Executes the function "ascapeStart" in R.
	 * 
	 * @throws RInterfaceException
	 *             if problem executing ascapeStart
	 */
	public void ascapeStart() throws RInterfaceException {
		rInterface
				.printlnToConsole("Executing support function ascapeStart()");
		rInterface.parseEvalPrint("ascapeStart()");
	}
}
