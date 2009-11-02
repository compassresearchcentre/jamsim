package org.jamsim.r;

import org.jamsim.matrix.IndexedDenseDoubleMatrix2D;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

/**
 * Low level interface to R using the JRI methods.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class RInterfaceJRI {

	private static Rengine rEngine = null;
	private static long rniP_dataframeClass;

	private static void initREngine() {

		rniP_dataframeClass = rEngine.rniPutString("data.frame");
		rEngine.rniPreserve(rniP_dataframeClass);

	}

	public static Rengine loadREngine(RMainLoopCallbacks rloopHandler)
			throws RInterfaceException {

		// tell Rengine code not to die if it can't
		// load the JRI native DLLs. This allows
		// us to catch the UnsatisfiedLinkError
		// ourselves
		System.setProperty("jri.ignore.ule", "yes");

		try {

			if (!Rengine.versionCheck()) {
				throw new RInterfaceException(
						"Cannot load R: JRI library version does not match R version");
			}
		} catch (UnsatisfiedLinkError e) {
			throw new RInterfaceException("Cannot load R: "
					+ "Please make sure that jri.dll is "
					+ "in a directory listed in java.library.path.\n");
		}

		// start the main loop with our loop handler
		rEngine =
				new Rengine(new String[] { "--no-save" }, true, rloopHandler);

		// the engine creates R in a new thread,
		// so we should wait until it's ready
		if (!rEngine.waitForR()) {
			rEngine = null;
			throw new RInterfaceException("R thread died");
		}

		initREngine();

		// return successfully
		return rEngine;

	}

	/**
	 * Assign a java side matrix object to a data frame inside R.
	 * 
	 * @param name
	 *            name of dataframe in R to create
	 * @param matrix
	 *            matrix to be created in R
	 * @throws RInterfaceException
	 *             if R engine not yet started.
	 */
	public static void assignDataFrame(String name,
			IndexedDenseDoubleMatrix2D matrix) throws RInterfaceException {

		if (rEngine == null) {
			throw new RInterfaceException("R engine not yet started.");
		}

		long rniP = putDataFrame(matrix);

		rEngine.rniAssign("jgrtemp", rniP, 0);
		rEngine.eval(name + "=jgrtemp");

	}

	// TODO: this should create a matrix object, not dataframe

	private static long putDataFrame(IndexedDenseDoubleMatrix2D matrix) {

		int rows = matrix.rows();
		int cols = matrix.columns();

		int[] rowNames = new int[rows];
		long[] colVectorsP = new long[cols];

		// create a vector for each column (ie: variable) in
		// original matrix
		double[][] mArrayTransposed = matrix.viewDice().toArray();
		for (int i = 0; i < cols; i++) {
			colVectorsP[i] = rEngine.rniPutDoubleArray(mArrayTransposed[i]);
			// this.rEngine.rniProtect(objP);
			// this.rniProtectedCounter++;
		}

		// create an array of row names (1,2,3....)
		for (int i = 0; i < rows; i++) {
			rowNames[i] = i + 1;
		}

		// TODO: protect
		// tells R that the object is in use so it is not destroyed
		// The protection mechanism is stack-based, so UNPROTECT(n)
		// unprotects the last n objects which were protected.
		// The calls to PROTECT and UNPROTECT must balance when the code
		// returns.
		// see
		// http://cran.r-project.org/doc/manuals/R-exts.html#Garbage-Collection

		// create a list from all the vectors
		long dataframeP = rEngine.rniPutVector(colVectorsP);
		// this.rEngine.rniProtect(objP);
		// this.rniProtectedCounter++;

		// convert the list to a dataframe

		// define the column names
		rEngine.rniSetAttr(dataframeP, "names", rEngine
				.rniPutStringArray(matrix.getColumnNames()));

		// define the row names
		rEngine.rniSetAttr(dataframeP, "row.names", rEngine
				.rniPutIntArray(rowNames));

		// set the class attribute to dataframe
		rEngine.rniSetAttr(dataframeP, "class", rniP_dataframeClass);

		return dataframeP;

	}
}