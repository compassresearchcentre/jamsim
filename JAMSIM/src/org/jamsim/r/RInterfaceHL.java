package org.jamsim.r;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import net.casper.data.model.CDataCacheContainer;
import net.casper.io.file.util.ArrayUtil;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
 * <li>single instance, because R is single threaded.
 * <li>Conversion of Collection to R dataframe.
 * <li>Conversion of Casper dataset to R dataframe.
 * <li>Parsing and evaluation of commands in R.
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
			} catch (REngineException e) {
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

		RInterfaceHL instance = null;

		try {
			instance = SingletonHolder.getInstance();
		} catch (ExceptionInInitializerError e) {

			// re-throw exception that occurred in the initializer
			// so our caller can deal with it
			Throwable exceptionInInit = e.getCause();
			throw new RInterfaceException(exceptionInInit); // NOPMD
		}

		return instance;
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
	 */
	private RInterfaceHL(RMainLoopCallbacks rloopHandler)
			throws REngineException {

		// tell Rengine code not to die if it can't
		// load the JRI native DLLs. This allows
		// us to catch the UnsatisfiedLinkError
		// ourselves
		System.setProperty("jri.ignore.ule", "yes");

		rosudaEngine =
				new JRIEngine(new String[] { "--no-save" }, rloopHandler);
	}

	/**
	 * Evaluate a text file in R in the global environment.
	 * 
	 * @param file
	 *            text file to evaluate in R
	 * @return REXP result of the evaluation.
	 * @throws RInterfaceException
	 *             if problem during evaluation.
	 * @throws IOException
	 *             if file cannot be read.
	 */
	public REXP parseAndEval(File file) throws IOException,
			RInterfaceException {
		FileReader fr = new FileReader(file);
		String expr = IOUtils.toString(fr);

		// release file after loading,
		// instead of waiting for VM exit/garbage collection
		fr.close();

		// strip "\r" otherwise we will get parse errors
		expr = StringUtils.remove(expr, "\r");

		try {
			return tryParseAndEval(expr);
		} catch (RInterfaceException e) {
			throw new RInterfaceException(file.getCanonicalPath() + " "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Evaluate an expression in R in the global environment.
	 * 
	 * @param expr
	 *            expression to evaluate.
	 * @return REXP result of the evaluation.
	 * @throws RInterfaceException
	 *             if problem during parse or evaluation. Parse errors will
	 *             simply return the message "parse error".
	 */
	public REXP parseAndEval(String expr) throws RInterfaceException {
		if (!initialized()) {
			throw new RInterfaceException("REngine has not been initialized.");
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
	public String[] parseAndEvalStringVector(String expr)
			throws RInterfaceException {
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
	 * Wraps a try around a parse and eval. This will catch parse and evaluation
	 * errors and return the error message in the exception.
	 * 
	 * @param expr
	 *            expression to try and parse and eval
	 * @return REXP result of the evaluation.
	 * @throws RInterfaceException
	 *             if there is a parse or evaluation error the error message is
	 *             returned in the exception
	 */
	public REXP tryParseAndEval(String expr) throws RInterfaceException {
		if (!initialized()) {
			throw new RInterfaceException("REngine has not been initialized.");
		}

		try {

			rosudaEngine.assign(".tmp.", expr);
			REXP r =
					rosudaEngine
							.parseAndEval("try(eval(parse(text=.tmp.)),silent=TRUE)");
			if (r == null) {
				// evaluated OK and returned nothing
				return null;
			} else if (r.inherits("try-error")) {
				// evaluated with error and returned "try-error" object which
				// contains error message
				throw new RInterfaceException(r.asString());
			} else {
				// evaluated OK and returned object
				return r;
			}

		} catch (REngineException e) {
			throw new RInterfaceException(e.getMessage(), e);
		} catch (REXPMismatchException e) {
			throw new RInterfaceException(e.getMessage(), e);
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
		assignDataFrame(name, RUtil.toRList(col, stopClass));
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
		assignDataFrame(name, RUtil.toRList(container));
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

	public double evalMean(double[] array) throws RInterfaceException {
		assign(".tmp.evalMean", RUtil.toVector(array));
		REXP result = parseAndEval("mean(.tmp.evalMean)");
		try {
			return result.asDouble();
		} catch (REXPMismatchException e) {
			throw new RInterfaceException(e);
		}
	}

	public double evalMeanError(double[] array) throws RInterfaceException {
		assign(".tmp.evalMeanError", RUtil.toVector(array));
		REXP result =
				parseAndEval("qt(0.975,df=length(.tmp.evalMeanError)-1)*sd(.tmp.evalMeanError)/sqrt(length(.tmp.evalMeanError))");
		try {
			return result.asDouble();
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
			availablePkgs = parseAndEval(".packages(TRUE)");
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
	 *             if problem during command evaluation.
	 */
	public void loadPackage(String pack) throws RInterfaceException {
		String packages = getCurrentPackages();
		if (!packages.contains(pack)) {
			printlnToConsole("Package " + pack
					+ " not found. Attempting to download...");
			parseAndEval("install.packages('" + pack + "');library(" + pack
					+ ")");
		} else {
			printlnToConsole("Loading package: " + pack);
			parseAndEval("library(" + pack + ")");
		}
	}

}
