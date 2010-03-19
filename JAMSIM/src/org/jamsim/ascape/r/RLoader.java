package org.jamsim.ascape.r;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.ascape.runtime.RuntimeEnvironment;
import org.ascape.runtime.swing.DesktopEnvironment;
import org.ascape.runtime.swing.UserFrame;
import org.ascape.util.swing.AscapeGUIUtil;
import org.jamsim.r.RInterfaceException;
import org.jamsim.r.RInterfaceHL;
import org.jamsim.r.RSwingConsole;
import org.jamsim.r.RUtil;

/**
 * Create an {@link RSwingConsole}, load and initialise R, and add the console
 * to the Ascape GUI.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class RLoader {

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
	 * Load and initialise R without a startup file.
	 * 
	 * @throws RInterfaceException
	 *             if problem loading or initialising R
	 */
	public RLoader() throws RInterfaceException {
		// create console but don't show the prompt
		rConsole = new RSwingConsole(false);

		// load & initialise R
		rInterface = loadR(rConsole);
		initR();
	}

	/**
	 * Construct with a file to load at R startup.
	 * 
	 * @param startUpFile
	 *            file of R commands to load into R when it is started, or
	 *            {@code null} if no file to load.
	 * @throws RInterfaceException
	 *             if problem loading or initialising R
	 * @throws IOException
	 *             if problem reading startup file
	 */
	public RLoader(File startUpFile) throws RInterfaceException, IOException {
		this();

		if (startUpFile != null) {
			loadRFile(startUpFile);
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
	 * Loads R and creates the R Console as a tab on the console pane.
	 * 
	 * @throws RInterfaceException
	 *             if problem loading R.
	 */
	private RInterfaceHL loadR(RSwingConsole rcon) throws RInterfaceException {
		// if running a desktop environment (ie: GUI)
		RuntimeEnvironment runtime = AscapeGUIUtil.getDesktopEnvironment();
		if (runtime == null) {
			throw new IllegalStateException(
					"Not running in desktop environment, "
							+ "or desktop environment not yet loaded.");
		}

		// display message on ascape log tab
		System.out.print("Starting R....");

		// load R
		RInterfaceHL rint = RInterfaceHL.getInstance(rcon);

		// if R loaded OK, display the R console
		displayRConsole(((DesktopEnvironment) runtime).getUserFrame(), rcon);

		return rint;
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
	 * Initialises the (already loaded) R environment. Loads required packages
	 * and support functions and sets options.
	 * 
	 * @throws RInterfaceException
	 *             if problem evaluating initialisation commands
	 */
	private void initR() throws RInterfaceException {
		rInterface.loadPackage("rJava");
		rInterface.loadPackage("JavaGD");

		rInterface.printlnToConsole("Setting max.print = " + MAX_PRINT);
		rInterface.eval("options(max.print=" + MAX_PRINT + ")");

		loadRSupportFunctions();
	}

	/**
	 * Evaluation the contents of a file in R.
	 * 
	 * @param file
	 *            file containing R commands
	 * @throws RInterfaceException
	 *             if problem evaluating file
	 * @throws IOException
	 *             if problem reading file
	 */
	public final void loadRFile(File file) throws RInterfaceException,
			IOException {
		rInterface.printlnToConsole("Loading " + file.getCanonicalPath());

		rInterface.parseEvalPrint(RUtil.readRFile(file));
	}

	/**
	 * Load the support functions.
	 * 
	 * @throws RInterfaceException
	 *             if problem evaluating support function file.
	 */
	private void loadRSupportFunctions() throws RInterfaceException {
		InputStream ins = getClass().getResourceAsStream(SUPPORT_FILE);

		try {
			rInterface.eval(RUtil.readRStream(ins));
		} catch (IOException e) {
			throw new RInterfaceException(e);
		}
	}

}
