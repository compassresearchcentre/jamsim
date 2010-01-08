package org.jamsim.ascape.stats;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.casper.data.model.CBuilder;
import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.io.file.util.ArrayUtil;

import org.ascape.model.Scape;
import org.ascape.util.data.StatCollector;
import org.jamsim.matrix.CBuildFromMatrix;
import org.jamsim.matrix.IndexedDenseDoubleMatrix2D;

/**
 * Collection of {@link CollectorFunction}s created from
 * {@link StatsFunctionRow}s or {@link StatsPredicateRow}s.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class StatsRows implements StatsOutputModel,
		MultiRunOutputDatasetProvider {

	/**
	 * Default ratio multiplier used when none is specified by a constructor.
	 */
	public static final int DEFAULT_RATIO_MULTIPLIER = 100;

	private final Collection<CollectorFunction<?>> stats =
			new LinkedList<CollectorFunction<?>>();

	private final String columnHeading;
	private final String name;

	private final double ratioMultiplier;

	private final List<double[]> valuesFromAllRuns =
			new LinkedList<double[]>();

	private final String shortName;

	/**
	 * Invoke the constructor with the default ratio multiplier.
	 * 
	 * @param <T>
	 *            type of scape members
	 * @param shortName
	 *            short name
	 * @param name
	 *            name
	 * @param iteratingScape
	 *            scape. Used to get the current iteration.
	 * @param rows
	 *            rows to create in the table
	 * @param columnHeading
	 *            column heading
	 * @param function
	 *            value to collect from simulation.
	 */
	public <T> StatsRows(String shortName, String name, Scape iteratingScape,
			Collection<StatsPredicateRow<T>> rows, String columnHeading,
			StatsFunction<T> function) {
		this(shortName, name, iteratingScape, rows, columnHeading, function,
				DEFAULT_RATIO_MULTIPLIER);
	}

	/**
	 * Construct from a collection of {@link StatsPredicateRow}s that collects
	 * the same value for each row (but with a different predicate) per
	 * iteration and produces an average percentage over all iterations. The
	 * percentage denominator is specified by the {@link StatsPredicateRow}
	 * denominator. Each {@link StatsPredicateRow} acts on a subset of the
	 * scape, as determined by its {@link StatsPredicate}.
	 * 
	 * @param <T>
	 *            type of scape members
	 * @param shortName
	 *            short name
	 * @param name
	 *            name
	 * @param iteratingScape
	 *            scape. Used by {@link CollectorFunctionPerIteration} to get
	 *            the current iteration. If {@code null} then will use
	 *            {@link CollectorFunction} instead.
	 * @param rows
	 *            rows to create in the table
	 * @param columnHeading
	 *            column heading
	 * @param function
	 *            value to collect from simulation.
	 * @param ratioMultiplier
	 *            amount to multiple the ratio by (eg: 100 to get percentage)
	 */
	public <T> StatsRows(String shortName, String name, Scape iteratingScape,
			Collection<StatsPredicateRow<T>> rows, String columnHeading,
			StatsFunction<T> function, double ratioMultiplier) {
		this(shortName, name, columnHeading, ratioMultiplier);

		for (StatsPredicateRow<T> row : rows) {
			stats.add(row.getCollectorFunction(function, iteratingScape));
		}

	}

	/**
	 * Construct from a collection of {@link StatsFunctionRow}s with the default
	 * ratio multiplier.
	 * 
	 * @param shortName
	 *            short name
	 * @param name
	 *            name
	 * @param scape
	 *            scape. Used to get the current iteration.
	 * @param rows
	 *            rows to create in the table
	 * @param columnHeading
	 *            column heading
	 * @param predicate
	 *            predicate to apply to all rows
	 * @param <T>
	 *            type of scape members
	 */
	public <T> StatsRows(String shortName, String name, Scape scape,
			Collection<StatsFunctionRow<T>> rows, String columnHeading,
			StatsPredicate<T> predicate) {
		this(shortName, name, scape, rows, columnHeading, predicate,
				DEFAULT_RATIO_MULTIPLIER);
	}

	/**
	 * Construct from a collection of {@link StatsFunctionRow}s that collects a
	 * different value for each row, but with the same predicate, per iteration
	 * and produces an average ratio over all iterations. The ratio denominator
	 * is specified by the {@link StatsFunctionRow} denominator. Each
	 * {@link StatsFunctionRow} acts on a subset of the scape, as determined by
	 * its {@link StatsPredicate}.
	 * 
	 * @param shortName
	 *            short name
	 * @param name
	 *            name
	 * @param iteratingScape
	 *            scape. Used by {@link CollectorFunctionPerIteration} to get
	 *            the current iteration. If {@code null} then will use
	 *            {@link CollectorFunction} instead.
	 * @param rows
	 *            rows to create in the table
	 * @param columnHeading
	 *            column heading
	 * @param predicate
	 *            predicate to apply to all rows
	 * @param ratioMultiplier
	 *            amount to multiple the ratio by (eg: 100 to get percentage)
	 * @param <T>
	 *            type of scape members
	 */
	public <T> StatsRows(String shortName, String name, Scape iteratingScape,
			Collection<StatsFunctionRow<T>> rows, String columnHeading,
			StatsPredicate<T> predicate, double ratioMultiplier) {
		this(shortName, name, columnHeading, ratioMultiplier);

		for (StatsFunctionRow<T> row : rows) {
			stats.add(row.getCollectorFunction(predicate, iteratingScape));
		}
	}

	private StatsRows(String shortName, String name, String columnHeading,
			double ratioMultiplier) {
		this.columnHeading = columnHeading;
		this.name = name;
		this.shortName = shortName;
		this.ratioMultiplier = ratioMultiplier;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public Collection<? extends StatCollector> getStatCollectors() {
		return stats;
	}

	@Override
	public int getChartViewType() {
		return StatsOutputModel.NO_CHART;
	}

	@Override
	public Collection<String> getChartSeries() {
		return Collections.emptyList();
	}

	/**
	 * Return collector function values as a double array.
	 * 
	 * @param run
	 *            run number
	 * @return collector function values.
	 */
	@Override
	public double[] getValues(int run) {
		double[] values = new double[stats.size()];
		int index = 0;
		for (CollectorFunction<?> cf : stats) {
			values[index++] = getValue(cf);
		}
		return values;
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

		// Fill container with values from the collector functions
		for (CollectorFunction<?> cf : stats) {
			container
					.addSingleRow(new Object[] { cf.getName(), getValue(cf) });
		}

		// Store this run
		valuesFromAllRuns.add(getValues(run));

		return container;
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
						new String[] { columnHeading }, getRowNames(),
						runNumbers(numberRuns), array);

		CBuilder builder =
				new CBuildFromMatrix(name + " (All runs)", allRuns);

		return new CDataCacheContainer(builder);
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
	 * Return array of the collector function names.
	 * 
	 * @return collector function names
	 */
	public String[] getRowNames() {
		String[] rowNames = new String[stats.size()];

		int index = 0;
		for (CollectorFunction<?> cf : stats) {
			rowNames[index++] = cf.getName();
		}
		return rowNames;
	}

	/**
	 * Get the collector function ratio * ratioMultiplier.
	 * 
	 * @param cfunc
	 *            collector function
	 * @return collector function ratio * ratioMultiplier
	 */
	private double getValue(CollectorFunction<?> cfunc) {
		return cfunc.getRatio() * ratioMultiplier;
	}

}