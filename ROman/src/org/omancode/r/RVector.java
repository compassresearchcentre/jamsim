package org.omancode.r;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPFactor;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPRaw;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REXPVector;
import org.rosuda.REngine.RList;

/**
 * Represents a vector of a single Java type. All elements of the vector must be
 * of this type. Provides methods to convert to/from an {@link REXPVector} for
 * use with R.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class RVector {

	/**
	 * Vector name.
	 */
	private final String name;

	/**
	 * List of values. This is a raw type so that any type of List can be
	 * specified at runtime. The actual list will be an implementation that
	 * returns a primitive array so that it can be used to create a
	 * {@link REXPVector} via the {@link #getREXPVector()} method.
	 */
	@SuppressWarnings("unchecked")
	private final List values;

	private final Class<?> klass;

	/**
	 * Create a new {@link RVector} with given name, value type, a initial size.
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
		this.values = createList(klass, initialSize);

		if (values == null) {
			throw new UnsupportedTypeException("Unsupported type "
					+ klass.getCanonicalName());
		}

		this.klass = klass;
	}

	/**
	 * Constructor used by {@link #create(String, Class, int)}.
	 * 
	 * @param name
	 * @param klass
	 * @param values
	 */
	private RVector(String name, Class<?> klass, List values) {
		this.name = name;
		this.values = values;
		this.klass = klass;
	}

	/**
	 * Create an {@link RVector} or return {@code null} if the supplied class is
	 * of the wrong type.
	 * 
	 * @param name
	 *            vector name.
	 * @param klass
	 *            type of the values this vector will hold.
	 * @param initialSize
	 *            number of elements in this vector.
	 * @return {@link RVector} or {@code null} if {@code klass} is of
	 *         unsupported type.
	 */
	public static RVector create(String name, Class<?> klass, int initialSize) {
		List values = createList(klass, initialSize);

		if (values == null) {
			return null;
		}

		return new RVector(name, klass, values);
	}

	/**
	 * Create a new {@link RVector} from the values in a {@link REXPVector}.
	 * 
	 * @param name
	 *            vector name
	 * @param rexp
	 *            source of vector values
	 * @throws UnsupportedTypeException
	 *             if {@code rexp} is of an unsupported type (ie: no
	 *             corresponding primitive list implementation exists)
	 * @throws REXPMismatchException
	 *             if {@code rexp} cannot be converted to a primitive array list
	 */
	public RVector(String name, REXPVector rexp)
			throws UnsupportedTypeException, REXPMismatchException {
		this.name = name;
		this.values = createList(rexp);
		this.klass = calcJavaType(rexp);
	}

	/**
	 * Return java type for given R expression object.
	 * 
	 * @param rexp
	 *            R expression object
	 * @return java type
	 * @throws UnsupportedTypeException
	 *             if {@code rexp} is of a type that is not supported (ie: not
	 *             yet implemented).
	 */
	private Class<?> calcJavaType(Object rexp)
			throws UnsupportedTypeException {
		if (rexp instanceof REXPDouble) {
			return Double.class;
		} else if (rexp instanceof REXPFactor) {
			return String.class;
		} else if (rexp instanceof REXPInteger) {
			return Integer.class;
		} else if (rexp instanceof REXPLogical) {
			return Boolean.class;
		} else if (rexp instanceof REXPString) {
			return String.class;
		} else if (rexp instanceof REXPRaw) {
			return Byte.class;
		} else {
			throw new UnsupportedTypeException(rexp.getClass());
		}
	}

	/**
	 * Creates a primitive array list of the appropriate type for the given
	 * rexp.
	 * 
	 * @param rexp
	 *            an {@link REXPVector}.
	 * @return primitive array containing the values of {@code rexp}.
	 * @throws UnsupportedTypeException
	 *             if {@code rexp} is of an unsupported type (ie: no
	 *             corresponding primitive list implementation exists)
	 * @throws REXPMismatchException
	 *             if {@code rexp} cannot be converted to a primitive array list
	 */
	private List createList(REXPVector rexp) throws UnsupportedTypeException,
			REXPMismatchException {
		if (rexp instanceof REXPDouble) {
			return new DoubleArrayList(rexp.asDoubles());
		} else if (rexp instanceof REXPFactor) {
			return Arrays.asList(rexp.asStrings());
		} else if (rexp instanceof REXPInteger) {
			return new IntArrayList(rexp.asIntegers());
		} else if (rexp instanceof REXPLogical) {
			return new IntArrayList(rexp.asIntegers());
		} else if (rexp instanceof REXPString) {
			return Arrays.asList(rexp.asStrings());
		} else if (rexp instanceof REXPRaw) {
			return new ByteArrayList(rexp.asBytes());
		} else {
			throw new UnsupportedTypeException(rexp.getClass());
		}

	}

	/**
	 * Creates a primitive array list of the appropriate type for the given
	 * {@code klass}. Uses <a href="http://fastutil.dsi.unimi.it/">fastutil</a>
	 * primitive array collections for efficiency. When values are added using
	 * {@link #addValue(Object)} they are automatically unboxed and stored as
	 * primitives.
	 * 
	 * @param klass
	 *            type of the values the list should hold.
	 * @param initialSize
	 *            number of elements in the list. Used to initial the list to
	 *            this size.
	 * @returns primitive list, or {@code null} if no corresponding primitive
	 *          list implementation exists
	 */
	private static List createList(Class<?> klass, int initialSize) {
		List values = null;

		if (klass == double.class || klass == Double.class) {
			values = new DoubleArrayList(initialSize);
		} else if (klass == int.class || klass == Integer.class) {
			values = new IntArrayList(initialSize);
		} else if (klass == boolean.class || klass == Boolean.class) {
			values = new BooleanArrayList(initialSize);
		} else if (klass == String.class) {
			values = new ArrayList<String>(initialSize);
		} else if (klass == float.class || klass == Float.class) {
			// no R type for float, use double instead
			values = new DoubleArrayList(initialSize);
		} else if (klass == long.class || klass == Long.class) {
			// no R type for long, use int instead
			// R doesn't have anything that can represent long natively,
			// so you have to convert it to either int (if 32-bit is
			// enough), double (giving you about 52-bits lossless
			// storage) or raw vector with 8-bytes for each long.
			// we'll use int
			values = new IntArrayList(initialSize);
		} else if (klass == char.class || klass == Character.class) {
			// no R type for char, use String instead
			values = new ArrayList<String>(initialSize);
		} else if (klass.isArray()) {
			values = new RList(initialSize, false);
		}

		return values;

	}

	/**
	 * Add a single value to this vector. Automatically unboxes objects of
	 * primitive type and stores them in a primitive list.
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
		} else if (klass.isArray()) {
			// this must be an array object passed in, so wrap it in a
			// REXPVector
			values.add(RUtil.toVector(value));
		} else {
			values.add(value);
		}

	}

	/**
	 * Returns the element at the specified index. The element is boxed before
	 * being returned from the primitive list.
	 * 
	 * @param index
	 *            index
	 * @return element at this index
	 */
	public Object getValue(int index) {
		return values.get(index);
	}

	/**
	 * Get the list containing all the values of this vector.
	 * 
	 * @return List containing this vector's vales.
	 */
	@SuppressWarnings("unchecked")
	public List getValues() {
		return values;
	}

	/**
	 * Returns the number of elements in this vector.
	 * 
	 * @return size
	 */
	public int size() {
		return values.size();
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
	 * Get the type of the values in the vector.
	 * 
	 * @return java class type
	 */
	public Class<?> getType() {
		return klass;
	}

	/**
	 * Convert this vector to an REXPVector.
	 * 
	 * @return an REXPVector.
	 * @throws UnsupportedTypeException
	 *             if the type of this vector's values is not supported in R.
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
					"Unsupported backing list type "
							+ values.getClass().getCanonicalName());
		}

	}

	@Override
	public final String toString() {
		if (name == null) {
			return super.toString();
		}
		return name;
	}
}
