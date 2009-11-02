package org.jamsim.matrix;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * Matrix utility functions. Contains methods for manipulating matrices.
 * <p>
 * Uses the <a href="http://acs.lbl.gov/~hoschek/colt/">COLT</a> libraries.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class MatrixUtil {

	/**
	 * Multiply each row in a 2D matrix by a 1D matrix
	 * 
	 * @param m2
	 *            Source 2D matrix
	 * @param m1
	 *            1D matrix to multiple each row of m2 by
	 * @return A new 2D matrix, with the same dynamic type as m2, with each
	 *         column of each row multiplied by the corresponding column in m1
	 */
	public static DoubleMatrix2D mult(DoubleMatrix2D m2, DoubleMatrix1D m1) {
		DoubleMatrix2D result = m2.copy();

		for (int i = m2.rows(); --i >= 0;) {
			result.viewRow(i).assign(m1, cern.jet.math.Functions.mult);
		}

		return result;
	}

	/**
	 * For each row in mToAdjust, multiply mToAdjust by this row and save the
	 * result to an array of matrices.
	 * 
	 * @param mToAdjust
	 *            matrix to be adjusted
	 * @param mAdjustBy
	 *            matrix used to adjust {@code mToAdjust}
	 * @return DoubleMatrix2D[] array of adjusted matrices
	 */
	public static DoubleMatrix2D[] createAdjustedMatrixArray(
			DoubleMatrix2D mToAdjust, DoubleMatrix2D mAdjustBy) {
		int numAdjustments = mAdjustBy.rows();
		DoubleMatrix2D[] result = new DoubleMatrix2D[numAdjustments];

		for (int i = 0; i < numAdjustments; i++) {
			result[i] = mult(mToAdjust, mAdjustBy.viewRow(i));
		}

		return result;
	}

}
