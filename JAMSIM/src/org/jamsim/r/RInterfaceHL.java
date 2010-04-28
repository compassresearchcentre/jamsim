package org.jamsim.r;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.casper.io.file.util.ArrayUtil;

import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.JRI.JRIEngine;

/**
 * General purpose interface to the R high level engine. Provides
 * <ul>
 * <li>Single instance, because R is single threaded.
 * <li>Assignment of R objects.
 * <li>Parsing and evaluation of commands in R in multiple ways (
 * {@link #eval(String)}, {@link #parseEvalPrint(String)},
 * {@link #parseEvalTry(String)}).
 * 
 * <li>Message output to the R console.
 * </ul>
 * 
 * Uses the org.rosuda.REngine interface, which prevents re-entrance and has
 * Java side objects that represent R data structures, rather than the lower
 * level org.rosuda.JRI interface which doesn't have these features.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class RInterfaceHL {

	/**
	 * Support file containing R functions to load into R environment on
	 * startup.
	 */
	private static final String SUPPORT_FILE = "RInterfaceHL.r";

	/**
	 * Private constructor prevents instantiation from other classes.
	 */
	private RInterfaceHL() {
	}

	/**
	 * R REPL (read-evaluate-parse loop) handler.
	 */
	private static RMainLoopCallbacks rloopHandler = null;

	/**
	 * SingletonHolder is loaded, and the static initializer executed, on the
	 * first execution of Singleton.getInstance() or the first access to
	 * SingletonHolder.INSTANCE, not before.
	 */
	private static final class SingletonHolder {

		/**
		 * Singleton instance, with static initializer.
		 */
		private static final RInterfaceHL INSTANCE = initRInterfaceHL();

		/**
		 * Initialize RInterfaceHL singleton instance using rLoopHandler from
		 * outer class.
		 * 
		 * @return RInterfaceHL instance
		 */
		private static RInterfaceHL initRInterfaceHL() {
			try {
				return new RInterfaceHL(rloopHandler); // NOPMD
			} catch (RInterfaceException e) {
				// a static initializer cannot throw exceptions
				// but it can throw an ExceptionInInitializerError
				throw new ExceptionInInitializerError(e);
			}
		}

		/**
		 * Prevent instantiation.
		 */
		private SingletonHolder() {
		}

		/**
		 * Get singleton RInterfaceHL.
		 * 
		 * @return RInterfaceHL singleton.
		 */
		public static RInterfaceHL getInstance() {
			return SingletonHolder.INSTANCE;
		}

	}

	/**
	 * Return the singleton instance of RInterfaceHL. Only the first call to
	 * this will establish the rloopHandler.
	 * 
	 * @param rloopHandler
	 *            R REPL handler supplied by client.
	 * @return RInterfaceHL singleton instance
	 * @throws RInterfaceException
	 *             if REngine cannot be created
	 */
	public static RInterfaceHL getInstance(RMainLoopCallbacks rloopHandler)
			throws RInterfaceException {
		RInterfaceHL.rloopHandler = rloopHandler;

		try {
			return SingletonHolder.getInstance();
		} catch (ExceptionInInitializerError e) {

			// re-throw exception that occurred in the initializer
			// so our caller can deal with it
			Throwable exceptionInInit = e.getCause();
			throw new RInterfaceException(exceptionInInit); // NOPMD
		}
	}

	/**
	 * org.rosuda.REngine.REngine high level R interface.
	 */
	private REngine rosudaEngine = null;

	/**
	 * Construct new RInterfaceHL. Only ever gets called once by
	 * {@link SingletonHolder.initRInterfaceHL}.
	 * 
	 * @param rloopHandler
	 *            R REPL handler supplied by client.
	 * @throws REngineException
	 *             if R cannot be loaded.
	 * @throws RInterfaceException
	 */
	private RInterfaceHL(RMainLoopCallbacks rloopHandler)
			throws RInterfaceException {

		// tell Rengine code not to die if it can't
		// load the JRI native DLLs. This allows
		// us to catch the UnsatisfiedLinkError
		// ourselves
		System.setProperty("jri.ignore.ule", "yes");

		try {
			rosudaEngine =
					new JRIEngine(new String[] { "--no-save" }, rloopHandler);

			loadRSupportFunctions();

		} catch (REngineException e) {
			throw new RInterfaceException(e);
		} catch (IOException e) {
			throw new RInterfaceException(e);
		}

	}

	/**
	 * Load support functions from support file.
	 * 
	 * @throws RInterfaceException
	 *             if problem loading file
	 * @throws IOException
	 *             if problem loading file
	 */
	private void loadRSupportFunctions() throws RInterfaceException,
			IOException {
		InputStream ins = getClass().getResourceAsStream(SUPPORT_FILE);

		eval(RUtil.readRStream(ins));
	}

	/**
	 * Evaluate an expression in R in the global environment. Does not
	 * explicitly call "parse" so any syntax errors will result in the unhelpful
	 * "parse error" exception message. Any evaluation errors will have the
	 * error messages printed to the console and return the exception message
	 * "error during evaluation". Unlike, the REPL and
	 * {@link #parseEvalPrint(String)}, does not print the result of the
	 * expression (ie: does just the E part of the REPL). Any prints within the
	 * expression or functions called by the expression will print. Warnings
	 * produced by the expression will not be printed.
	 * 
	 * @param expr
	 *            expression to evaluate.
	 * @return REXP result of the evaluation. Not printed to the console.
	 * @throws RInterfaceException
	 *             if problem during parse or evaluation. Parse errors will
	 *             simply return the message "parse error". Any evaluation
	 *             errors with have the error messages printed to the console
	 *             and return the exception message "error during evaluation".
	 */
	public REXP eval(String expr) throws RInterfaceException {
		if (!initialized()) {
			throw new IllegalStateException(
					"REngine has not been initialized.");
		}

		try {
			return rosudaEngine.parseAndEval(expr);
		} catch (REngineException e) {
			throw new RInterfaceException(e.getMessage(), e);
		} catch (REXPMismatchException e) {
			throw new RInterfaceException(e.getMessage(), e);
		}
	}

	/**
	 * Evaluate an expression and test that it returns a {@link REXPString}.
	 * 
	 * @param expr
	 *            expression to evaluate.
	 * @return REXP result of the evaluation.
	 * @throws RInterfaceException
	 *             if problem during parse or evaluation, or expression does not
	 *             return a {@link REXPString}.
	 */
	public String[] evalReturnString(String expr) throws RInterfaceException {
		try {
			REXP rexp = rosudaEngine.parseAndEval(expr);

			// r command must return a REXPString
			if (!(rexp instanceof REXPString)) {
				throw new RInterfaceException(expr + " returned "
						+ rexp.getClass().getCanonicalName()
						+ " instead of REXPString");
			}

			return rexp.asStrings();
		} catch (REXPMismatchException e) {
			throw new RInterfaceException(e.getMessage(), e);
		} catch (REngineException e) {
			throw new RInterfaceException(e.getMessage(), e);
		}
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
		if (!initialized()) {
			throw new IllegalStateException(
					"REngine has not been initialized.");
		}

		try {

			/**
			 * Place the expression in a character vector, syntax errors and
			 * all. If we tried to execute the expression directly we might run
			 * into syntax errors that wouldn't be trapped by try.
			 */
			rosudaEngine.assign(".expression.", expr);

			/**
			 * parse: converts a file, or character vector, into an expression
			 * object but doesn't evaluate it. If there is a problem parsing,
			 * because of syntax error, it will print a detailed message. This
			 * message can be captured by "try".
			 * 
			 * eval: evaluates an expression object
			 * 
			 * try: returns a "try-error" object with the contents of the error
			 * text, or if no error returns the evaluated expression's return
			 * object (if it has one).
			 * 
			 */
			String exec = "try(eval(parse(text=.expression.)), silent=TRUE)";

			REXP rexp = rosudaEngine.parseAndEval(exec);

			if (rexp == null) {
				// evaluated OK and returned nothing
				return null;
			} else if (rexp.inherits("try-error")) {
				// evaluated with error and returned "try-error" object which
				// contains error message
				throw new RInterfaceException(rexp.asString());
			} else {
				// evaluated OK and returned object
				return rexp;
			}

		} catch (REngineException e) {
			throw new RInterfaceException(e.getMessage(), e);
		} catch (REXPMismatchException e) {
			throw new RInterfaceException(e.getMessage(), e);
		}
	}

	/**
	 * Wraps a parse around an eval and prints (shows) result. Returns the
	 * expression result AND prints it to the console if it is visible, ie: the
	 * REP parts of the REPL. Errors & warnings are output to the console, ie:
	 * doesn't produce Java exceptions for expression errors. Uses R global
	 * environment.
	 * 
	 * @param expr
	 *            expression to parse, eval and show
	 * @return REXP result of the evaluation. Also printed to the console if
	 *         visible. Returns {@code null} if there was an exception generated
	 *         whilst evaluating the expression.
	 */
	public REXP parseEvalPrint(String expr) {
		if (!initialized()) {
			throw new IllegalStateException(
					"REngine has not been initialized.");
		}

		try {

			/**
			 * Place the expression in a character vector, syntax errors and
			 * all. If we tried to execute the expression directly we might run
			 * into syntax errors in the executing statement.
			 */
			rosudaEngine.assign(".expression.", expr);

			String exec = ".pep(.expression.)";
			// String exec = expr;

			REXP result = rosudaEngine.parseAndEval(exec);

			return result;

		} catch (REngineException e) {
			// swallow! error message will be printed to the console
			return null;
		} catch (REXPMismatchException e) {
			// swallow! error message will be printed to the console
			return null;
		}
	}

	/**
	 * Parse and evaluate a text file in R. Calls {@link #parseEvalTry(File)}.
	 * Throws exception if problem reading the file.
	 * 
	 * @param file
	 *            text file to evaluate in R
	 * @return REXP result of the evaluation.
	 * @throws RInterfaceException
	 *             if problem during evaluation. See
	 *             {@link #parseEvalTry(String)}.
	 * @throws IOException
	 *             if file cannot be read.
	 */
	public REXP parseEvalTry(File file) throws IOException,
			RInterfaceException {
		try {
			return parseEvalTry(RUtil.readRFile(file));
		} catch (RInterfaceException e) {
			throw new RInterfaceException(file.getCanonicalPath() + " "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Check if R engine has been loaded/initialized.
	 * 
	 * @return true if R engine has been loaded/initialized.
	 */
	public boolean initialized() {
		return rosudaEngine != null;
	}

	/**
	 * Print a message out to the R console.
	 * 
	 * @param msg
	 *            message to print.
	 * @throws RInterfaceException
	 *             if problem during evaluation.
	 */
	public void printToConsole(String msg) throws RInterfaceException {
		rloopHandler.rWriteConsole(null, msg, 0);
		// parseAndEval("cat('" + msg + "')");
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
		printToConsole(msg + "\n");
	}

	/**
	 * Create an R expression in R as an object so it can be referenced (in the
	 * global environment).
	 * 
	 * @param name
	 *            symbol name
	 * @param rexp
	 *            r expression
	 * @throws RInterfaceException
	 *             if problem assigning
	 */
	public void assign(String name, REXP rexp) throws RInterfaceException {
		try {
			rosudaEngine.assign(name, rexp);
		} catch (REngineException e) {
			throw new RInterfaceException(e);
		} catch (REXPMismatchException e) {
			throw new RInterfaceException(e);
		}
	}

	/**
	 * Create an {@link RList} in R as a dataframe.
	 * 
	 * @param name
	 *            the name of the dataframe to create in R.
	 * @param rlist
	 *            the rlist
	 * @throws RInterfaceException
	 *             if problem assigning list
	 */
	public void assignDataFrame(String name, RList rlist)
			throws RInterfaceException {
		try {
			// turn the rlist into a dataframe
			REXP dataframe = REXP.createDataFrame(rlist);

			// assign the dataframe to a named R object
			rosudaEngine.assign(name, dataframe);
		} catch (REngineException e) {
			throw new RInterfaceException(e);
		} catch (REXPMismatchException e) {
			throw new RInterfaceException(e);
		}
	}

	/**
	 * Get R_DEFAULT_PACKAGES.
	 * 
	 * @return default packages
	 * @throws RInterfaceException
	 *             if problem during command evaluation.
	 */
	public String getCurrentPackages() throws RInterfaceException {

		REXP availablePkgs;
		try {
			availablePkgs = eval(".packages(TRUE)");
			if (availablePkgs.isNull() || availablePkgs.asStrings() == null) {
				return "";
			} else {
				return ArrayUtil.toString(availablePkgs.asStrings());
			}
		} catch (REXPMismatchException e) {
			throw new RInterfaceException(e);
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
	public void loadPackage(String pack) throws RInterfaceException {
		String packages = getCurrentPackages();
		if (!packages.contains(pack)) {
			printlnToConsole("Package " + pack
					+ " not found. Attempting to download...");
			parseEvalPrint("install.packages('" + pack + "');library(" + pack
					+ ")");
		} else {
			printlnToConsole("Loading package: " + pack);
			parseEvalPrint("library(" + pack + ")");
		}
	}

}
