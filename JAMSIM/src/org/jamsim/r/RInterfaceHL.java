package org.jamsim.r;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.casper.data.model.CDataGridException;
import net.casper.data.model.CDataRowSet;
import net.casper.data.model.CRowMetaData;
import net.casper.io.beans.util.BeanPropertyInspector;
import net.casper.io.file.util.ArrayUtil;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPNull;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REXPVector;
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
		String expr = IOUtils.toString(new FileReader(file));
		
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
		//parseAndEval("cat('" + msg + "')");
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

	public static REXPVector toVector(double[] array) {
		return new REXPDouble(array);
	}

	public static REXPVector toVector(int[] array) {
		return new REXPInteger(array);
	}

	public static REXPVector toVector(boolean[] array) {
		return new REXPLogical(array);
	}

	public static REXPVector toVector(String[] array) {
		return new REXPString(array);
	}

	public static REXP toVector(Object array) {
		if (array == null) {
			return new REXPNull();
		}

		Class<?> arrayClass = array.getClass();

		if (arrayClass == double[].class) {
			return new REXPDouble((double[]) array);
		} else if (arrayClass == int[].class) {
			return new REXPInteger((int[]) array);
		} else if (arrayClass == boolean[].class) {
			return new REXPLogical((boolean[]) array);
		} else if (arrayClass == String[].class) {
			return new REXPString((String[]) array);
		} else {
			throw new IllegalArgumentException("Cannot convert "
					+ arrayClass.getCanonicalName() + " to R object");
		}
	}

	/**
	 * Represents a vector of a single Java type. All elements of the vector
	 * must be of this type. Provides method to convert this to a REXPVector for
	 * use with R.
	 * 
	 * @author oman002
	 * 
	 */
	public static final class RVector {

		/**
		 * Vector name.
		 */
		private String name;

		/**
		 * List of values. This is a raw type so that any type of List can
		 * specified at runtime.
		 */
		@SuppressWarnings("unchecked")
		private List values;

		private boolean klassIsArray = false;

		/**
		 * Create a new RVector with given name, value type, a initial size.
		 * 
		 * @param name
		 *            vector name.
		 * @param klass
		 *            type of the values this vector will hold.
		 * @param initialSize
		 *            number of elements in this vector.
		 * @throws UnsupportedTypeException
		 *             if {@code klass} is of unsupported type.
		 */
		public RVector(String name, Class<?> klass, int initialSize)
				throws UnsupportedTypeException {
			this.name = name;
			setValues(klass, initialSize);
		}

		/**
		 * Create the ArrayList of the appropriate type for the given {@code
		 * klass}. Uses <a href="http://fastutil.dsi.unimi.it/">fastutil</a>
		 * primitive array collections for efficiency. When values are added
		 * using {@link #addValue(Object)} they are automatically unboxed and
		 * stored as primitives.
		 * 
		 * @param klass
		 *            type of the values this vector will hold.
		 * @param initialSize
		 *            number of elements in this vector.
		 * @throws UnsupportedTypeException
		 *             if {@code klass} is of unsupported type.
		 */
		private void setValues(Class<?> klass, int initialSize)
				throws UnsupportedTypeException {
			if (klass.isPrimitive() || klass == String.class) {
				if (klass == double.class) {
					values = new DoubleArrayList(initialSize);
				} else if (klass == int.class) {
					values = new IntArrayList(initialSize);
				} else if (klass == boolean.class) {
					values = new BooleanArrayList(initialSize);
				} else if (klass == String.class) {
					values = new ArrayList<String>(initialSize);
				} else if (klass == Float.class) {
					// no R type for float, use double instead
					values = new DoubleArrayList(initialSize);
				} else if (klass == long.class) {
					// no R type for long, use int instead
					// R doesn't have anything that can represent long natively,
					// so you have to convert it to either int (if 32-bit is
					// enough), double (giving you about 52-bits lossless
					// storage) or raw vector with 8-bytes for each long.
					// we'll use int
					values = new IntArrayList(initialSize);
				} else if (klass == char.class) {
					// no R type for char, use String instead
					values = new ArrayList<String>(initialSize);
				} else {
					throw new UnsupportedTypeException(
							"Cannot set value for column [" + this.toString()
									+ "]. Unsupported type "
									+ klass.getCanonicalName());
				}
			} else if (klass.isArray()) {
				klassIsArray = true;
				values = new RList(initialSize, false);
			} else {
				throw new UnsupportedTypeException(
						"Cannot set value for column [" + this.toString()
								+ "]. Unsupported type "
								+ klass.getCanonicalName());
			}

		}

		/**
		 * Add a single value to this vector. Automatically unboxes objects of
		 * primitive type and stores them in a primitive collection.
		 * 
		 * @param value
		 *            single value to add.
		 */
		@SuppressWarnings("unchecked")
		public void addValue(Object value) {
			// if its a Character, we are storing it
			// in a ArrayList<String> so we need to
			// convert it to a String first.
			if (value instanceof Character) {
				values.add(value.toString());
			} else if (klassIsArray) {
				// this must be an array object passed in, so wrap it in a
				// REXPVector
				values.add(toVector(value));
			} else {
				values.add(value);
			}
		}

		/**
		 * Get all values of this vector.
		 * 
		 * @return List containing this vector's vales.
		 */
		@SuppressWarnings("unchecked")
		public List getValues() {
			return values;
		}

		/**
		 * Get vector name.
		 * 
		 * @return vector name.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Set the vector name.
		 * 
		 * @param name
		 *            vector name.
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Convert this vector to an REXPVector.
		 * 
		 * @return an REXPVector.
		 * @throws UnsupportedTypeException
		 *             if the type of this vector's values is not supported in
		 *             R.
		 */
		public REXPVector getREXPVector() throws UnsupportedTypeException {

			// get primitives out of the list (using the primitive method)
			// and create an REXP from them.
			if (values instanceof DoubleArrayList) {
				return new REXPDouble(((DoubleArrayList) values).elements());
			} else if (values instanceof IntArrayList) {
				return new REXPInteger(((IntArrayList) values).elements());
			} else if (values instanceof BooleanArrayList) {
				return new REXPLogical(((BooleanArrayList) values).elements());
			} else if (values instanceof ArrayList<?>) {
				return new REXPString(((ArrayList<?>) values)
						.toArray(new String[values.size()]));
			} else if (values instanceof RList) {
				return new REXPGenericVector((RList) values);
			} else {
				throw new UnsupportedTypeException(
						"Cannot get R vector for column [" + this.toString()
								+ "]. " + "Unsupported backing list type "
								+ values.getClass().getCanonicalName());
			}

		}

		@Override
		public String toString() {
			if (name == null) {
				return super.toString();
			}
			return name;
		}
	}

	/**
	 * Create a dataframe in R from the given Collection. Introspection is used
	 * to determine the bean properties (i.e.: getter methods) that are exposed,
	 * and each one becomes a column in the dataframe. Columns are only created
	 * for primitive properties; object properties are ignored without warning.
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

		if (col.size() == 0) {
			throw new RInterfaceException(
					"Cannot create dataframe for empty collection \"" + name
							+ "\"");
		}

		Object bean = col.iterator().next();

		BeanPropertyInspector props;
		try {
			props = new BeanPropertyInspector(bean, stopClass);
		} catch (IntrospectionException e) {
			throw new RInterfaceException(e);
		}

		int numElements = col.size();

		// create a List of RVectors that hold an unknown type
		ArrayList<RVector> vectors = new ArrayList<RVector>();

		// create an RVector for each property
		// only properties of primitive types are included
		for (BeanPropertyInspector.Property prop : props) {
			Class<?> klass = prop.getPropertyType();
			if (klass.isPrimitive() || klass == String.class) {
				RVector vector;
				try {
					vector = new RVector(// NOPMD
							prop.getName(), klass, numElements);
				} catch (UnsupportedTypeException e) {
					throw new RInterfaceException(e);
				}
				vectors.add(vector);
			} else if (klass.isArray()) {
				RVector vector;
				try {
					vector = new RVector(// NOPMD
							prop.getName(), klass, numElements);
				} catch (UnsupportedTypeException e) {
					throw new RInterfaceException(e);
				}
				vectors.add(vector);

			}
		}

		// fill the RVectors' values row by row
		// from the bean's property values
		for (Object element : col) {
			for (RVector vector : vectors) {
				String propName = vector.getName();
				Object prop;
				try {
					prop = PropertyUtils.getProperty(element, propName);
				} catch (IllegalAccessException e) {
					throw new RInterfaceException("Oh no, "
							+ "couldn't get property [" + propName + "]", e);
				} catch (InvocationTargetException e) {
					throw new RInterfaceException("Oh no, "
							+ "couldn't get property [" + propName + "]", e);
				} catch (NoSuchMethodException e) {
					throw new RInterfaceException("Oh no, "
							+ "couldn't get property [" + propName + "]", e);
				}
				vector.addValue(prop);
			}
		}

		// create an rlist of REXPVectors from each RVector
		RList rlist = new RList(vectors.size(), true);
		for (RVector vector : vectors) {
			try {
				rlist.put(vector.getName(), vector.getREXPVector());
			} catch (UnsupportedTypeException e) {
				throw new RInterfaceException(e);
			}
		}

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
	 * Create a dataframe in R from the given casper dataset.
	 * 
	 * NB: doesn't automatically create factors like read.table does.
	 * 
	 * @param name
	 *            the name of the dataframe to create in R.
	 * @param cdrs
	 *            the casper datarowset to convert.
	 * @throws RInterfaceException
	 *             if Collection cannot be read, or dataframe cannot be created.
	 */
	public void assignDataFrame(String name, CDataRowSet cdrs)
			throws RInterfaceException {

		int rows = cdrs.getNumberRows();
		if (rows == 0) {
			return;
		}

		CRowMetaData dsmeta = cdrs.getMetaDefinition();
		int cols = dsmeta.getColumnCount();

		// CDataRowSet dataset = cdcc.getAll();
		CDataRowSet dataset = cdrs;

		String[] columnNames = dsmeta.getColumnNames();

		RList rlist = new RList(cols, true);

		// create vectors for each column
		// and add to rlist
		for (String columnName : columnNames) {
			Class<?> klass = null;
			Object[] columnValues = null;
			try {
				klass = dsmeta.getColumnType(columnName);
				columnValues = dataset.getColumnValues(columnName);
			} catch (CDataGridException e) {
				throw new RInterfaceException(e);
			}

			if (klass == Double.class) {
				// convert Object[] to Double[]
				Double[] doubleObjects =
						Arrays.copyOf(columnValues, columnValues.length,
								Double[].class);
				// convert Double[] to double[]
				double[] doubles = ArrayUtils.toPrimitive(doubleObjects);
				// create R vector from doubles
				REXPDouble rvector = new REXPDouble(doubles); // NOPMD

				// add to list
				rlist.put(columnName, rvector);

				// } else if (klass == String.class) {

			} else {
				throw new RInterfaceException(
						"Cannot create dataframe: column [" + columnName
								+ "] is of unsupported type "
								+ klass.getCanonicalName());
			}

		}

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
	 * loads a package. If it isn't installed, it is loaded from CRAN
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
