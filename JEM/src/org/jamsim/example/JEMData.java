package org.jamsim.example;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.table.TableModel;

import org.jamsim.ascape.DataDictionary;
import org.jamsim.ascape.MicroSimCell;
import org.jamsim.ascape.ScapeData;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.ascape.weights.SingleVarWeightCalc;
import org.jamsim.ascape.weights.WeightCalculator;
import org.jamsim.example.types.AGE_GRP;
import org.jamsim.example.types.Disability;
import org.jamsim.example.types.SEX;
import org.jamsim.io.FileLoader;
import org.jamsim.io.ParameterSet;
import org.jamsim.math.ColtRNG;
import org.jamsim.math.IntervalsIntMap;
import org.jamsim.math.RNG;
import org.jamsim.matrix.IndexedDenseDoubleMatrix2D;

/**
 * Global data for JEM.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class JEMData implements ScapeData {

	private final FileLoader loader;

	private final DataDictionary dict;

	private final RNG random;

	/**
	 * Table model representations of parameters for display in Navigator.
	 */
	private final Map<String, ParameterSet> parameterSets =
			new LinkedHashMap<String, ParameterSet>();

	/**
	 * Weight calculators.
	 */
	private Map<String, WeightCalculator> wcalcs;

	/**
	 * Model specific data -------------------
	 */

	private final int[] annual_earnings_scale;

	private final double[] prob_mort_male;

	private final double[] prob_mort_female;

	private final IndexedDenseDoubleMatrix2D dfle_transitions_matrix;

	/**
	 * Disability state transitions, ie: cumulative distributions for each
	 * sex/agegrp/current disability combination.
	 */
	private final IntervalsIntMap[][][] dfle_transition_cumdists;

	private int peopleCount;
	
	/**
	 * Constructor.
	 * 
	 * @param loader
	 *            file loader
	 * @throws IOException
	 *             if problem loading datasets
	 */
	public JEMData(FileLoader loader) throws IOException {
		this.loader = loader;

		this.random = new ColtRNG();
		// this.random = new NonRNG();
		
		loader.setDefaultFileLocations(JEMDataDefn.DEFAULT_FILE_LOCATIONS);

		dict = new DataDictionary(loader.loadMap(JEMDataDefn.DICTIONARY_MAP));

		annual_earnings_scale =
				loader.loadIntArray(JEMDataDefn.ANNUAL_EARNINGS_SCALE);

		prob_mort_female =
				loader.loadDoubleArray(JEMDataDefn.PROB_MORT_FEMALE);

		prob_mort_male = loader.loadDoubleArray(JEMDataDefn.PROB_MORT_MALE);

		dfle_transitions_matrix =
				loader.loadMatrix(JEMDataDefn.DFLE_TRANSITIONS);

		dfle_transition_cumdists =
				createDisabilityStateCDs(dfle_transitions_matrix);

	}

	/**
	 * Create a cumulative distribution for each sex/agegrp/current disability
	 * combination in the dfle_matrix.
	 * 
	 * @param dfle_matrix
	 *            Indexed matrix of number of visit (1 - 10) probabilities for
	 *            each agegrp/hhtype/gender and category combination.
	 * @return cumulative distributions for number of visits, by
	 *         agegrp/hhtype/gender index and condition category.
	 * 
	 */
	private final IntervalsIntMap[][][] createDisabilityStateCDs(
			IndexedDenseDoubleMatrix2D dfle_matrix) {

		IntervalsIntMap[][][] cumdists =
				new IntervalsIntMap[SEX.values().length][AGE_GRP.values().length][Disability
						.values().length];

		// create individual CDs for each sex/agegrp/current disability
		// combination from rows of the matrix
		for (SEX sex : SEX.values()) {
			for (AGE_GRP ageGrp : AGE_GRP.values()) {
				for (Disability dfle : Disability.values()) {
					Object[] key =
							new Object[] { sex.getValue(), ageGrp.getValue(),
									dfle.getValue() };
					int index = dfle_matrix.indexLookupRow(key);
					double[] probs = dfle_matrix.viewRow(index).toArray();

					IntervalsIntMap cd =
							IntervalsIntMap.newCumulativeDistribution(probs,
									Disability.allValues);

					cumdists[sex.ordinal()][ageGrp.ordinal()][dfle.ordinal()] =
							cd;
				}
			}
		}

		return cumdists;

	}

	/**
	 * Lookup the appropriate cumulative distribution based on gender, age, and
	 * current disability state. Draw the new disability state from the
	 * cumulative distribution using the supplied random number.
	 * 
	 * @param random
	 *            random number used for drawing from the cumulative
	 *            distribution
	 * @param sex
	 *            sex
	 * @param ageGrp
	 *            age
	 * @param dfle
	 *            current disability state
	 * @return new disability state
	 */
	public Disability lookupCumdfle(double random, SEX sex, AGE_GRP ageGrp,
			Disability dfle) {

		IntervalsIntMap cd =
				dfle_transition_cumdists[sex.ordinal()][ageGrp.ordinal()][dfle
						.ordinal()];

		int newDisability = cd.getMappedValue(random);

		return Disability.get(newDisability);

	}

	public int lookupEarningsScale(Disability disabilityState) {
		return annual_earnings_scale[disabilityState.ordinal()];
	}

	public double probabilityOfDeath(SEX sex, int age) {
		if (sex == SEX.FEMALE) {
			return prob_mort_female[age];
		} else {
			return prob_mort_male[age];
		}
	}

	/**
	 * Map of possible {@link WeightCalculator}s the user may select from.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @return maps of wcals
	 * @throws IOException
	 *             if problem creating the {@link WeightCalculator}s
	 */
	public Map<String, WeightCalculator> getWeightCalculators(
			ScapeRInterface scapeR) throws IOException {

		wcalcs = new LinkedHashMap<String, WeightCalculator>(1);
		
		double scaling = 69899568 / (double)peopleCount; 

		wcalcs.put("Sex", new SingleVarWeightCalc(scapeR, "people$sex",
				"sex", "Sex", scaling, loader.getPrefs()));

		return wcalcs;
	}

	@Override
	public Map<String, TableModel> getInputDatasets() {
		return loader.getTableModels();
	}

	@Override
	public Map<String, ParameterSet> getParameterSets() {
		return parameterSets;
	}

	@Override
	public RNG getRNG() {
		return random;
	}

	@Override
	public Collection<? extends MicroSimCell<? extends ScapeData>> loadAgents(
			File basefile) throws IOException {
		Collection<? extends MicroSimCell<? extends ScapeData>> people =
				loader.loadJSONMarkedUpBeans("JEM base file definition",
						basefile, Person.class);
		peopleCount = people.size();
		return people;
	}

	@Override
	public DataDictionary getDataDictionary() {
		return dict;
	}

}
