package org.jamsim.r;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.casper.data.model.CBuilder;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;

/**
 * An R expression that is an R dataframe. The dataframe "names" attribute is
 * used for the column headings. Implements {@link CBuilder}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class RDataFrame implements CBuilder {

	private final String name;
	private final String[] colNames;
	private int currentRowIndex;
	private final Class<?>[] columnTypes;
	private final List<RVector> rvectors;
	private final int numRows;

	/**
	 * Create an {@link RDataFrame} from an {@link REXP}.
	 * 
	 * @param name
	 *            name. If {@code null}, one will be generated from the column
	 *            and row variable names.
	 * @param rexp
	 *            R expression
	 * @throws REXPMismatchException
	 *             if rexp is not an {@link REXPGenericVector} or an R dataframe
	 *             class.
	 * @throws UnsupportedTypeException
	 *             if {@code rexp} contains a type that cannot be handled
	 */
	public RDataFrame(String name, REXP rexp) throws REXPMismatchException,
			UnsupportedTypeException {
		if (!RDataFrame.isDataFrame(rexp)
				|| !(rexp instanceof REXPGenericVector)) {
			throw new REXPMismatchException(rexp, "dataframe");
		}

		this.name = name;
		colNames = ((REXPString) rexp.getAttribute("names")).asStrings();

		// convert rlist to list of rvectors
		RList rlist = rexp.asList();
		rvectors = RUtil.toRVectors(rlist);

		// get column types and number of rows
		columnTypes = calcColumnTypes(rvectors);
		numRows = rvectors.get(0).size();
	}

	/**
	 * Return an array of java types for the elements of the rlist.
	 * 
	 * @param rvectors
	 *            rlist
	 * @return array of java types for the elements of rlist
	 * @throws UnsupportedTypeException
	 *             if rlist contains a type that is not supported (ie:
	 *             implemented).
	 */
	private Class<?>[] calcColumnTypes(List<RVector> rvectors)
			throws UnsupportedTypeException {
		Class<?>[] columnTypes = new Class<?>[rvectors.size()];

		int index = 0;
		for (RVector rvec : rvectors) {
			columnTypes[index++] = rvec.getType();
		}

		return columnTypes;
	}

	/**
	 * Test whether rexp has a class of data.frame.
	 * 
	 * @param rexp
	 *            expression to test
	 * @return true/false
	 */
	public static boolean isDataFrame(REXP rexp) {
		String clazz = RUtil.getClassAttribute(rexp);
		return clazz.contains("data.frame");
	}

	@Override
	public void close() {
		// nothing to do
	}

	/**
	 * Column names comes from the dataframe "names" attribute.
	 * 
	 * @return column names
	 */
	@Override
	public String[] getColumnNames() {
		return colNames;
	}

	@Override
	public Class[] getColumnTypes() {
		return columnTypes;
	}

	/**
	 * Returns a LinkedHashMap so that rows will be in insertion order (because
	 * we also have a null primary key).
	 * 
	 * @return tree map
	 */
	@Override
	public Map getConcreteMap() {
		return new LinkedHashMap();
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * No primary key.
	 * 
	 * @return null
	 */
	@Override
	public String[] getPrimaryKeyColumns() {
		return null;
	}

	@Override
	public void open() throws IOException {
		currentRowIndex = 0;
	}

	@Override
	public Object[] readRow() throws IOException {

		if (currentRowIndex == numRows) {
			return null;
		}

		Object[] row = new Object[rvectors.size()];

		int index = 0;
		for (RVector rvec : rvectors) {
			row[index++] = rvec.getValue(currentRowIndex);
		}
		currentRowIndex++;
		return row;
	}

}
