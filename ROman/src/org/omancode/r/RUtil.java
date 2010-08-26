package org.omancode.r;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.data.model.CDataRowSet;
import net.casper.data.model.CRowMetaData;
import net.casper.io.beans.CMarkedUpRow;
import net.casper.io.beans.CMarkedUpRowBean;
import net.casper.io.beans.util.BeanPropertyInspector;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPNull;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REXPVector;
import org.rosuda.REngine.RList;

/**
 * Static utility class of R related functions.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class RUtil {

	private RUtil() {
		// no instantiation
	}

	/**
	 * Convert array to {@link REXPVector}.
	 * 
	 * @param array
	 *            array to convert
	 * @return {@link REXPVector}.
	 */
	public static REXPVector toVector(double[] array) {
		return new REXPDouble(array);
	}

	/**
	 * Convert array to {@link REXPVector}.
	 * 
	 * @param array
	 *            array to convert
	 * @return {@link REXPVector}.
	 */
	public static REXPVector toVector(int[] array) {
		return new REXPInteger(array);
	}

	/**
	 * Convert array to {@link REXPVector}.
	 * 
	 * @param array
	 *            array to convert
	 * @return {@link REXPVector}.
	 */
	public static REXPVector toVector(boolean[] array) {
		return new REXPLogical(array);
	}

	/**
	 * Convert array to {@link REXPVector}.
	 * 
	 * @param array
	 *            array to convert
	 * @return {@link REXPVector}.
	 */
	public static REXPVector toVector(String[] array) {
		return new REXPString(array);
	}

	/**
	 * Convert object to {@link REXPVector}.
	 * 
	 * @param array
	 *            object that is null or an array
	 * @return if object is null, returns {@link REXPNull}, otherwise if object
	 *         is a primitive array returns an {@link REXPVector}, otherwise
	 *         throws an {@link IllegalArgumentException}.
	 */
	public static REXP toVector(Object array) {
		if (array == null) {
			return new REXPNull();
		}

		Class<?> arrayClass = array.getClass();

		if (arrayClass == double[].class) {
			return new REXPDouble((double[]) array);
		} else if (arrayClass == int[].class) {
			return new REXPInteger((int[]) array);
		} else if (arrayClass == String[].class) {
			return new REXPString((String[]) array);
		} else if (arrayClass == boolean[].class) {
			return new REXPLogical((boolean[]) array);
		} else {
			throw new IllegalArgumentException("Cannot convert "
					+ arrayClass.getCanonicalName() + " to R object");
		}
	}

	/**
	 * Convert Object to REXP.
	 * 
	 * @param value
	 *            object
	 * @return REXP
	 */
	public static REXP toREXP(Object value) {

		if (value == null) {
			return new REXPNull();
		} else if (value instanceof Double) {
			return new REXPDouble((Double) value);
		} else if (value instanceof Integer) {
			return new REXPInteger((Integer) value);
		} else if (value instanceof String) {
			return new REXPString((String) value);
		} else if (value instanceof Boolean) {
			return new REXPLogical((Boolean) value);
		} else if (value instanceof Character) {
			return new REXPString(((Character) value).toString());
		} else {
			throw new IllegalArgumentException("Cannot convert "
					+ value.getClass().getCanonicalName() + " to R object");
		}

	}

	/**
	 * Get bean property. Return reflection errors as
	 * {@link RInterfaceException}s.
	 * 
	 * @param bean
	 *            Bean whose property is to be extracted
	 * @param name
	 *            Possibly indexed and/or nested name of the property to be
	 *            extracted
	 * @return the property value
	 * @throws RInterfaceException
	 *             if reflection problem getting property
	 */
	private static Object getProperty(Object bean, String name)
			throws RInterfaceException {
		Object prop;
		try {
			prop = PropertyUtils.getProperty(bean, name);
		} catch (IllegalAccessException e) {
			throw new RInterfaceException("Oh no, "
					+ "couldn't get property [" + name + "]", e);
		} catch (InvocationTargetException e) {
			throw new RInterfaceException("Oh no, "
					+ "couldn't get property [" + name + "]", e);
		} catch (NoSuchMethodException e) {
			throw new RInterfaceException("Oh no, "
					+ "couldn't get property [" + name + "]", e);
		}
		return prop;

	}

	/**
	 * Create an {@link RList} from a {@link CDataCacheContainer}.
	 * 
	 * NB: doesn't automatically create factors like read.table does.
	 * 
	 * @param container
	 *            casper container
	 * @return rlist rlist
	 * @throws RInterfaceException
	 *             if problem
	 */
	public static RList toRList(CDataCacheContainer container)
			throws RInterfaceException {

		if (container.size() == 0) {
			throw new RInterfaceException(
					"Cannot create RList from empty casper container \""
							+ container.getCacheName() + "\"");
		}

		// create an RVector for each column
		List<RVector> vectors =
				getNewRVectors(container.getMetaDefinition(), container
						.size());

		if (vectors.isEmpty()) {
			throw new RInterfaceException(
					"Container does not contain any columns "
							+ "that can be converted to a RList");
		}

		// fill the RVectors' values row by row
		// from the columns
		try {
			CDataRowSet cdrs = container.getAll();
			while (cdrs.next()) {
				for (RVector vector : vectors) {
					String propName = vector.getName();
					Object prop = cdrs.getObject(propName);
					vector.addValue(prop);
				}
			}
		} catch (CDataGridException e) {
			throw new RInterfaceException(e);
		}

		return toRList(vectors);
	}

	/**
	 * Create an {@link RList} from the given Collection. Introspection is used
	 * to determine the bean properties (i.e.: getter methods) that are exposed,
	 * and each one becomes a column in the dataframe. Columns are only created
	 * for primitive properties and arrays of primitive properties; object
	 * properties are ignored without warning.
	 * 
	 * NB: doesn't automatically create factors like read.table does.
	 * 
	 * @param col
	 *            the Java collection to convert.
	 * @param stopClass
	 *            Columns are created for all getter methods that are defined by
	 *            {@code stopClass}'s subclasses. {@code stopClass}'s getter
	 *            methods and superclass getter methods are not converted to
	 *            columns in the dataframe.
	 * @return rlist rlist
	 * @throws RInterfaceException
	 *             if Collection cannot be read, or dataframe cannot be created.
	 */
	public static RList toRList(Collection<?> col, Class<?> stopClass)
			throws RInterfaceException {

		if (col.isEmpty()) {
			throw new RInterfaceException(
					"Cannot create RList for empty collection");
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

			RVector vector =
					RVector.create(prop.getName(), klass, numElements);
			if (vector != null) {
				// only add classes we can handle, ignore the rest
				vectors.add(vector);
			}
		}

		// fill the RVectors' values row by row
		// from the bean's property values
		for (Object element : col) {
			for (RVector vector : vectors) {
				String propName = vector.getName();
				Object prop = getProperty(element, propName);
				vector.addValue(prop);
			}
		}

		String markedUpRowGetterName = props.getGetMarkedUpRowMethodName();
		if (markedUpRowGetterName != null) {
			List<RVector> rowProps =
					extractMarkedRow(col, markedUpRowGetterName);
			vectors.addAll(rowProps);
		}

		if (vectors.isEmpty()) {
			throw new RInterfaceException(
					"Collection does not contain any properties "
							+ "that can be converted to a dataframe");
		}

		return toRList(vectors);

	}

	/**
	 * Extract the values of a {@link CMarkedUpRow} that is retrieved via a
	 * getter method on a collection of beans.
	 * 
	 * @param col
	 *            collection of beans that expose a {@link CMarkedUpRow}.
	 * @param markedUpRowGetterName
	 *            getter method used on the bean to retrieve the
	 *            {@link CMarkedUpRow}.
	 * @return list of {@link RVector}s. One for each value in the
	 *         {@link CMarkedUpRow}.
	 * @throws RInterfaceException
	 *             if problem reading collection.
	 */
	public static List<RVector> extractMarkedRow(Collection<?> col,
			String markedUpRowGetterName) throws RInterfaceException {

		if (col.isEmpty()) {
			throw new RInterfaceException("Empty collection of "
					+ CMarkedUpRowBean.class);
		}

		Object cMarkedUpRowBean = col.iterator().next();
		CMarkedUpRow firstRow =
				(CMarkedUpRow) getProperty(cMarkedUpRowBean,
						markedUpRowGetterName);

		// get list of vectors, one for each column in the row
		// the vectors will be empty
		List<RVector> vectors =
				getNewRVectors(firstRow.getMetaDefinition(), col.size());

		if (vectors.isEmpty()) {
			throw new RInterfaceException(
					"Container does not contain any columns "
							+ "that can be converted to a RList");
		}

		// fill the RVectors' values row by row
		// from the bean's property values
		for (Object element : col) {
			for (RVector vector : vectors) {
				String propName = vector.getName();
				CMarkedUpRow row =
						(CMarkedUpRow) getProperty(element,
								markedUpRowGetterName);
				Object prop;
				try {
					prop = row.getObject(propName);
				} catch (CDataGridException e) {
					throw new RInterfaceException(e);
				}
				vector.addValue(prop);
			}
		}

		return vectors;

	}

	/**
	 * Generate a list of {@link RVector}s, one for each column in the meta
	 * data. Each {@link RVector} will be empty and initialised to the specified
	 * size.
	 * 
	 * @param meta
	 *            meta data
	 * @param numElements
	 *            initial size of each vector
	 * @return list of empty {@link RVector}s
	 * @throws RInterfaceException
	 */
	private static List<RVector> getNewRVectors(CRowMetaData meta,
			int numElements) throws RInterfaceException {
		String[] columnNames = meta.getColumnNames();
		Class<?>[] columnTypes = meta.getColumnTypes();

		// create a List of RVectors that hold an unknown type
		ArrayList<RVector> vectors = new ArrayList<RVector>();

		// create an RVector for each column
		for (int i = 0; i < columnNames.length; i++) {
			Class<?> klass = columnTypes[i];

			RVector vector =
					RVector.create(columnNames[i], klass, numElements);
			if (vector != null) {
				// only add classes we can handle, ignore the rest
				vectors.add(vector);
			}
		}
		return vectors;
	}

	/**
	 * Create an {@link RList} from a collection of {@link RVector}s.
	 * 
	 * @param vectors
	 *            vector collection
	 * @return rlist rlist
	 * @throws RInterfaceException
	 *             if problem
	 */
	public static RList toRList(Collection<RVector> vectors)
			throws RInterfaceException {
		// create an rlist of REXPVectors from each RVector
		RList rlist = new RList(vectors.size(), true);
		for (RVector vector : vectors) {
			try {
				rlist.put(vector.getName(), vector.getREXPVector());
			} catch (UnsupportedTypeException e) {
				throw new RInterfaceException("Cannot get R vector ["
						+ vector.getName() + "]. " + e.getMessage(), e);
			}
		}
		return rlist;
	}

	/**
	 * Create a list of {@link RVector}s from an {@link RList}.
	 * 
	 * @param rlist
	 *            rlist
	 * @return list of {@link RVector}s.
	 * @throws UnsupportedTypeException
	 *             if {@code rlist} contains a type than cannot be converted to
	 *             an {@link RVector}.
	 * @throws RInterfaceException
	 *             if problem converting a member of the {@code rlist} to a
	 *             {@link RVector}.
	 */
	public static List<RVector> toRVectors(RList rlist)
			throws UnsupportedTypeException, RInterfaceException {
		ArrayList<RVector> rVectors = new ArrayList<RVector>(rlist.size());

		int index = 0;
		for (Object element : rlist) {

			if (element instanceof REXPVector) {
				REXPVector rexp = (REXPVector) element;

				rVectors.add(new RVector((String) rlist.names.get(index++),
						rexp));
			} else {
				throw new UnsupportedTypeException("rlist contains "
						+ element.getClass().getCanonicalName());
			}

		}

		return rVectors;
	}

	/**
	 * Returns the R class attribute of rexp.
	 * 
	 * @param rexp
	 *            expression to test
	 * @return a comma separated string of the rexp classes, eg: {@code
	 *         "[xtabs, table]"}. If it has no class attribute, returns the name
	 *         of the java class instead (eg: {@code REXPDouble}).
	 */
	public static String getClassAttribute(REXP rexp) {

		REXPString classAttribute = (REXPString) rexp.getAttribute("class");

		String[] clazz = null;
		if (classAttribute == null) {
			// If the object does not have a class attribute,
			// it has an implicit class, "matrix", "array" or the
			// result of mode(x) (except that integer vectors have implicit
			// class "integer")

			// return the java type instead
			clazz = new String[] { rexp.getClass().getSimpleName() };
		} else {
			clazz = classAttribute.asStrings();
		}

		return Arrays.toString(clazz);
	}

	/**
	 * Get the {@code names} attribute of rexp. Like the R function {@code
	 * names}, this will return {@code dimnames[[1]]} for a one-dimensional
	 * array.
	 * 
	 * @param rexp
	 *            r expression
	 * @return names attribute
	 * @throws RInterfaceException
	 *             if problem reading attribute
	 */
	public static String[] getNamesAttribute(REXP rexp)
			throws RInterfaceException {

		REXPString namesAttribute = (REXPString) rexp.getAttribute("names");

		String[] names = null;
		if (namesAttribute != null) {
			names = namesAttribute.asStrings();
		} else if (getDimensions(rexp) == 1) {
			names = getDimNames(rexp);
		}

		return names;
	}

	/**
	 * Get dimension (dim) attribute from rexp.
	 * 
	 * @param rexp
	 *            rexp
	 * @return number of dimensions
	 */
	public static int getDimensions(REXP rexp) {
		int[] dims = rexp.dim();
		return dims == null ? 0 : dims.length;
	}

	/**
	 * Return the {@code names} attribute of the {@code dimnames} attribute.
	 * 
	 * @param rexp
	 *            rexp
	 * @return {@code names} of {@code dimnames}, or {@code null} if there is no
	 *         {@code names} attribute.
	 */
	public static String[] getNamesDimNames(REXP rexp) {

		REXP dimNames = rexp.getAttribute("dimnames");

		if (dimNames == null) {
			return null;
		}

		REXPString namesDimNames =
				(REXPString) dimNames.getAttribute("names");

		if (namesDimNames == null) {
			return null;
		}

		return namesDimNames.asStrings();

	}

	/**
	 * Return the {@code dimnames} attribute.
	 * 
	 * @param rexp
	 *            rexp
	 * @return {@code dimnames}or {@code null} if there is no {@code dimnames}
	 *         attribute.
	 * @throws RInterfaceException
	 *             if problem determining {@code dimnames} attribute.
	 */
	public static String[] getDimNames(REXP rexp) throws RInterfaceException {

		REXPGenericVector dimNames =
				(REXPGenericVector) rexp.getAttribute("dimnames");

		if (dimNames == null) {
			return null;
		}

		try {
			return dimNames.asList().at(0).asStrings();
		} catch (REXPMismatchException e) {
			throw new RInterfaceException(e);
		}

	}

	/**
	 * Returns contents of an R file. Removes "\r" in the string, because R
	 * doesn't like them.
	 * 
	 * @param file
	 *            text file
	 * @return contents of text file with "\r" removed
	 * @throws IOException
	 *             if file cannot be read.
	 */
	public static String readRFile(File file) throws IOException {
		FileReader reader = new FileReader(file);
		String expr = IOUtils.toString(reader);

		// release file after loading,
		// instead of waiting for VM exit/garbage collection
		reader.close();

		// strip "\r" otherwise we will get parse errors
		return StringUtils.remove(expr, "\r");
	}

	/**
	 * Returns contents of an {@link InputStream}. Removes "\r" in the string,
	 * because R doesn't like them.
	 * 
	 * @param stream
	 *            stream
	 * @return contents of text file with "\r" removed
	 * @throws IOException
	 *             if file cannot be read.
	 */
	public static String readRStream(InputStream stream) throws IOException {
		String expr = IOUtils.toString(stream);

		// release file after loading,
		// instead of waiting for VM exit/garbage collection
		stream.close();

		// strip "\r" otherwise we will get parse errors
		return StringUtils.remove(expr, "\r");
	}

	/**
	 * Returns a string representing the boolean value in R.
	 * 
	 * @param bool boolean
	 * @return "TRUE", or "FALSE"
	 */
	public static String rBoolean(boolean bool) {
		return bool ? "TRUE" : "FALSE";
	}
}
