package org.jamsim.ascape.r;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.data.model.CMarkedUpRowBean;

import org.apache.commons.lang.ArrayUtils;
import org.jamsim.ascape.DataDictionary;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.navigator.MicroSimScapeNode;
import org.omancode.math.NamedNumber;
import org.omancode.r.RFace;
import org.omancode.r.RFaceException;
import org.omancode.r.RUtil;
import org.omancode.r.types.RDataFrame;
import org.omancode.r.types.REXPUtil;
import org.omancode.r.types.RMatrix;
import org.omancode.r.types.RVectorList;
import org.omancode.r.types.UnsupportedTypeException;
import org.omancode.r.ui.RObjectTreeBuilder;
import org.omancode.r.ui.RSwingConsole;
import org.omancode.util.ExecutionTimer;
import org.omancode.util.StringUtil;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;

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
	 * Support file containing Common R functions.
	 */
	private static final String COMMON_R = "Common.r";

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
	private final transient RFace rInterface;

	/**
	 * R console.
	 */
	private final RSwingConsole rConsole;

	private final MicroSimScape<?> msscape;

	private final ExecutionTimer timer = new ExecutionTimer();

	private String baseFileUpdateCmd;

	/**
	 * Stored statically for use by R and {@link AscapeGD}.
	 */
	private static ScapeRInterface lastInstance;

	/**
	 * Get the last (i.e: most recently created) {@link ScapeRInterface}'s
	 * {@link MicroSimScape}'s {@link MicroSimScapeNode}.
	 * 
	 * This is exposed statically so it can be accessed from {@link AscapeGD}
	 * and R code to add navigator nodes from R.
	 * 
	 * @return the most recently created {@link MicroSimScape}.
	 */
	public static MicroSimScapeNode getLastMsScapeNode() {
		return lastInstance.getMsScape().getScapeNode();
	}

	/**
	 * Get the scape. For {@link AscapeGD}.
	 * 
	 * @return micro simulation scape
	 */
	public MicroSimScape<?> getMsScape() {
		return msscape;
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
		this.dataFrameSymbol = dataFrameSymbol;
		this.msscape = msScape;
		this.keepAllRunDFs = keepAllRunDFs;

		this.rConsole = rLoader.getRConsole();
		this.rInterface = rLoader.getRInterface();

		lastInstance = this;
	}

	/**
	 * Evaluate the contents of a file in R with a "loading" message printed to
	 * the console. The file is evaluated relative to the folder it is in.
	 * 
	 * @param file
	 *            file containing R commands
	 * @throws IOException
	 *             if problem reading or evaluating file
	 */
	public void loadRFile(File file) throws IOException {
		if (file != null) {
			rInterface.printlnToConsole("Loading " + file.getCanonicalPath());

			// rInterface.parseEvalPrint(RUtil.readRFile(file));
			rInterface.loadFile(file);
		}
	}

	/**
	 * Load the common R functions.
	 * 
	 * @throws IOException
	 *             if problem loading or evaluating functions.
	 */
	public void loadCommonRFunctions() throws IOException {
		loadRResource(COMMON_R);
	}

	/**
	 * Evaluate the contents of a resource in R.
	 * 
	 * @param resourceName
	 *            name of resource
	 * @throws IOException
	 *             if problem loading or evaluating the resource.
	 */
	private void loadRResource(String resourceName) throws IOException {
		rInterface.printlnToConsole("Loading resource " + resourceName);

		InputStream ins = getClass().getResourceAsStream(resourceName);
		if (ins == null) {
			throw new IOException(resourceName + " not found on classpath.");
		}
		rInterface.parseEvalPrint(RUtil.readRStream(ins));

	}

	/**
	 * Set the working directory. NB: this sets the working directory in not
	 * just R, but the java application environment.
	 * 
	 * @param dir
	 *            working directory
	 * @throws RFaceException
	 *             if problem setting directory
	 */
	public void setWd(String dir) throws RFaceException {
		rInterface.setWd(dir);
	}

	/**
	 * Get the working directory.
	 * 
	 * @return working directory
	 * @throws RFaceException
	 *             if problem getting directory
	 */
	public String getWd() throws RFaceException {
		return rInterface.getWd();
	}

	/**
	 * Define the R command that is called by {@link #baseFileUpdated()}.
	 * 
	 * @param cmd
	 *            base file update command, or {@code null} if none
	 */
	public void setBaseFileUpdateCmd(String cmd) {
		this.baseFileUpdateCmd = cmd;
	}

	/**
	 * Create a hash in R from a {@link Map}. Requires the {@code hash} R
	 * package to have been loaded.
	 * 
	 * @param name
	 *            name of hash
	 * @param map
	 *            map to write out as hash
	 * @throws RFaceException
	 *             if problem creating hash
	 */
	public void assignHash(String name, Map<String, ?> map)
			throws RFaceException {
		rInterface.assignHash(name, map);
	}

	/**
	 * Create an R expression in R as an object so it can be referenced (in the
	 * global environment).
	 * 
	 * @param name
	 *            symbol name
	 * @param rexp
	 *            r expression
	 * @throws RFaceException
	 *             if problem assigning
	 */
	public void assign(String name, REXP rexp) throws RFaceException {
		rInterface.assign(name, rexp);
	}

	/**
	 * Assign a source expression to a variable using the assignment operator
	 * <-.
	 * 
	 * @param x
	 *            destination variable name
	 * @param value
	 *            source expression
	 * @throws RFaceException
	 *             if problem assigning
	 */
	public void assign(String x, String value) throws RFaceException {
		rInterface.assign(x, value);
	}

	/**
	 * Create a dataframe from the scape. If the scape has no members, silently
	 * does nothing.
	 * 
	 * @param runNumber
	 *            run number. Used in the naming of the dataframe
	 * @throws RFaceException
	 *             if problem during creation
	 */
	@SuppressWarnings("unchecked")
	public void assignScapeDataFrame(int runNumber) throws RFaceException {
		String dataframeName = getScapeDFRunName(runNumber);

		if (msscape.size() > 0) {
			timer.start();

			assignDataFrame(dataframeName, msscape, msscape
					.getPrototypeAgent().getClass().getSuperclass());
			rInterface.printlnToConsole("Created dataframe " + dataframeName);

			timer.stop();

			System.out.println("Created dataframe " + dataframeName + " ("
					+ timer.duration() + " ms)");

			if (runNumber == 0) {
				baseFileUpdated();
			}
		}
	}

	/**
	 * Execute the base file update command. Call this when you have changed
	 * variables and after writing the scape to a dataframe.
	 */
	public void baseFileUpdated() {
		if (baseFileUpdateCmd != null) {
			parseEvalPrint(baseFileUpdateCmd);
			System.out.println("Executed " + baseFileUpdateCmd);
			printPrompt();
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
	 * Return the name of the scape dataframe created for the given run number.
	 * 
	 * @param run
	 *            run number
	 * @return dataframe name
	 */
	public String getScapeDFRunName(int run) {
		String dfName =
				msscape.getName().toLowerCase() + (keepAllRunDFs ? run : "");
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
	 * @throws RFaceException
	 *             if Collection cannot be read, or dataframe cannot be created.
	 */
	public void assignDataFrame(String name,
			Collection<? extends CMarkedUpRowBean> col, Class<?> stopClass)
			throws RFaceException {
		try {
			RList rlist =
					new RVectorList(col, stopClass).addCMarkedUpRow(col)
							.asRList();

			rInterface.assignDataFrame(name, rlist);
		} catch (IntrospectionException e) {
			throw new RFaceException(e.getMessage(), e);
		}
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
	 * @throws RFaceException
	 *             if Collection cannot be read, or dataframe cannot be created.
	 */
	public void assignDataFrame(String name, CDataCacheContainer container)
			throws RFaceException {
		rInterface
				.assignDataFrame(name, new RVectorList(container).asRList());
		// msScape.addDataFrameNode(name);
	}

	/**
	 * Create a matrix in R from the given casper dataset. All elements of the
	 * casper dataset must be of the same type.
	 * 
	 * @param name
	 *            the name of the dataframe to create in R.
	 * @param container
	 *            the casper container to convert.
	 * @throws RFaceException
	 *             if Collection cannot be read, or the matrix cannot be
	 *             created.
	 */
	public void assignMatrix(String name, CDataCacheContainer container)
			throws RFaceException {
		rInterface.assignMatrix(name, new RVectorList(container).asRList());
		// msScape.addDataFrameNode(name);
	}

	/**
	 * Evaluate a String expression in R in the global environment. See
	 * {@link RFace#eval(String)}.
	 * 
	 * @param expr
	 *            expression to evaluate.
	 * @return REXP result of the evaluation.
	 * @throws RFaceException
	 *             if problem during parse or evaluation. Parse errors will
	 *             simply return the message "parse error".
	 */
	public REXP eval(String expr) throws RFaceException {
		return rInterface.eval(expr);
	}

	/**
	 * Evaluate an expression printing to the console any errors (including
	 * syntactic) and the expression if it is visible. See
	 * {@link RFace#parseEvalPrint(String)}.
	 * 
	 * @param expr
	 *            expression to evaluate
	 * @return evaluated result
	 */
	public REXP parseEvalPrint(String expr) {
		return rInterface.parseEvalPrint(expr);
	}

	/**
	 * Run {@link #rcmdReplace(String, int)} on the rCommand then
	 * {@link #parseEvalPrint(String)}. Output to log when execution has
	 * finished.
	 * 
	 * @param rCommand
	 *            R command
	 * @param runNumber
	 *            run number
	 * @return evaluated result
	 */
	public REXP parseEvalPrintLogReplace(String rCommand, int runNumber) {

		timer.start();

		String rcmd = rcmdReplace(rCommand, runNumber);

		REXP result = parseEvalPrint(rcmd);

		timer.stop();

		System.out.println("Executed " + rcmd + " (" + timer.duration()
				+ " ms)");

		return result;
	}

	/**
	 * Wraps a parse and try around an eval. The parse will generate syntax
	 * error messages, and the try will catch parse and evaluation errors and
	 * return them in the exception as opposed to printing it on the console.
	 * 
	 * @param expr
	 *            expression to try and parse and eval
	 * @return REXP result of the evaluation.
	 * @throws RFaceException
	 *             if there is a parse or evaluation error the error message is
	 *             returned in the exception. Nothing printed to the console.
	 */
	public REXP parseEvalTry(String expr) throws RFaceException {
		return rInterface.parseEvalTry(expr);
	}

	/**
	 * Calls {@link RFace#parseEvalTryReturnRMatrix(String)}.
	 * 
	 * @param expr
	 *            expression
	 * @return {@link RMatrix}
	 * @throws RFaceException
	 *             if problem evaluating {@code expr}, including if {@code expr}
	 *             does not return an expression that can be represented as a
	 *             {@link RMatrix}.
	 */
	public RMatrix parseEvalTryReturnRMatrix(String expr)
			throws RFaceException {
		return rInterface.parseEvalTryReturnRMatrix(expr);
	}

	/**
	 * Evaluates {@code expr} and returns an array of {@link NamedNumber}.
	 * 
	 * @param expr
	 *            expression
	 * @return named number array
	 * @throws RFaceException
	 *             if problem evaluating {@code expr}, including if {@code expr}
	 *             does not return a {@link REXPDouble} or
	 *             {@link org.rosuda.REngine.REXPInteger}.
	 */
	public NamedNumber[] parseEvalTryReturnNamedNumber(String expr)
			throws RFaceException {
		return rInterface.parseEvalTryReturnNamedNumber(expr);
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
	 * @throws RFaceException
	 *             if problem during parse or evaluation. Parse errors will
	 *             simply return the message "parse error".
	 */
	public String evalCaptureOutput(String expr) throws RFaceException {
		return rInterface.evalCaptureOutput(expr);
	}

	/**
	 * Evaluate expression returning a String array. See
	 * {@link RFace#evalReturnStrings(String)}.
	 * 
	 * @param expr
	 *            expression to evaluate.
	 * @return REXP result of the evaluation.
	 */
	public String[] evalReturnStrings(String expr) {
		try {
			return rInterface.evalReturnStrings(expr);
		} catch (RFaceException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Evaluate expression that returns a character vector. Returns the
	 * character vector as a String. See {@link RFace#evalReturnString(String)}.
	 * 
	 * @param expr
	 *            expression to evaluate.
	 * @return REXP result of the evaluation.
	 */
	public String evalReturnString(String expr) {
		try {
			return rInterface.evalReturnString(expr);
		} catch (RFaceException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Print a message out to the R console (no line feed).
	 * 
	 * @param msg
	 *            message to print.
	 */
	public void printToConsole(String msg) {
		try {
			rInterface.printToConsole(msg);
		} catch (RFaceException e) {
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
		} catch (RFaceException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Loads a package. If it isn't installed, it is loaded from CRAN.
	 * 
	 * @param pack
	 *            package name
	 * @throws RFaceException
	 *             if problem loading package.
	 */
	public final void loadPackage(String pack) throws RFaceException {
		rInterface.loadPackage(pack);
	}

	/**
	 * Execute the r command "meanOfRuns" on the supplied dataset. First saves
	 * the dataset as a dataframe called {@code dfName} then executes
	 * "meanOfRuns".
	 * 
	 * @param allRuns
	 *            dataset from all runs. The first variable is the row name and
	 *            subsequent variables are run values for each row, eg:
	 * 
	 *            Category Run 1 Run 2
	 * 
	 *            1 0.0039392527 4.189704e-03
	 * 
	 *            2 0.0052892006 5.554406e-03
	 * 
	 *            3 0.0500477200 4.921984e-02
	 * 
	 *            4 0.0061327012 6.273054e-03
	 * @param dfName
	 *            dataframe name to create
	 * @param dfDesc
	 *            dataframe description
	 * @return the original dataset plus the additional variables: Mean, Err,
	 *         Left, Right
	 * @throws RFaceException
	 *             if problem creating dataframe or executing R command
	 */
	public CDataCacheContainer meanOfRuns(CDataCacheContainer allRuns,
			String dfName, String dfDesc) throws RFaceException {

		// save multi run dataset to R frame
		assignDataFrame(dfName, allRuns);
		printlnToConsole("Created multi-run dataframe " + dfName + "("
				+ dfDesc + ")");

		String rcmd = dfName + " <- meanOfRuns(" + dfName + ")";
		try {
			// execute meanOfRuns on multi run dataframe
			parseEvalTry(rcmd);
			REXP rexp = parseEvalTry(dfName);
			RDataFrame df = new RDataFrame(allRuns.getCacheName(), rexp);

			return new CDataCacheContainer(df);

		} catch (CDataGridException e) {
			throw new RFaceException(e);
		} catch (UnsupportedTypeException e) {
			throw new RFaceException(e);
		}
	}

	/**
	 * Calls updateScenarioWeights in R. Updates the "weightScenario" variable
	 * in the basefile based on the desired proportions ({@code props}) for
	 * factor {@code factorName}.
	 * 
	 * @param basefileName
	 *            name of base file
	 * @param factorName
	 *            name of factor variable
	 * @param props
	 *            proportions
	 * @throws RFaceException
	 *             if problem calling.
	 */
	public void updateScenarioWeights(String basefileName, String factorName,
			double[] props) throws RFaceException {

		REXPDouble rprops = REXPUtil.toVector(props);
		rInterface.assign(".desiredProp", rprops);

		// construct funtion call string
		// eg: children <- updateScenarioWeights(children, "SESBTH",
		// c(0.2,0.3,0.5))
		String rcmdinner =
				StringUtil.functionCall("updateScenarioWeights",
						basefileName, StringUtil.doublequote(factorName),
						".desiredProp");

		// assign(".scape", scape, envir = .GlobalEnv)
		String rcmd =
				StringUtil.functionCall("assign",
						StringUtil.doublequote(basefileName), rcmdinner,
						"envir = .GlobalEnv");

		parseEvalTry(rcmd);

		printlnToConsole("Updated scenario weights: " + factorName + " "
				+ ArrayUtils.toString(props));

		baseFileUpdated();
	}

	/**
	 * Creates a new R object tree builder with the set of R objects present in
	 * the global environment at time of creation.
	 * 
	 * @param includeClasses
	 *            specify the classes of object to display, or {@code null} to
	 *            display objects of any class.
	 * 
	 * @return R object tree builder.
	 * @throws RFaceException
	 *             if problem during interrogation of R environment
	 */
	public RObjectTreeBuilder createRObjectTreeBuilder(String[] includeClasses)
			throws RFaceException {
		/*
		 * return new RObjectTreeBuilder(rInterface);
		 */

		return new RObjectTreeBuilder(rInterface, getMsScape().getName()
				.toLowerCase(), includeClasses);

	}
}