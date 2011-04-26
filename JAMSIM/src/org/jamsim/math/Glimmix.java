package org.jamsim.math;

import java.util.LinkedHashMap;
import java.util.Map;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.data.model.CDataRowSet;
import net.casper.data.model.CRowMetaData;

/**
 * Generalised linear mixed model (GLIMMIX).
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class Glimmix {

	/**
	 * Map containing effect names to estimate value mappings.
	 */
	private final Map<String, Double> effectEstimates =
			new LinkedHashMap<String, Double>();

	/**
	 * Construct {@link Glimmix} from casper container. The container must have
	 * a string column called "Effect" which specifies the variable name, and a
	 * double column called "Estimate" which specifies the variables
	 * coefficient.
	 * 
	 * @param container
	 *            casper container
	 * @throws CDataGridException
	 *             if problem reading container
	 */
	public Glimmix(CDataCacheContainer container) throws CDataGridException {
		CRowMetaData meta = container.getMetaDefinition();
		CDataRowSet rows = container.getAll();

		while (rows.next()) {
			String effect = rows.getString("Effect");

			// if this effect has levels, append the level
			// to the effect name
			if (meta.containsColumn(effect)) {
				effect = effect + "Lvl" + rows.getString(effect);
			}

			effectEstimates.put(effect, rows.getDouble("Estimate"));
		}
	}

	/**
	 * Calculates the sum of the product of supplied values joined with the
	 * effect estimates (coefficients) stored in this model.
	 * 
	 * @param values
	 *            set of values with same names as this model. These values are
	 *            multiplied with the model's effect estimates (coefficients).
	 * @return sum
	 */
	public double sumOfProducts(Map<String, ? extends Number> values) {
		double result = 0;
		for (Map.Entry<String, Double> entry : effectEstimates.entrySet()) {
			String effect = entry.getKey();
			double coefficient = entry.getValue();

			if (!values.containsKey(effect)) {
				throw new IllegalArgumentException("Missing value for "
						+ effect);
			}

			double value = values.get(effect).doubleValue();

			result = result + coefficient * value;
		}
		return result;
	}

	/**
	 * Evaluate the logit probability of {@link #sumOfProducts(Map)} of values.
	 * Probability = exp(logit)/(1+exp(logit)) where logit =
	 * sumOfProducts(values) + gamma.
	 * 
	 * @param values
	 *            set of values with same names as this model. These values are
	 *            multiplied with the model's effect estimates (coefficients).
	 * @param gamma
	 *            gamma to add to the sum of products
	 * @return logit probability
	 */
	public double evaluateLogitProb(Map<String, ? extends Number> values,
			double gamma) {
		double logit = sumOfProducts(values) + gamma;
		double prob = MathUtil.probFromLogit(logit);
		return prob;
	}

	/**
	 * Evaluate logit probability then randomly draw a number from a uniform
	 * distribution and compare to the logit probability. If
	 * random.nextUniform01() <= logitProb ? 1 : 2.
	 * 
	 * @param values
	 *            set of values with same names as this model. These values are
	 *            multiplied with the model's effect estimates (coefficients).
	 * @param gamma
	 *            gamma to add to the sum of products
	 * @param random
	 *            random number generator
	 * @return random.nextUniform01() <= prob ? 1 : 2.
	 */
	public int binaryLogitDraw(Map<String, ? extends Number> values,
			double gamma, RNG random) {
		double logitProb = evaluateLogitProb(values, gamma);

		return random.nextUniform01() <= logitProb ? 1 : 2;
	}

	/**
	 * Return Math.exp({@link Glimmix#sumOfProducts(Map)}. If value is outside
	 * min or max, it will be set to the min/max if {@code limit = true}.
	 * 
	 * @param values
	 *            set of values with same names as this model. These values are
	 *            multiplied with the model's effect estimates (coefficients).
	 * @param gamma
	 *            gamma to add to the sum of products
	 * @param limit
	 *            toggle to determine whether min/max limits are applied.
	 * @param min
	 *            min limit. If {@code limit = true} results less than min will
	 *            be returned as min.
	 * @param max
	 *            max limit. If {@code limit = true} results greater than max
	 *            will be returned as max.
	 * @return integer value
	 */
	public int evaluateInteger(Map<String, ? extends Number> values,
			double gamma, boolean limit, int min, int max) {
		double model = sumOfProducts(values) + gamma;
		double value = Math.exp(model);

		int intValue = (int) Math.round(value);
		if (limit) {
			if (intValue < min) {
				intValue = min;
			} else if (intValue > max) {
				intValue = max;
			}
		}
		return intValue;
	}

	/**
	 * Get underlying map of effect names and estimates.
	 * 
	 * @return effect estimates.
	 */
	public Map<String, Double> getEffectEstimates() {
		return effectEstimates;
	}

	@Override
	public String toString() {
		return effectEstimates.toString();
	}

}
