package org.jamsim.ascape.weights;

import java.util.Map;
import java.util.Observable;
import java.util.prefs.Preferences;

import javax.swing.table.TableModel;

import org.apache.commons.lang.NotImplementedException;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.shared.InvalidDataException;
import org.omancode.math.NamedNumber;
import org.omancode.r.RFaceException;
import org.omancode.r.types.REXPUtil;
import org.omancode.util.StringUtil;
import org.rosuda.REngine.REXPDouble;

/**
 * Displays levels of a continuous variable and allows user to add amount to
 * each level.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ContinuousVarWeightCalc extends Observable implements
		WeightCalculator {

	/**
	 * The R variable that will be used as the basis for bin incrementing, eg:
	 * {@code children$bwkg}.
	 */
	private final String rVariable;

	/**
	 * The name of a variable in r that stores the original values in rVariable.
	 */
	private final String rVariableOriginal;

	/**
	 * The name of the R variable, eg: {@code sol1}.
	 */
	private final String variableName;

	/**
	 * Variable description. Used for display purposes.
	 */
	private final String variableDesc;

	/**
	 * Size of the breaks to make in the continuous var.
	 */
	private final double binSize;

	/**
	 * The left hand side of the last break, or {@code null} to use the max
	 * value.
	 */
	private final Double breakLast;

	/**
	 * A {@link TableModel} wrapped around {@link #weights}.
	 */
	private final ContinuousVarWCTableModel tableModel;

	private final ScapeRInterface scapeR;

	/**
	 * Construct a set of weightings at each factor level of
	 * {@code variableName} and load the initial values of the weight numerators
	 * from prefs.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param rVariable
	 *            the R variable that will be used as the basis for bin
	 *            incrementing, eg: {@code children$bwkg}
	 * @param variableName
	 *            the name of the R variable, eg: {@code sol1}
	 * @param variableDesc
	 *            description of the R variable. Used for display purposes.
	 * @param binSize
	 *            size of the breaks to make in the continuous var
	 * @param breakLast
	 *            the left hand side of the last break, or {@code null} to use
	 *            the max value.
	 * @param prefs
	 *            Preferences that store the state of the weightings
	 * @throws RFaceException
	 *             if problem getting rVariable
	 */
	public ContinuousVarWeightCalc(ScapeRInterface scapeR, String rVariable,
			String variableName, String variableDesc, double binSize,
			Double breakLast, Preferences prefs) throws RFaceException {
		this(scapeR, rVariable, variableName, variableDesc, binSize,
				breakLast);
		loadState(prefs);
	}

	/**
	 * Construct a set of weightings from {@code variableName} at each break
	 * level of size {@code binSize} .
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param rVariable
	 *            the R variable that will be used as the basis for weighting,
	 *            eg: {@code children$sol1}
	 * @param variableName
	 *            the name of the R variable, eg: {@code sol1}
	 * @param variableDesc
	 *            description of the R variable. Used for display purposes.
	 * @param binSize
	 *            size of the breaks to make in the continuous var
	 * @param breakLast
	 *            the left hand side of the last break, or {@code null} to use
	 *            the max value.
	 * @throws RFaceException
	 *             if problem getting rVariable
	 */
	public ContinuousVarWeightCalc(ScapeRInterface scapeR, String rVariable,
			String variableName, String variableDesc, double binSize,
			Double breakLast) throws RFaceException {

		this.rVariable = rVariable;
		this.variableName = variableName;
		this.variableDesc = variableDesc;
		this.binSize = binSize;
		this.breakLast = breakLast;
		NamedNumber[] counts =
				getBinLevelsWithCount(scapeR, rVariable, binSize, breakLast);
		this.tableModel = new ContinuousVarWCTableModel(counts);
		this.scapeR = scapeR;

		// store copy of original
		this.rVariableOriginal = ".original." + variableName;
		scapeR.assign(rVariableOriginal, rVariable);

	}

	private NamedNumber[] getBinLevelsWithCount(ScapeRInterface scapeR,
			String rVariable, double binSize, Double breakLast)
			throws RFaceException {

		// eg: table(bin(children$bwkg,0.5))
		String rcmd = cmdTableBin(rVariable, binSize, breakLast);

		NamedNumber[] counts = scapeR.parseEvalTryReturnNamedNumber(rcmd);
		return counts;

	}

	private String cmdTableBin(String rVariable, double binSize,
			Double breakLast) {
		// eg: prop.table(table(bin(children$bwkg,0.5)))
		if (breakLast == null) {
			return StringUtil.functionCall("prop.table", StringUtil
					.functionCall("table", StringUtil.functionCall("bin",
							rVariable, binSize)));
		} else {
			return StringUtil.functionCall("prop.table", StringUtil
					.functionCall("table", StringUtil.functionCall("bin",
							rVariable, binSize, breakLast)));
		}

	}

	@Override
	public double getLevelWeight(Map<String, ?> vars) {
		throw new NotImplementedException("getLevelWeight not implemented.");
	}

	@Override
	public double[] getAllLevelProps() {
		throw new NotImplementedException("getAllLevelProps not implemented.");
	}

	@Override
	public final String getName() {
		return variableDesc;
	}

	/**
	 * Get the name of factor variable.
	 * 
	 * @return name
	 */
	public final String getFactorName() {
		return variableName;
	}

	@Override
	public TableModel getTableModel() {
		return tableModel;
	}

	@Override
	public void validateAndNotify() throws InvalidDataException {
		// notify all observers
		setChanged();
		notifyObservers();
	}

	@Override
	public void resetDefaults() {
		try {
			setRVariable(rVariableOriginal);
		} catch (RFaceException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Increment rVariable at the break levels.
	 * 
	 * @throws RFaceException
	 *             if problem incrementing.
	 */
	public void applyIncrements() throws RFaceException {
		REXPDouble binIncrements = REXPUtil.toVector(getIncrements());
		scapeR.assign(".binIncrements", binIncrements);

		// incrementBins(children$bwkg, 0.5, c(1,0,0,0,0,0,0,0,0))
		String incrementBins =
				(breakLast == null) ? StringUtil
						.functionCall("incrementBins", rVariable,
								".binIncrements", binSize) : StringUtil
						.functionCall("incrementBins", rVariable,
								".binIncrements", binSize, breakLast);

		setRVariable(incrementBins);
	}

	/**
	 * Set rVariable to rexpr.
	 * 
	 * @param rexpr
	 * 
	 * @throws RFaceException
	 *             if problem setting
	 */
	private void setRVariable(String rexpr) throws RFaceException {
		// assign rvariable
		// eg: children$bwkg <- incrementBins(...)
		scapeR.assign(rVariable, rexpr);

		scapeR.printlnToConsole("Updated levels for " + rVariable);

		scapeR.baseFileUpdated();

		recalculateLevels();
	}

	/**
	 * Calculate the current levels and update the table model.
	 * 
	 * @throws RFaceException
	 *             if problem calculating levels.
	 */
	private void recalculateLevels() throws RFaceException {
		tableModel.setProps(getBinLevelsWithCount(scapeR, rVariable, binSize,
				breakLast));
	}

	/**
	 * Get increments.
	 * 
	 * @return increments.
	 */
	public double[] getIncrements() {
		return tableModel.getIncrements();
	}

	@Override
	public final void loadState(Preferences prefs) {
	}

	@Override
	public void saveState(Preferences prefs) {
	}

	@Override
	public double getWeightBase() {
		return 1;
	}

}
