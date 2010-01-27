package org.jamsim.ascape;

import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.TooManyListenersException;

import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import net.casper.data.model.CDataCacheContainer;

import org.ascape.model.event.DefaultScapeListener;
import org.ascape.model.event.ScapeEvent;
import org.ascape.runtime.RuntimeEnvironment;
import org.ascape.runtime.swing.DesktopEnvironment;
import org.ascape.runtime.swing.UserFrame;
import org.jamsim.r.RInterfaceException;
import org.jamsim.r.RInterfaceHL;
import org.jamsim.r.RSwingConsole;
import org.jamsim.r.RUtil;
import org.jamsim.util.ExecutionTimer;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;

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
	 * Dataframe replacement symbol.
	 */
	public static final String DATAFRAME_SYMBOL = "DATAFRAME";

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
	 * File of R commands to load into R when it is started.
	 */
	private final File startUpFile;

	private final RSwingConsole rConsole;

	private final String rRunEndCommand;

	private MicroSimScape<?> msScape;

	/**
	 * Flag set after first time scape is closed.
	 */
	private boolean firstCloseExecuted = false;

	private int currentRun = 0;

	private final ExecutionTimer timer = new ExecutionTimer();

	/**
	 * Flag to keep the dataframes from each run in R. This means creating each
	 * new dataframe with a unique name.
	 */
	private final boolean keepAllRunDFs;

	/**
	 * Default constructor.
	 */
	public ScapeRInterface() {
		this(null, null, false);
	}

	/**
	 * Construct {@link #ScapeRInterface()} with a file to load at R startup.
	 * 
	 * @param startUpFile
	 *            file of R commands to load into R when it is started
	 * @param rRunEndCommand
	 *            R command to run at the end of each run, or {@code null}.
	 * @param keepAllRunDFs
	 *            flag to keep the dataframes from each run in R. This means
	 *            creating each new dataframe with a unique name.
	 */
	public ScapeRInterface(File startUpFile, String rRunEndCommand,
			boolean keepAllRunDFs) {
		super("R Scape Interface");
		this.startUpFile = startUpFile;
		this.keepAllRunDFs = keepAllRunDFs;

		rConsole = new RSwingConsole(false);
		rConsole.setFont(new Font("Monospaced", Font.PLAIN, 12));
		this.rRunEndCommand = rRunEndCommand;
	}

	/**
	 * Called immediatly after the scape is initialized.
	 * 
	 * @param scapeEvent
	 *            the scape event
	 */

	/**
	 * At the beginning of all runs, print a blank line to the R console.
	 */
	public void scapeInitialized(ScapeEvent scapeEvent) {
		if (currentRun == 0) {
			try {
				printlnToConsole("");
			} catch (RInterfaceException e) {
				throw new RuntimeException(e);
			}
		}
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
		if (!(scape instanceof MicroSimScape<?>)) {
			throw new IllegalArgumentException(this.getClass()
					.getSimpleName()
					+ " must be added to an instance of "
					+ MicroSimScape.class.getSimpleName());
		}
		msScape = (MicroSimScape<?>) scape;

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
						rInterface.printlnToConsole("Loading "
								+ startUpFile.getCanonicalPath());
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

				throw new RuntimeException(e);
			}

		}
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
		currentRun++;

		try {
			if (rInterface != null) {

				String dataframeName = getScapeDFRunName(currentRun);

				timer.start();

				assignDataFrame(dataframeName, scape, scape
						.getPrototypeAgent().getClass().getSuperclass());
				rInterface.printlnToConsole("Created dataframe "
						+ dataframeName);

				timer.stop();

				System.out.println("Created dataframe " + dataframeName
						+ " (" + timer.duration() + " ms)");

				if (rRunEndCommand != null) {

					timer.start();

					String rcmd = rcmdReplace(rRunEndCommand, currentRun);

					rInterface.parseAndEval(rcmd);

					timer.stop();

					System.out.println("Executed " + rcmd + " ("
							+ timer.duration() + " ms)");
				}
			}

		} catch (RInterfaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e); // NOPMD
		}
	}

	/**
	 * Where the string {@link #DATAFRAME_SYMBOL} appears, substitute with
	 * {@code dataFrameName + run number}.
	 * 
	 * @param rcmd
	 *            R command containing text to replace
	 * @param run
	 *            run number
	 * @return R command text with string replaced
	 */
	public String rcmdReplace(String rcmd, int run) {
		return rcmd.replace(DATAFRAME_SYMBOL, getScapeDFRunName(run));
	}

	/**
	 * Prompt r console prompt when scape closes (ie: end of simulation).
	 */
	@Override
	public void scapeClosing(ScapeEvent scapeEvent) {

		// scapeClosing gets called twice when the scape closes
		// so we need a flag (firstCloseExecuted) to make sure
		// it doesn't get called twice
		if (firstCloseExecuted) {
			rConsole.printPrompt();
		} else {
			firstCloseExecuted = true;
		}
	}

	/**
	 * Return the name of the scape dataframe created for the given run number.
	 * 
	 * @param run
	 *            run number
	 * @return dataframe name
	 */
	public String getScapeDFRunName(int run) {
		String dfName =
				scape.getName().toLowerCase() + (keepAllRunDFs ? run : "");
		return dfName;
	}

	/**
	 * Create a dataframe in R from the given collection. Introspection is used
	 * to determine the bean properties (i.e.: getter methods) that are exposed,
	 * and each one becomes a column in the dataframe. Columns are only created
	 * for primitive properties and arrays of primitive properties; object
	 * properties are ignored without warning.
	 * 
	 * NB: doesn't automatically create factors like read.table does.
	 * 
	 * @param name
	 *            the name of the dataframe to create in R.
	 * @param col
	 *            the Java collection to convert.
	 * @param stopClass
	 *            Columns are created for all getter methods that are defined by
	 *            {@code stopClass}'s subclasses. {@code stopClass}'s getter
	 *            methods and superclass getter methods are not converted to
	 *            columns in the dataframe.
	 * @throws RInterfaceException
	 *             if Collection cannot be read, or dataframe cannot be created.
	 */
	public void assignDataFrame(String name, Collection<?> col,
			Class<?> stopClass) throws RInterfaceException {
		rInterface.assignDataFrame(name, RUtil.toRList(col, stopClass));
		msScape.addDataFrameNode(name);
	}

	/**
	 * Create a dataframe in R from the given casper dataset.
	 * 
	 * NB: doesn't automatically create factors like read.table does.
	 * 
	 * @param name
	 *            the name of the dataframe to create in R.
	 * @param container
	 *            the casper container to convert.
	 * @throws RInterfaceException
	 *             if Collection cannot be read, or dataframe cannot be created.
	 */
	public void assignDataFrame(String name, CDataCacheContainer container)
			throws RInterfaceException {
		rInterface.assignDataFrame(name, RUtil.toRList(container));
		msScape.addDataFrameNode(name);
	}

	/**
	 * Evaluate a String expression in R in the global environment.
	 * 
	 * @param expr
	 *            expression to evaluate.
	 * @return REXP result of the evaluation.
	 * @throws RInterfaceException
	 *             if problem during parse or evaluation. Parse errors will
	 *             simply return the message "parse error".
	 */
	public REXP parseAndEval(String expr) throws RInterfaceException {
		return rInterface.parseAndEval(expr);
	}

	/**
	 * Evaluate a String expression in R in the global environment. Returns all
	 * console output produced by this evaluation. Does not return the
	 * {@link REXP} produced by the evaluation.
	 * 
	 * @param expr
	 *            expression to evaluate.
	 * @return console output from the evaluation.
	 * @throws RInterfaceException
	 *             if problem during parse or evaluation. Parse errors will
	 *             simply return the message "parse error".
	 */
	public String parseAndEvalCaptureOutput(String expr)
			throws RInterfaceException {
		rConsole.startOutputCapture();
		rInterface.parseAndEval(expr);
		return rConsole.stopOutputCapture();
	}

	/**
	 * Evaluate expression returning a String array.
	 * 
	 * @param expr
	 *            expression to evaluate.
	 * @return REXP result of the evaluation.
	 */
	public String[] parseAndEvalStringVector(String expr) {
		try {
			return rInterface.parseAndEvalStringVector(expr);
		} catch (RInterfaceException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Print a message out to the R console with a line feed.
	 * 
	 * @param msg
	 *            message to print.
	 * @throws RInterfaceException
	 *             if problem during evaluation.
	 */
	public void printlnToConsole(String msg) throws RInterfaceException {
		rInterface.printToConsole(msg + "\n");
	}

}