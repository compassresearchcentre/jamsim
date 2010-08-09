package org.omancode.r;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import net.casper.data.model.CBuilder;
import net.casper.ext.narrow.NarrowException;
import net.casper.ext.narrow.NarrowUtil;

import org.apache.commons.lang.ArrayUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;

/**
 * An R expression that is a numeric matrix (ie: a 2D numeric vector). This
 * includes objects of class "table". eg:
 * 
 * <pre>
 * > yearlyFreq(children$single, "single")
 * 
 *         single
 *           1    2
 *  Year 1  81 1029
 *  Year 2 131  979
 *  Year 3 157  953
 *  Year 4 153  957
 *  Year 5 156  954
 * 
 * > str(yearlyFreq(children$single, "single"))
 * 
 * int [1:5, 1:2] 81 131 157 153 156 1029 979 953 957 954
 * - attr(*, "dimnames")=List of 2
 * ..$ : chr [1:5] "Year 1" "Year 2" "Year 3" "Year 4" ...
 * ..$ single: chr [1:2] "1" "2"
 * </pre>
 * 
 * The first column of the generated dataset is the names of the rows of the
 * matrix (if it has any) eg: {@code "Year 1" "Year 2" "Year 3" "Year 4"}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class RMatrix implements CBuilder {

	private final String name;

	/**
	 * The variable name for the {@link #rowNames}. This will the name header of
	 * the first column. eg: "Frequency"
	 */
	private final String rowVariableName;

	/**
	 * Identifies the name of each row. This will be the first column in the
	 * resultant dataset. eg: "1,2,3,4,5..". This is an Object[] so it can be
	 * narrowed to either Integer[] or String[].
	 */
	private Object[] rowNames;

	/**
	 * Column name headers for the 2nd and subsequent columns.
	 */
	private final String[] colNames;

	/**
	 * Matrix of values in the table.
	 */
	private final double[][] values;

	/**
	 * Number of rows.
	 */
	private final int numRows;

	private int currentRowIndex;

	/**
	 * Create an {@link RMatrix} from a {@link REXP}. Rexp must be a numeric
	 * matrix (ie: a {@link REXPDouble} or {@link REXPInteger} with 2
	 * dimensions). This includes objects of class "table".
	 * 
	 * @param name
	 *            name. If {@code null}, one will be generated from the column
	 *            and row variable names.
	 * @param rexp
	 *            R expression
	 * @throws RInterfaceException
	 *             if rexp is not an {@link REXPDouble} or an R table class.
	 */
	public RMatrix(String name, REXP rexp) throws RInterfaceException {
		if (!RMatrix.isMatrix(rexp)) {
			throw new RInterfaceException(rexp,
					"Cannot be accessed as a RMatrix");
		}

		// get names of the dimensions (dimnames)
		String[] namesDimNames = RUtil.getNamesDimNames(rexp);
		rowVariableName =
				(namesDimNames == null) ? "" : namesDimNames[0]
						+ (!("".equals(namesDimNames[0]) || ""
								.equals(namesDimNames[1])) ? " / " : "")
						+ namesDimNames[1];
		String colVariableName =
				(namesDimNames == null) ? "" : namesDimNames[1];
		this.name =
				(name == null) ? rowVariableName + " by " + colVariableName
						: name;

		try {
			// get rowNames & colNames from the dimnames attribute
			RList dimnames = rexp.getAttribute("dimnames").asList();
			if (dimnames.at(0).isString()) {
				rowNames = ((REXPString) dimnames.at(0)).asStrings();
			} else {
				rowNames = null; // no row names
			}
			colNames = ((REXPString) dimnames.at(1)).asStrings();

			// set values
			values = rexp.asDoubleMatrix();
			numRows = rexp.dim()[0];

		} catch (REXPMismatchException e) {
			throw new RInterfaceException(e);
		}

	}

	/**
	 * Test whether rexp is a numeric matrix (ie: a {@link REXPDouble} or
	 * {@link REXPInteger} with 2 dimensions). Includes objects of class
	 * "table".
	 * 
	 * @param rexp
	 *            expression to test
	 * @return true/false
	 */
	public static boolean isMatrix(REXP rexp) {

		return ((rexp instanceof REXPDouble || rexp instanceof REXPInteger) && (RUtil
				.getDimensions(rexp) == 2));
	}

	@Override
	public void close() {
		// nothing to do
	}

	/**
	 * Return column names. If there are row names, then the first column is the
	 * rows' variable name and the rest are the column dimnames. If there are no
	 * row names, then the columns are just the dimnames.
	 * 
	 * @return column names
	 */
	@Override
	public String[] getColumnNames() {
		if (rowNames == null) {
			return colNames;
		} else {
			// first column is the rows' variable name. The rest are the column
			// dimnames.
			return (String[]) ArrayUtils.add(colNames, 0, rowVariableName);
		}
	}

	@Override
	public Class[] getColumnTypes() {

		Class<?>[] colTypes;

		if (rowNames == null) {
			// no row names. col types all double
			colTypes = new Class<?>[colNames.length];
			for (int i = 0; i < colTypes.length; i++) {
				colTypes[i] = Double.class;
			}

		} else {
			// first col is row names
			colTypes = new Class<?>[colNames.length + 1];

			// narrow the row names so that if they are numbers,
			// they will be number sorted not string sorted
			NarrowUtil nutil = new NarrowUtil();
			Class<?> rowNamesType = nutil.calcNarrowestType(rowNames, false);
			try {
				rowNames = nutil.narrowArray(rowNames, rowNamesType, false);
			} catch (NarrowException e) {
				throw new IllegalStateException(e);
			}

			// first col is row dimnames
			colTypes[0] = rowNamesType;

			for (int i = 1; i < colTypes.length; i++) {
				colTypes[i] = Double.class;
			}
		}
		return colTypes;
	}

	@Override
	public Map getConcreteMap() {
		return new LinkedHashMap();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String[] getPrimaryKeyColumns() {
		if (rowNames == null) {
			return null;
		} else {
			return new String[] { rowVariableName };
		}
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

		Object[] row;

		if (rowNames == null) {
			// row names column
			row = new Object[colNames.length];
			for (int i = 0; i < colNames.length; i++) {
				row[i] = values[currentRowIndex][i];
			}
		} else {
			row = new Object[colNames.length + 1];

			// first col is row dimnames
			row[0] = rowNames[currentRowIndex];
			for (int i = 0; i < colNames.length; i++) {
				row[i + 1] = values[currentRowIndex][i];
			}
		}
		currentRowIndex++;

		return row;
	}

}
