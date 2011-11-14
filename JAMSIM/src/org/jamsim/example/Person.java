package org.jamsim.example;

import java.util.HashMap;
import java.util.Map;

import net.casper.data.model.CDataGridException;
import net.casper.data.model.CMarkedUpRow;

import org.jamsim.ascape.MicroSimCell;
import org.jamsim.ascape.weights.WeightCalculator;
import org.jamsim.data.DataUtil;
import org.jamsim.example.types.AGE_GRP;
import org.jamsim.example.types.Disability;
import org.jamsim.example.types.SEX;
import org.jamsim.math.RNG;

/**
 * A person.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class Person extends MicroSimCell<JEMData> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8630987348628357814L;

	private final JEMData mdata;

	private final RNG rng;

	private int age;

	private AGE_GRP ageGrp;

	private SEX sex;

	private double weight;

	private double weightBase;
	
	private boolean alive;

	private int[] earnings_to_date;

	private int[] disability_state;

	private Disability current_disability_state;

	private int total_earnings;

	private Map<String, Character> varsUsedToCalculateWeight =
			new HashMap<String, Character>();

	/**
	 * Default constructor. Before construction
	 * {@link MicroSimCell#setData(org.jamsim.ascape.ScapeData)} must be called
	 * to set globals.
	 */
	public Person() {
		mdata = getScapeData();
		rng = (mdata != null) ? mdata.getRNG() : null;
	}

	/**
	 * Get the {@link CMarkedUpRow} which is exported to R.
	 * 
	 * @return marked up row
	 */
	@Override
	public CMarkedUpRow getMarkedUpRow() {
		return null;
	}

	/**
	 * Load the {@link Person}'s attributes from a row and setup simulation
	 * modules.
	 * 
	 * @param row
	 *            row to load attributes from
	 * 
	 * @throws CDataGridException
	 *             if problem reading row
	 */
	@Override
	public void setMarkedUpRow(CMarkedUpRow row) throws CDataGridException {
		// load attributes
		char sexChar = row.getChar("sex");
		varsUsedToCalculateWeight.put("sex", sexChar);
		sex = SEX.get(sexChar);

		// initialize here, before the simulation begins
		initialize();
	}

	/**
	 * Called at the beginning of each run. Reset changed variables.
	 */
	@Override
	public void initialize() {
		age = 0;
		ageGrp = AGE_GRP.getFromAge(age);
		earnings_to_date = DataUtil.missingIntArray(JEMScape.NUM_ITERATIONS);
		disability_state = DataUtil.missingIntArray(JEMScape.NUM_ITERATIONS);
		current_disability_state = Disability.NO_DIS;
		total_earnings = 0;
		alive = true;
	}

	/**
	 * Performs simulation on this {@link Person} each time step.
	 */
	@Override
	public void iterate() {
		int iterationIndex = getIteration() - 1;

		if (alive) {

			if (age > JEMDataDefn.MAX_LIFE) {
				alive = false;
			} else {

				// determine the disability status of the person
				current_disability_state =
						mdata.lookupProbdfle(rng.nextUniform01(), sex, ageGrp,
								current_disability_state);

				disability_state[iterationIndex] =
						current_disability_state.getValue();

				// assign an appropriate annual income to the person based on
				// the disability status
				total_earnings +=
						mdata.lookupEarningsScale(current_disability_state);

				earnings_to_date[iterationIndex] = total_earnings;

				age++;
				ageGrp = AGE_GRP.getFromAge(age);

				// determine whether the person dies this year
				if (rng.nextUniform01() <= mdata.probabilityOfDeath(sex,
						age - 1)) {
					alive = false;
				}

			}
		}
	}

	@Override
	public String toString() {
		return sex + " " + age;
	}

	public int getAge() {
		return age;
	}

	public char getSex() {
		return sex.getValue();
	}

	public int[] getEarningsToDate() {
		return earnings_to_date;
	}

	public int getTotalEarnings() {
		return total_earnings;
	}

	public int getCurrentDisabilityState() {
		return current_disability_state.getValue();
	}
	
	public int[] getDisabilityState() {
		return disability_state;
	}

	public boolean getAlive() {
		return alive;
	}

	public double getWeightScenario() {
		return weight;
	}

	public double getWeightBase() {
		return weightBase;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public void setWeight(WeightCalculator weights) {
		setWeight(weights.getLevelWeight(varsUsedToCalculateWeight));
		this.weightBase = weights.getWeightBase();
	}

}
