package org.jamsim.ascape.weights;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.prefs.Preferences;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.io.MutableNumeratorTableModel;
import org.jamsim.math.MathUtil;
import org.jamsim.math.MutableNumerator;
import org.jamsim.shared.InvalidDataException;
import org.omancode.r.RInterfaceException;
import org.omancode.r.RUtil;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPMismatchException;

/**
 * Calculates weights for each factor level of a single variable.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class SingleVarWeightCalc extends Observable implements
		WeightCalculator {

	/**
	 * The name of the R variable, eg: {@code sol1}.
	 */
	private final String variableName;

	/**
	 * Variable description. Used for display purposes.
	 */
	private final String variableDesc;

	/**
	 * Variable factor levels and their weighting. A map version for lookup.
	 */
	private final Map<Double, MutableNumerator> factorLevelWeights;

	/**
	 * Weights at each factor level. An array version of
	 * {@link #factorLevelWeights} for iteration.
	 */
	private final MutableNumerator[] weights;

	/**
	 * A {@link TableModel} wrapped around {@link #weights}.
	 */
	private final AbstractTableModel tableModel;

	/**
	 * Weights are between 0 and 1. Adjustment factor used for display purposes
	 * only.
	 */
	private static final int ADJ_FACTOR = 100;

	/**
	 * Construct a set of weightings at each factor level of {@code
	 * variableName}.
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
	 * @throws RInterfaceException
	 *             if problem reading category proportions of {@code variable}
	 *             from R
	 * @throws InvalidDataException
	 *             if weights do not sum to 1. See {@link #validate()}.
	 */
	public SingleVarWeightCalc(ScapeRInterface scapeR, String rVariable,
			String variableName, String variableDesc)
			throws RInterfaceException, InvalidDataException {

		this.variableName = variableName;
		this.variableDesc = variableDesc;
		this.factorLevelWeights = getFactorLevelsWithProp(rVariable, scapeR);
		this.weights =
				factorLevelWeights.values().toArray(
						new MutableNumerator[factorLevelWeights.size()]);
		this.tableModel =
				new MutableNumeratorTableModel(weights, ADJ_FACTOR, 1);

		validate();

	}

	/**
	 * Construct a set of weightings at each factor level of {@code
	 * variableName} and load the initial values of the weight numerators from
	 * prefs.
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
	 * @param prefs
	 *            Preferences that store the state of the weightings
	 * @throws RInterfaceException
	 *             if problem reading category proportions of {@code variable}
	 *             from R
	 * @throws InvalidDataException
	 *             if weights do not sum to 1. See {@link #validate()}.
	 */
	public SingleVarWeightCalc(ScapeRInterface scapeR, String rVariable,
			String variableName, String variableDesc, Preferences prefs)
			throws RInterfaceException, InvalidDataException {
		this(scapeR, rVariable, variableName, variableDesc);
		loadState(prefs);
	}

	/**
	 * Gets a map of factor levels for {@code variable} and calculates the
	 * proportion (ie: factor level count / total counts) at each factor level.
	 * 
	 * @param variable
	 *            variable name
	 * @param scapeR
	 *            scape R interface
	 * @return an map of {@link MutableNumerator} for each factor level with the
	 *         denominator and numerator set to the proportion of counts at each
	 *         factor level. The map key represents the factor level value.
	 * @throws RInterfaceException
	 */
	private Map<Double, MutableNumerator> getFactorLevelsWithProp(
			String variable, ScapeRInterface scapeR)
			throws RInterfaceException {
		String cmd = "prop.table(table(" + variable + "))";

		REXP rexp = scapeR.parseEvalTry(cmd);

		// r command must return a REXPDouble
		if (!(rexp instanceof REXPDouble)) {
			throw new RInterfaceException(cmd + " returned "
					+ rexp.getClass().getCanonicalName());
		}

		// get names. these are the factors.
		String[] valueNames = RUtil.getNamesAttribute(rexp);
		if (valueNames == null) {
			throw new RInterfaceException("Result of " + cmd
					+ " does not supply names attribute.");
		}

		// get values
		double[] values;
		try {
			values = rexp.asDoubles();
		} catch (REXPMismatchException e) {
			throw new RInterfaceException(e);
		}

		// create MutableNumerators with the denominator and numerator
		// equal to the count proportion
		Map<Double, MutableNumerator> adjFactors =
				new LinkedHashMap<Double, MutableNumerator>(values.length);
		for (int i = 0; i < values.length; i++) {
			MutableNumerator num =
					new MutableNumerator(valueNames[i], values[i], values[i]);
			adjFactors.put(Double.valueOf(valueNames[i]), num);
		}

		return adjFactors;
	}

	/**
	 * Lookup the variable name and return the appropriate factor weight based
	 * on the value of the variable in {@code vars}.
	 * 
	 * @param vars
	 *            map of variable names and values
	 * @return weight for value of variable in {@code vars}.
	 */
	public double getWeight(Map<String, Double> vars) {
		Double var = vars.get(variableName);

		MutableNumerator factorReweight = factorLevelWeights.get(var);

		if (factorReweight == null) {
			throw new IllegalStateException(
					"Cannot find reweighting value for " + variableName
							+ " with value = " + var);
		}

		double weight = factorReweight.getFraction();

		return weight;
	}

	@Override
	public final String getName() {
		return variableDesc;
	}

	@Override
	public TableModel getTableModel() {
		return tableModel;
	}

	@Override
	public void validateAndNotify() throws InvalidDataException {
		validate();

		// notify all observers
		setChanged();
		notifyObservers();
	}

	@Override
	public void resetDefaults() {

		// reset weights
		for (MutableNumerator num : weights) {
			num.setNumerator(num.getDenominator());
		}
		tableModel.fireTableDataChanged();

		// notify all observers
		setChanged();
		notifyObservers();

	}

	/**
	 * Validate weightings, ie: make sure they sum to 1.
	 * 
	 * @throws InvalidDataException
	 *             if weightings cannot be validated
	 */
	public final void validate() throws InvalidDataException {
		double total = 0;

		for (MutableNumerator adj : weights) {
			total += adj.doubleValue();
		}

		if (!MathUtil.equals(total, 1)) {
			throw new InvalidDataException("Weights (" + total * ADJ_FACTOR
					+ ") must add to " + ADJ_FACTOR);
		}

	}

	@Override
	public final void loadState(Preferences prefs) {
		String key = WeightCalculator.WCALC_KEY + " " + getName();
		String savedNumStr = prefs.get(key, "");

		if (savedNumStr.length() > 0) {
			tableModel.fireTableDataChanged();

			String[] savedNums = savedNumStr.split(",");

			if (savedNums.length == weights.length) {
				for (int i = 0; i < weights.length; i++) {
					weights[i].setNumerator(Double.parseDouble(savedNums[i]));
				}
			}
		}
	}

	@Override
	public void saveState(Preferences prefs) {
		StringBuffer sb = new StringBuffer(32);

		// construct string of numerators
		for (int i = 0; i < weights.length - 1; i++) {
			sb.append(weights[i].doubleValue());
			sb.append(", ");
		}
		sb.append(weights[weights.length - 1].doubleValue());

		String key = WeightCalculator.WCALC_KEY + " " + getName();
		prefs.put(key, sb.toString());
	}
}
