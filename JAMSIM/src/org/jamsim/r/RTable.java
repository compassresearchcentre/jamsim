package org.jamsim.r;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.casper.data.model.CBuilder;

import org.apache.commons.lang.ArrayUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;

/**
 * An R expression that is a R table. The first column is the names of the rows
 * of the R table.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class RTable implements CBuilder {

	private static final int FIRST_COL = 0;
	private final String rowVariableName;
	private final String colVariableName;
	private final String[] rowNames;
	private final String[] colNames;
	private final double[][] values;
	private int currentRowIndex;

	/**
	 * Create an RTable from a Rexp.
	 * 
	 * @param rexp
	 * @throws REXPMismatchException
	 *             if rexp is not of the right Java type or R class.
	 */
	public RTable(REXP rexp) throws REXPMismatchException {
		if (!RTable.isTable(rexp) || !(rexp instanceof REXPDouble)) {
			throw new REXPMismatchException(rexp, "table");
		}

		String[] dimNamesNames =
				rexp.getAttribute("dimnames").getAttribute("names")
						.asStrings();

		rowVariableName = dimNamesNames[0];
		colVariableName = dimNamesNames[1];

		RList dimnames = rexp.getAttribute("dimnames").asList();
		rowNames = ((REXPString) dimnames.at(0)).asStrings();
		colNames = ((REXPString) dimnames.at(1)).asStrings();

		values = rexp.asDoubleMatrix();
	}

	/**
	 * Test whether rexp has a class of table.
	 * 
	 * @param rexp
	 *            expression to test
	 * @return true/false
	 */
	public static boolean isTable(REXP rexp) {
		String[] clazz =
				((REXPString) rexp.getAttribute("class")).asStrings();

		return ArrayUtils.contains(clazz, "table");
	}

	@Override
	public void close() {
		// nothing to do 
	}

	/**
	 * First column is the rows' variable name. The rest are the column
	 * dimnames.
	 * 
	 * @return column names
	 */
	@Override
	public String[] getColumnNames() {
		// return (String[])ArrayUtils.addAll(new String[] {rowVariableName},
		// colNames);
		return (String[]) ArrayUtils.add(colNames, 0, rowVariableName);
	}

	@Override
	public Class[] getColumnTypes() {
		Class<?>[] colTypes = new Class<?>[colNames.length + 1]; 
	
		// first col is row dimnames
		colTypes[0] = String.class;
		for (int i = 1; i < colTypes.length; i++) {
			colTypes[i] = Double.class;
		}

		return colTypes;
	}

	@Override
	public Map getConcreteMap() {
		return new HashMap();
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPrimaryKeyColumns() {
		return new String[] { rowVariableName };
	}

	@Override
	public void open() throws IOException {
		currentRowIndex = 0;
	}

	@Override
	public Object[] readRow() throws IOException {
		
		Object[] row = new Object[colNames.length + 1]; 
		
		// first col is row dimnames
		row[0] = rowNames[currentRowIndex];
		for (int i = 1; i < row.length; i++) {
			row[i] = values[currentRowIndex][i];
		}

		currentRowIndex++;
		
		return row;
	}

}
