package org.jamsim.ascape.weights;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.prefs.Preferences;

import javax.swing.table.TableModel;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.data.model.CDataRowSet;

import org.apache.commons.lang.NotImplementedException;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.math.ArrayMath;
import org.jamsim.shared.InvalidDataException;
import org.omancode.math.NamedNumber;
import org.omancode.r.RFaceException;
import org.omancode.r.RUtil;
import org.omancode.r.types.RDataFrame;
import org.omancode.r.types.REXPAttr;
import org.omancode.r.types.REXPUtil;
import org.omancode.r.types.UnsupportedTypeException;
import org.omancode.util.StringUtil;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;

/**
 * Displays levels of a continuous variable and allows user to add an amount to
 * each level.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ContinuousVarAdjustment extends Observable implements
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
	 * Variable description. Used for display purposes.
	 */
	private final String variableDesc;

	/**
	 * An R expression specifying either a numeric vector of two or more cut
	 * points, or a single number giving the size of the bins in which case x is
	 * divided in bins of size breaks.
	 */
	private final String breaksExpr;

	/**
	 * The left hand side of the last break, or {@code null} to use the max
	 * value.
	 */
	private final Double breakLast;

	/**
	 * Amount to adjust user entered increment value before applying, or
	 * {@code 1} to apply no adjustment.
	 */
	private final double adjIncrements;

	/**
	 * A {@link TableModel} wrapped around binned levels.
	 */
	private ContinuousVarAdjTableModel tableModel;

	private final ScapeRInterface scapeR;

	/**
	 * Construct incrementable proportions at each level of
	 * {@code rVariableName} specified by {@code breaksExpr} and load the inital
	 * increment amounts from {@code prefs}.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param rVariable
	 *            the R variable that will be used as the basis for bin
	 *            incrementing, eg: {@code children$bwkg}
	 * @param variableName
	 *            the name of the R variable, eg: {@code sol1}. Used to lookup
	 *            variable description in the data dictionary.
	 * @param breaksExpr
	 *            an R expression specifying either a numeric vector of two or
	 *            more cut points, or a single number giving the size of the
	 *            bins in which case x is divided in bins of size breaks. If
	 *            this expression has names then these are the labels for the
	 *            levels of the resulting category. If no names, labels are
	 *            constructed using "(a,b]" interval notation.
	 * @param breakLast
	 *            the left hand side of the last break, or {@code null} to use
	 *            the max value.
	 * @param adjIncrements
	 *            amount to adjust user entered increment value before applying,
	 *            or {@code 1} to apply no adjustment.
	 * @param prefs
	 *            Preferences that store the state of the weightings
	 * @throws RFaceException
	 *             if problem getting rVariable
	 */
	public ContinuousVarAdjustment(ScapeRInterface scapeR, String rVariable,
			String variableName, String breaksExpr, Double breakLast,
			double adjIncrements, Preferences prefs) throws RFaceException {
		this(scapeR, rVariable, variableName, breaksExpr, breakLast,
				adjIncrements);
		loadState(prefs);
	}

	/**
	 * Construct incrementable proportions at each level of
	 * {@code rVariableName} specified by {@code breaksExpr}.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param rVariable
	 *            the R variable that will be used as the basis for weighting,
	 *            eg: {@code children$sol1}
	 * @param variableName
	 *            the name of the R variable, eg: {@code sol1}. Used to lookup
	 *            variable description in the data dictionary.
	 * @param breaksExpr
	 *            an R expression specifying either a numeric vector of two or
	 *            more cut points, or a single number giving the size of the
	 *            bins in which case x is divided in bins of size breaks. If
	 *            this expression has names then these are the labels for the
	 *            levels of the resulting category. If no names, labels are
	 *            constructed using "(a,b]" interval notation.
	 * @param breakLast
	 *            the left hand side of the last break, or {@code null} to use
	 *            the max value.
	 * @param adjIncrements
	 *            amount to adjust user entered increment value before applying,
	 *            or {@code 1} to apply no adjustment.
	 * @throws RFaceException
	 *             if problem getting rVariable
	 */
	public ContinuousVarAdjustment(ScapeRInterface scapeR, String rVariable,
			String variableName, String breaksExpr, Double breakLast,
			double adjIncrements) throws RFaceException {

		this.rVariable = rVariable;
		this.variableDesc = scapeR.getMsScape().getDictionary()
				.getDescription(variableName);
		this.breaksExpr = breaksExpr;
		this.breakLast = breakLast;
		this.adjIncrements = adjIncrements;
		this.scapeR = scapeR;

		getTableModel();

		// store copy of original
		this.rVariableOriginal = ".original." + variableName;
		scapeR.assign(rVariableOriginal, rVariable);

	}

	private NamedNumber[] getBinLevelsWithCount(ScapeRInterface scapeR,
			String rVariable, String breaksExpr, Double breakLast)
			throws RFaceException {

		// eg: table(bin(children$bwkg,0.5))
		String rcmd = cmdTableBin(rVariable, breaksExpr, breakLast);
		// System.out.println(rcmd);

		NamedNumber[] counts = scapeR.parseEvalTryReturnNamedNumber(rcmd);
		return counts;

	}

	private String cmdTableBin(String rVariable, String breaksExpr,
			Double breakLast) {
		// eg: prop.table(table(bin(children$bwkg,0.5)))

		// NB: useNA='ifany' to pick up any mis-specified breaks
		return StringUtil.functionCall("prop.table", StringUtil.functionCall(
				"table", cmdBin(rVariable, breaksExpr, breakLast),
				"useNA='ifany'"));
	}

	private String cmdBin(String rVariable, String breaksExpr, Double breakLast) {
		// eg: bin(children$bwkg,0.5)
		return StringUtil.functionCall("bin", rVariable, breaksExpr,
				"breaklast=" + RUtil.asNullString(breakLast));

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

	@Override
	public final TableModel getTableModel() {

		try {
			NamedNumber[] counts = getBinLevelsWithCount(scapeR, rVariable,
					breaksExpr, breakLast);

			this.tableModel = new ContinuousVarAdjTableModel(counts);

			return tableModel;

		} catch (RFaceException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
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
			assignRVariable(rVariableOriginal);
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
		// eg: incByFactor(children$bwkg, bin(children$bwkg, 0.5),
		// c(0,0.5,0,0,0,0,0,0,0,0))
		String cBin = cmdBin(rVariable, breaksExpr, breakLast);
		String incrementBins = StringUtil.functionCall("incByFactor",
				rVariable, cBin, ".binIncrements");
		// System.out.println(incrementBins);
		assignRVariable(incrementBins);
	}

	/**
	 * Set rVariable to rexpr.
	 * 
	 * @param rexpr
	 * 
	 * @throws RFaceException
	 *             if problem setting
	 */
	private void assignRVariable(String rexpr) throws RFaceException {
		// assign rvariable
		// eg: children$bwkg <- incrementBins(...)
		scapeR.assign(rVariable, rexpr);

		scapeR.printToConsole("Adjusted continuous variable " + rVariable
				+ " by ");

		scapeR.parseEvalPrint("cat(.binIncrements)");
		scapeR.printlnToConsole("");

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
		tableModel.setProps(getBinLevelsWithCount(scapeR, rVariable,
				breaksExpr, breakLast));
	}

	/**
	 * Get increments with adjustment applied.
	 * 
	 * @return increments.
	 */
	public double[] getIncrements() {
		return ArrayMath.multiply(tableModel.getIncrements(), adjIncrements);
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

	/**
	 * Create a list of {@link ContinuousVarAdjustment}s from a dataframe
	 * containing the rows rVariable, variableName, breaksExpr, breakLast,
	 * adjIncrements.
	 * 
	 * @param scapeR
	 *            scapeR
	 * @param dataframe
	 *            dataframe
	 * @return list
	 * @throws IOException
	 *             if problem creating list
	 */
	public static List<ContinuousVarAdjustment> createList(
			ScapeRInterface scapeR, REXP dataframe) throws IOException {

		if (!RDataFrame.isDataFrame(dataframe)) {
			throw new IllegalArgumentException(
					"Cannot build list from REXP of class "
							+ REXPAttr.getClassAttribute(dataframe));
		}

		try {

			RDataFrame builder = new RDataFrame("ContinuousVarAdjustmentsSpec",
					dataframe);

			CDataCacheContainer container = new CDataCacheContainer(builder);

			CDataRowSet rowset = container.getAll();

			List<ContinuousVarAdjustment> cvas = new ArrayList<ContinuousVarAdjustment>(
					container.size());

			while (rowset.next()) {

				String rVariable = rowset.getString("rVariable");
				String variableName = rowset.getString("variableName");
				String breaksExpr = rowset.getString("breaksExpr");
				double breakLast = rowset.getDouble("breakLast");
				double adjIncrements = rowset.getDouble("adjIncrements");

				ContinuousVarAdjustment cva = new ContinuousVarAdjustment(
						scapeR, rVariable, variableName, breaksExpr, breakLast,
						adjIncrements);

				cvas.add(cva);
			}

			return cvas;

		} catch (UnsupportedTypeException e) {
			throw new RFaceException(e.getMessage(), e);
		} catch (CDataGridException e) {
			throw new RFaceException(e.getMessage(), e);
		}
	}
}
