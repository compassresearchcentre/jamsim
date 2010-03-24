package org.jamsim.ascape.r;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import net.casper.data.model.CDataCacheContainer;

import org.jamsim.ascape.MicroSimScape;
import org.jamsim.r.RInterfaceException;
import org.jamsim.r.RInterfaceHL;
import org.jamsim.r.RSwingConsole;
import org.jamsim.r.RUtil;
import org.omancode.util.ExecutionTimer;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

/**
 * Exposes scape specific R functionality for a particular scape, as well as
 * general R functionality.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ScapeRInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2117340899268748864L;

	/**
	 * Default dataframe replacement symbol to use if none specified in
	 * constructor.
	 */
	public static final String DEFAULT_DF_SYMBOL = "DATAFRAME";

	/**
	 * Dataframe replacement symbol.
	 */
	private final String dataFrameSymbol;

	/**
	 * Flag to keep the dataframes from each run in R. This means creating each
	 * new dataframe with a unique name.
	 */
	private final boolean keepAllRunDFs;

	/**
	 * R interface.
	 */
	private final transient RInterfaceHL rInterface;

	/**
	 * R console.
	 */
	private final RSwingConsole rConsole;

	private final MicroSimScape<?> msScape;

	private final ExecutionTimer timer = new ExecutionTimer();

	/**
	 * Expose statically for {@link AscapeGD}.
	 */
	public static ScapeRInterface LAST_INSTANCE;

	/**
	 * Get the scape. For {@link AscapeGD}.
	 * 
	 * @return micro simulation scape
	 */
	public MicroSimScape<?> getMsScape() {
		return msScape;
	}

	/**
	 * Constructor.
	 * 
	 * @param rLoader
	 *            R loader
	 * @param msScape
	 *            micro simulation scape
	 * @param dataFrameSymbol
	 *            replacement symbol. When evaluating commands that reference
	 *            the scape dataframe, this symbol is searched for and replaced
	 *            with the current run's dataframe name.
	 * @param keepAllRunDFs
	 *            flag to keep the dataframes from each run in R. This means
	 *            creating each new dataframe with a unique name.
	 */
	public ScapeRInterface(RLoader rLoader, MicroSimScape<?> msScape,
			String dataFrameSymbol, boolean keepAllRunDFs) {
		LAST_INSTANCE = this;
		this.dataFrameSymbol = dataFrameSymbol;
		this.msScape = msScape;
		this.keepAllRunDFs = keepAllRunDFs;

		this.rConsole = rLoader.getRConsole();
		this.rInterface = rLoader.getRInterface();

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
		try {
			rInterface.parseEvalTry(file);
		} catch (RInterfaceException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Create a dataframe from the scape.
	 * 
	 * @param runNumber
	 *            run number. Used in the naming of the dataframe
	 * @throws RInterfaceException
	 *             if problem during creation
	 */
	public void assignScapeDataFrame(int runNumber)
			throws RInterfaceException {
		String dataframeName = getScapeDFRunName(runNumber);

		timer.start();

		assignDataFrame(dataframeName, msScape, msScape.getPrototypeAgent()
				.getClass().getSuperclass());
		rInterface.printlnToConsole("Created dataframe " + dataframeName);

		timer.stop();

		System.out.println("Created dataframe " + dataframeName + " ("
				+ timer.duration() + " ms)");

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
	 * Return the name of the scape dataframe created for the given run number.
	 * 
	 * @param run
	 *            run number
	 * @return dataframe name
	 */
	public String getScapeDFRunName(int run) {
		String dfName =
				msScape.getName().toLowerCase() + (keepAllRunDFs ? run : "");
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
	 * Wraps a parse and try around an eval. The parse will generate syntax
	 * error messages, and the try will catch parse and evaluation errors and
	 * return them in the exception as opposed to printing it on the console.
	 * 
	 * @param expr
	 *            expression to try and parse and eval
	 * @return REXP result of the evaluation.
	 * @throws RInterfaceException
	 *             if there is a parse or evaluation error the error message is
	 *             returned in the exception. Nothing printed to the console.
	 */
	public REXP parseEvalTry(String expr) throws RInterfaceException {
		return rInterface.parseEvalTry(expr);
	}

	/**
	 * Display R help for given expression.
	 * 
	 * @param expr
	 *            expression. If {@code ""} displays help contents
	 */
	public void help(String expr) {
		try {
			REXP rexp =
					rInterface
							.parseEvalPrint("help(\"" + expr.trim() + "\")");

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
	public final void printPrompt() {
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
		parseEvalPrint(expr);
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
			rInterface.printlnToConsole(msg);
		} catch (RInterfaceException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Loads a package. If it isn't installed, it is loaded from CRAN.
	 * 
	 * @param pack
	 *            package name
	 * @throws RInterfaceException
	 *             if problem loading package.
	 */
	public final void loadPackage(String pack) throws RInterfaceException {
		rInterface.loadPackage(pack);
	}

}