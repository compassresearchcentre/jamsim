package org.jamsim.ascape.output;

import java.util.LinkedList;
import java.util.List;

import net.casper.data.model.CBuilder;
import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.io.file.util.ArrayUtil;

import org.jamsim.matrix.CBuildFromMatrix;
import org.jamsim.matrix.IndexedDenseDoubleMatrix2D;

public abstract class MultiRunOutputDatasetImpl implements
		MultiRunOutputDatasetProvider {

	private final String name;
	private final String shortName;
	private final List<double[]> valuesFromAllRuns =
			new LinkedList<double[]>();
	private final String columnHeading;

	public MultiRunOutputDatasetImpl(String name, String shortName, String columnHeading) {
		this.columnHeading = columnHeading;
		this.name = name;
		this.shortName = shortName;
	}

	/**
	 * Return dataset of collector function values from all runs. First column
	 * is the collector functions names, and then each following column is a
	 * particular run, eg: Run 1, Run 2, Run 3 ...etc.
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
	 * Return collector function values as a dataset.
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

		for (int i = 0; i < values.length; i++) {
			container.addSingleRow(new Object[] { names[i], values[i] });
		}

		// Store this run
		valuesFromAllRuns.add(getValues(run));

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
