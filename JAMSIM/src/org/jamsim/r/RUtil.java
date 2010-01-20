package org.jamsim.r;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.data.model.CDataRowSet;
import net.casper.data.model.CRowMetaData;
import net.casper.io.beans.util.BeanPropertyInspector;
import net.casper.io.file.util.ArrayUtil;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ArrayUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
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

		CRowMetaData meta = container.getMetaDefinition();
		String[] columnNames = meta.getColumnNames();
		Class<?>[] columnTypes = meta.getColumnTypes();

		int numElements = container.size();

		// create a List of RVectors that hold an unknown type
		ArrayList<RVector> vectors = new ArrayList<RVector>();

		// create an RVector for each column
		for (int i = 0; i < columnNames.length; i++) {
			Class<?> klass = columnTypes[i];

			// only process classes we can handle, ignore the rest
			if (klass.isPrimitive() || klass.isArray()
					|| klass.getSuperclass() == Number.class
					|| klass == String.class || klass == Character.class) {
				RVector vector;
				try {
					vector = new RVector(// NOPMD
							columnNames[i], klass, numElements);
				} catch (UnsupportedTypeException e) {
					throw new RInterfaceException("Cannot create column ["
							+ columnNames[i] + "]. " + e.getMessage(), e);
				}
				vectors.add(vector);
			}

		}

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
					Object prop;
					prop = cdrs.getObject(propName);
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

			// only process classes we can handle, ignore the rest
			if (klass.isPrimitive() || klass.isArray()
					|| klass.getSuperclass() == Number.class
					|| klass == String.class || klass == Character.class) {
				RVector vector;
				try {
					vector = new RVector(// NOPMD
							prop.getName(), klass, numElements);
				} catch (UnsupportedTypeException e) {
					throw new RInterfaceException("Cannot create column ["
							+ prop.getName() + "]. " + e.getMessage(), e);
				}
				vectors.add(vector);
			}
		}

		if (vectors.isEmpty()) {
			throw new RInterfaceException(
					"Collection does not contain any properties "
							+ "that can be converted to a dataframe");
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

		return toRList(vectors);

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
	 * @throws REXPMismatchException
	 *             if problem converting a member of the {@code rlist} to a
	 *             {@link RVector}.
	 */
	public static List<RVector> toRVectors(RList rlist)
			throws UnsupportedTypeException, REXPMismatchException {
		ArrayList<RVector> rVectors = new ArrayList<RVector>(rlist.size());

		int index = 0;
		for (Object element : rlist) {

			if (element instanceof REXPVector) {
				REXPVector rexp = (REXPVector) element;

				rVectors.add(new RVector(
						(String)rlist.names.get(index++), rexp));
			} else {
				throw new UnsupportedTypeException("rlist contains "
						+ element.getClass().getCanonicalName());
			}

		}

		return rVectors;
	}

	/**
	 * Returns the R class(es) of rexp.
	 * 
	 * @param rexp
	 *            expression to test
	 * @return true/false
	 */
	public static String getClassAttribute(REXP rexp) {
		String[] clazz =
				((REXPString) rexp.getAttribute("class")).asStrings();

		return ArrayUtil.toString(clazz);
	}

}
