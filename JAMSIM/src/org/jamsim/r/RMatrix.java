package org.jamsim.r;

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
 * An R expression that is a numeric matrix (ie: a 2D numeric vector). The first
 * column of the {@link RMatrix} is taken from the names of the rows of the R
 * table. Implements {@link CBuilder}.
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
	 * resultant dataset. eg: "1,2,3,4,5.."
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
	private int numRows;

	private int currentRowIndex;

	/**
	 * Create an {@link RMatrix} from a {@link REXP}.
	 * 
	 * @param name
	 *            name. If {@code null}, one will be generated from the column
	 *            and row variable names.
	 * @param rexp
	 *            R expression
	 * @throws REXPMismatchException
	 *             if rexp is not an {@link REXPDouble} or an R table class.
	 */
	public RMatrix(String name, REXP rexp) throws REXPMismatchException {
		if (!RMatrix.isMatrix(rexp)) {
			throw new REXPMismatchException(rexp, "table");
		}

		String[] dimNamesNames = RUtil.getDimNamesNames(rexp);
		rowVariableName =
				(dimNamesNames == null) ? "" : dimNamesNames[0] + " / "
						+ dimNamesNames[1];
		String colVariableName =
				(dimNamesNames == null) ? "" : dimNamesNames[1];
		this.name =
				(name == null) ? rowVariableName + " by " + colVariableName
						: name;

		RList dimnames = rexp.getAttribute("dimnames").asList();
		if (dimnames.at(0).isString()) {
			rowNames = ((REXPString) dimnames.at(0)).asStrings();
		} else {
			rowNames = null; // no row names
		}
		colNames = ((REXPString) dimnames.at(1)).asStrings();

		values = rexp.asDoubleMatrix();

		numRows = rexp.dim()[0];

	}

	/**
	 * Test whether rexp is a numeric matrix (ie: a REXPDouble with 2
	 * dimensions). Includes objects of class "table".
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
