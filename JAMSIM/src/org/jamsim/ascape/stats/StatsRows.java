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

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * Collection of {@link CollectorFunction}s created from
 * {@link StatsFunctionRow}s or {@link StatsPredicateRow}s.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class StatsRows implements StatsOutputModel,
		MultiRunOutputDatasetProvider {

	private final Collection<CollectorFunction<?>> stats =
			new LinkedList<CollectorFunction<?>>();

	private final String columnHeading;
	private final String name;

	private final double ratioMultipler;

	List<double[]> valuesFromAllRuns = new LinkedList<double[]>();

	private DoubleMatrix2D multiRun;

	/**
	 * Convenience constructor that generates a name.
	 * 
	 * @param <T>
	 *            type of scape members
	 * @param iteratingScape
	 *            scape. Used to get the current iteration.
	 * @param rows
	 *            rows to create in the table
	 * @param columnHeading
	 *            column heading
	 * @param function
	 *            value to collect from simulation.
	 */
	public <T> StatsRows(Scape iteratingScape,
			Collection<StatsPredicateRow<T>> rows, String columnHeading,
			StatsFunction<T> function) {
		this("Average percent with " + function.getName()
				+ " in each iteration by " + columnHeading + " ("
				+ rows.iterator().next().getDenominatorName() + ")",
				iteratingScape, rows, columnHeading, function);
	}

	/**
	 * Invoke the constructor with a {@code ratioMultipler} of 100.
	 * 
	 * @param <T>
	 *            type of scape members
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
	public <T> StatsRows(String name, Scape iteratingScape,
			Collection<StatsPredicateRow<T>> rows, String columnHeading,
			StatsFunction<T> function) {
		this(name, iteratingScape, rows, columnHeading, function, 100);
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
	 */
	public <T> StatsRows(String name, Scape iteratingScape,
			Collection<StatsPredicateRow<T>> rows, String columnHeading,
			StatsFunction<T> function, double ratioMultipler) {

		if (iteratingScape == null) {
			for (StatsPredicateRow<T> row : rows) {
				stats.add(new CollectorFunction<T>(row.getName(), function,
						row.getPredicate(), row.getDenominator()));
			}
		} else {
			for (StatsPredicateRow<T> row : rows) {
				stats.add(new CollectorFunctionPerIteration<T>(row.getName(),
						function, row.getPredicate(), row.getDenominator(),
						iteratingScape));
			}
		}

		this.columnHeading = columnHeading;
		this.name = name;
		this.ratioMultipler = ratioMultipler;
	}

	/**
	 * Construct from a collection of {@link StatsFunctionRow}s with a {@code
	 * ratioMultipler} of 100.
	 * 
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
	public <T> StatsRows(String name, Scape scape,
			Collection<StatsFunctionRow<T>> rows, String columnHeading,
			StatsPredicate<T> predicate) {
		this(name, scape, rows, columnHeading, predicate, 100);
	}

	/**
	 * Construct from a collection of {@link StatsFunctionRow}s that collects a
	 * different value for each row, but with the same predicate, per iteration
	 * and produces an average ratio over all iterations. The ratio denominator
	 * is specified by the {@link StatsFunctionRow} denominator. Each
	 * {@link StatsFunctionRow} acts on a subset of the scape, as determined by
	 * its {@link StatsPredicate}.
	 * 
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
	 * @param ratioMultipler
	 *            amount to multiple the ratio by (eg: 100 to get percentage)
	 * @param <T>
	 *            type of scape members
	 */
	public <T> StatsRows(String name, Scape iteratingScape,
			Collection<StatsFunctionRow<T>> rows, String columnHeading,
			StatsPredicate<T> predicate, double ratioMultipler) {

		if (iteratingScape == null) {
			for (StatsFunctionRow<T> row : rows) {
				stats.add(new CollectorFunction<T>(row.getName(), row
						.getFunction(), predicate, row.getDenominator()));
			}
		} else {
			for (StatsFunctionRow<T> row : rows) {
				stats.add(new CollectorFunctionPerIteration<T>(row.getName(),
						row.getFunction(), predicate, row.getDenominator(),
						iteratingScape));
			}
		}

		this.columnHeading = columnHeading;
		this.name = name;
		this.ratioMultipler = ratioMultipler;
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

		for (CollectorFunction<?> cf : stats) {
			container
					.addSingleRow(new Object[] { cf.getName(), getValue(cf) });
		}

		valuesFromAllRuns.add(getValues(run));

		return container;
	}

	public CDataCacheContainer getMultiRunDataset() throws CDataGridException {
		double[][] array =
				valuesFromAllRuns
						.toArray(new double[valuesFromAllRuns.size()][]);

		String[][] rowNames = new String[stats.size()][1];
		int i = 0;
		for (CollectorFunction<?> cf : stats) {
			rowNames[i++][0] = cf.getName();
		}

		IndexedDenseDoubleMatrix2D allRuns =
				new IndexedDenseDoubleMatrix2D(
						new String[] { columnHeading }, rowNames,
						new String[] { "Run 1", "Run 2" }, ArrayUtil
								.transpose(array));

		CBuilder builder =
				new CBuildFromMatrix(name + " (All runs)", allRuns);

		return new CDataCacheContainer(builder);
	}

	@Override
	public double[] getValues(int run) {
		double[] values = new double[stats.size()];
		int index = 0;
		for (CollectorFunction<?> cf : stats) {
			values[index++] = getValue(cf);
		}
		return values;
	}

	private double getValue(CollectorFunction<?> cfunc) {
		return cfunc.getRatio() * ratioMultipler;
	}

}