package org.jamsim.ascape.output;

import java.util.LinkedList;
import java.util.List;

import net.casper.data.model.CBuilder;
import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

import org.jamsim.matrix.CBuildFromMatrix;
import org.jamsim.matrix.IndexedDenseDoubleMatrix2D;
import org.omancode.util.ArrayUtil;

/**
 * Implementation of {@link MultiRunOutputDatasetProvider} based on an array of
 * doubles. Constructs a single run from the values provided by
 * {@link #getValues(int)} and {@link #getValueNames()}. Constructs a multiple
 * run dataset from all {@link #getValues(int)} calls made.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public abstract class Abstract1DMultiRunOutputDataset implements
		MultiRunOutputDatasetProvider {

	private final String name;
	private final String shortName;
	private final List<double[]> valuesFromAllRuns =
			new LinkedList<double[]>();
	private final String columnHeading;
	private final boolean datasetEachRun;

	/**
	 * Default constructor. Does not return individual run datasets, instead
	 * only returns a multi-run dataset at the end of the simulation.
	 * 
	 * @param shortName
	 *            short name. This will become the name of the dataframe created
	 *            in R to hold these results.
	 * @param name
	 *            name
	 * @param columnHeading
	 *            column heading of the dataset column that contains the value
	 *            names
	 */
	public Abstract1DMultiRunOutputDataset(String shortName, String name,
			String columnHeading) {
		this(shortName, name, columnHeading, false);
	}

	/**
	 * Master constructor.
	 * 
	 * @param shortName
	 *            short name. This will become the name of the dataframe created
	 *            in R to hold these results.
	 * @param name
	 *            name
	 * @param columnHeading
	 *            column heading of the dataset column that contains the value
	 *            names
	 * @param datasetEachRun
	 *            if {@code true} returns a dataset for each run and a multi-run
	 *            dataset at the end. If {@code false} only returns a multi-run
	 *            dataset at the end.
	 */
	public Abstract1DMultiRunOutputDataset(String shortName, String name,
			String columnHeading, boolean datasetEachRun) {
		this.datasetEachRun = datasetEachRun;
		this.columnHeading = columnHeading;
		this.name = name;
		this.shortName = shortName;
	}

	/**
	 * Return dataset of collector function values from all runs. i.e: all calls
	 * to {@link #getValues(int)}. First column is provided by
	 * {@link #getValueNames()}, and then each following column is a particular
	 * run, eg: Run 1, Run 2, Run 3 ...etc.
	 * 
	 * @return dataset of collector function values from all runs
	 * @throws CDataGridException
	 *             if problem creating dataset
	 */
	@Override
	public CDataCacheContainer getMultiRunDataset() throws CDataGridException {
		int numberRuns = valuesFromAllRuns.size();
		double[][] array =
				ArrayUtil.transpose(valuesFromAllRuns
						.toArray(new double[numberRuns][]));

		IndexedDenseDoubleMatrix2D allRuns =
				new IndexedDenseDoubleMatrix2D(
						new String[] { columnHeading }, getValueNames(),
						runNumbers(numberRuns), array);

		CBuilder builder =
				new CBuildFromMatrix(name + " (All runs)", allRuns);

		return new CDataCacheContainer(builder);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Output dataset for the given run number. This is a dataset wrapped around
	 * {@link #getValues(int)} and {@link #getValueNames()}.
	 * 
	 * @param run
	 *            run number
	 * @return collector function values.
	 * @throws CDataGridException
	 *             if problem creating dataset
	 */
	@Override
	public CDataCacheContainer getOutputDataset(int run)
			throws CDataGridException {

		// Store this run
		valuesFromAllRuns.add(getValues(run));

		if (datasetEachRun) {
			return getValuesAsDataset(run);
		}
		return null;

	}

	/**
	 * Output dataset for the given run number. This is a dataset wrapped around
	 * {@link #getValues(int)} and {@link #getValueNames()}.
	 * 
	 * @param run
	 *            run number
	 * @return collector function values.
	 * @throws CDataGridException
	 *             if problem creating dataset
	 */
	private CDataCacheContainer getValuesAsDataset(int run)
			throws CDataGridException {
		// Set up new Casper container for the results
		String columnNames = columnHeading + ",Value";
		final Class<?>[] columnTypes =
				new Class[] { String.class, Double.class };

		CDataCacheContainer container =
				CDataCacheContainer.newInsertionOrdered(getName(),
						columnNames, columnTypes);

		// Fill container with values from the arrays
		double[] values = getValues(run);
		String[] names = getValueNames();

		if (values.length != names.length) {
			throw new IllegalArgumentException("\"" + getShortName()
					+ "\" values.length (" + values.length
					+ ") != names.length (" + names.length + ")");
		}

		for (int i = 0; i < values.length; i++) {
			container.addSingleRow(new Object[] { names[i], values[i] });
		}

		return container;

	}

	@Override
	public String getShortName() {
		return shortName;
	}

	/**
	 * Create a string array of the form {"Run 1", "Run 2", "Run 3" .. etc }.
	 * 
	 * @param numberRuns
	 *            number of runs
	 * @return string array of runs
	 */
	private static String[] runNumbers(int numberRuns) {
		String[] runNums = new String[numberRuns];

		for (int i = 0; i < runNums.length; i++) {
			runNums[i] = "Run " + (i + 1);
		}

		return runNums;
	}

	/**
	 * Output data values for the given run number.
	 * 
	 * @param run
	 *            simulation run number
	 * @return output data values.
	 */
	public abstract double[] getValues(int run);

	/**
	 * Name of each value returned {@link #getValues(int)}.
	 * 
	 * @return value names
	 */
	public abstract String[] getValueNames();
}
