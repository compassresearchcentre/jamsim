package org.jamsim.r;

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
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.output.ROutputMultiRun;
import org.jamsim.util.ExecutionTimer;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

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
	 * Default dataframe replacement symbol to use if none specified in
	 * constructor.
	 */
	public static final String DEFAULT_DF_SYMBOL = "DATAFRAME";

	/**
	 * R Console tab title.
	 */
	private static final String RCONSOLE_TAB_TITLE = "R Console";

	private static final int MAX_PRINT = 256;

	/**
	 * Dataframe replacement symbol.
	 */
	private final String dataFrameSymbol;

	/**
	 * File of R commands to load into R when it is started.
	 */
	private final File startUpFile;

	private final String rRunEndCommand;

	/**
	 * Flag to keep the dataframes from each run in R. This means creating each
	 * new dataframe with a unique name.
	 */
	private final boolean keepAllRunDFs;

	/**
	 * R interface.
	 */
	private transient RInterfaceHL rInterface = null;

	private final RSwingConsole rConsole;

	private MicroSimScape<?> msScape;

	/**
	 * Flag set after first time scape is closed.
	 */
	private boolean firstCloseExecuted = false;

	private int runNumber = 0;

	private final ExecutionTimer timer = new ExecutionTimer();

	/**
	 * Default constructor. Construct with no startup file, no run end command,
	 * without keeping all run's dataframes, and with {@link #DEFAULT_DF_SYMBOL}
	 * as the dataframe replacement symbol.
	 */
	public ScapeRInterface() {
		this(DEFAULT_DF_SYMBOL, null, null, false);
	}

	/**
	 * Construct {@link #ScapeRInterface()} with a file to load at R startup.
	 * 
	 * @param dataFrameSymbol
	 *            replacement symbol. When evaluating {@link #rRunEndCommand}
	 *            and commands during the creation of output datasets in
	 *            {@link ROutputMultiRun}, this symbol is searched for and
	 *            replaced with the current run's dataframe name.
	 * @param startUpFile
	 *            file of R commands to load into R when it is started
	 * @param rRunEndCommand
	 *            R command to run at the end of each run, or {@code null}.
	 * @param keepAllRunDFs
	 *            flag to keep the dataframes from each run in R. This means
	 *            creating each new dataframe with a unique name.
	 */
	public ScapeRInterface(String dataFrameSymbol, File startUpFile,
			String rRunEndCommand, boolean keepAllRunDFs) {
		super("R Scape Interface");
		this.dataFrameSymbol = dataFrameSymbol;
		this.startUpFile = startUpFile;
		this.keepAllRunDFs = keepAllRunDFs;

		rConsole = new RSwingConsole(false);
		rConsole.setFont(new Font("Monospaced", Font.PLAIN, 12));
		this.rRunEndCommand = rRunEndCommand;
	}

	/**
	 * At the beginning of all runs, print a blank line to the R console.
	 * 
	 * @param scapeEvent
	 *            the scape event
	 */
	public void scapeInitialized(ScapeEvent scapeEvent) {
		if (runNumber == 0) {
			printlnToConsole("");
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
				rInterface.eval("options(max.print=" + MAX_PRINT + ")");

				if (startUpFile != null) {
					try {

						rInterface.printlnToConsole("Loading "
								+ startUpFile.getCanonicalPath());

						parseEvalPrint(RUtil.readRFile(startUpFile));

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
			rInterface.parseEvalTry(file);
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
		runNumber++;

		try {
			if (rInterface != null) {

				String dataframeName = getScapeDFRunName(runNumber);

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

					String rcmd = rcmdReplace(rRunEndCommand, runNumber);

					rInterface.eval(rcmd);

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
	 * Where the {@link #dataFrameSymbol} appears, substitute with the dataframe
	 * scape name returned by {@link #getScapeDFRunName(int)}.
	 * 
	 * @param rcmd
	 *            R command containing text to replace
	 * @param run
	 *            run number
	 * @return R command text with string replaced
	 */
	public String rcmdReplace(String rcmd, int run) {
		return rcmd.replace(dataFrameSymbol, getScapeDFRunName(run));
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
	 * Evaluate a String expression in R in the global environment. See
	 * {@link RInterfaceHL#eval(String)}.
	 * 
	 * @param expr
	 *            expression to evaluate.
	 * @return REXP result of the evaluation.
	 * @throws RInterfaceException
	 *             if problem during parse or evaluation. Parse errors will
	 *             simply return the message "parse error".
	 */
	public REXP eval(String expr) throws RInterfaceException {
		return rInterface.eval(expr);
	}

	/**
	 * Evaluate an expression printing to the console any errors (including
	 * syntactic) and the expression if it is visible. See
	 * {@link RInterfaceHL#parseEvalPrint(String)}.
	 * 
	 * @param expr
	 *            expression to evaluate
	 * @return evaluated result
	 */
	public REXP parseEvalPrint(String expr) {
		return rInterface.parseEvalPrint(expr);
	}

	/**
	 * Display R help for given expression.
	 * 
	 * @param expr
	 *            expression. If {@code ""} displays help contents
	 */
	public void help(String expr) {
		try {
			REXP rexp = rInterface.parseEvalPrint("help(" + expr + ")");

			if (rexp == null || rexp.length() == 0) {
				// error in expression or no documentation found.
				// error message will have been printed to console
				// so print prompt after it
				printPrompt();
			}

		} catch (REXPMismatchException e) {
			// shouldn't be here unless the rexp.length() fails
			// because of some unexpected response
			// from the evaluation
			throw new IllegalStateException();
		}
	}

	/**
	 * Print linefeed to console.
	 */
	public void linefeed() {
		rConsole.linefeed();
	}

	/**
	 * Print the prompt to the console.
	 */
	public void printPrompt() {
		rConsole.printPrompt();
	}

	/**
	 * Evaluate a String expression in R in the global environment. Returns all
	 * console output produced by this evaluation. Does not return the
	 * {@link REXP} produced by the evaluation. This is needed for functions
	 * like {@code str} which print their output and don't return anything.
	 * 
	 * @param expr
	 *            expression to evaluate.
	 * @return console output from the evaluation.
	 * @throws RInterfaceException
	 *             if problem during parse or evaluation. Parse errors will
	 *             simply return the message "parse error".
	 */
	public String evalCaptureOutput(String expr) throws RInterfaceException {
		rConsole.startOutputCapture();
		rInterface.eval(expr);
		return rConsole.stopOutputCapture();
	}

	/**
	 * Evaluate expression returning a String array. See
	 * {@link RInterfaceHL#evalReturnString(String)}.
	 * 
	 * @param expr
	 *            expression to evaluate.
	 * @return REXP result of the evaluation.
	 */
	public String[] evalReturnString(String expr) {
		try {
			return rInterface.evalReturnString(expr);
		} catch (RInterfaceException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Print a message out to the R console with a line feed.
	 * 
	 * @param msg
	 *            message to print.
	 */
	public void printlnToConsole(String msg) {
		try {
			rInterface.printToConsole(msg + "\n");
		} catch (RInterfaceException e) {
			throw new RuntimeException(e);
		}
	}

}