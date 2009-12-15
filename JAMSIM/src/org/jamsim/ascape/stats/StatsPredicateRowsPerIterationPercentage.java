package org.jamsim.ascape.stats;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

import org.ascape.model.Scape;
import org.ascape.util.data.StatCollector;

/**
 * Collection of {@link StatsPredicateRow}s that collects a value per iteration
 * and produces an average percentage over all iterations.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class StatsPredicateRowsPerIterationPercentage implements
		StatsOutputModel {

	private final Collection<CollectorFunctionPerIteration<?>> stats =
			new LinkedList<CollectorFunctionPerIteration<?>>();

	private final String columnHeading;
	private final String name;

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
	public <T> StatsPredicateRowsPerIterationPercentage(Scape iteratingScape,
			Collection<StatsPredicateRow<T>> rows, String columnHeading,
			StatsFunction<T> function) {
		this("Average percent with " + function.getName()
				+ " in each iteration by " + columnHeading + " ("
				+ rows.iterator().next().getDenominatorName() + ")",
				iteratingScape, rows, columnHeading, function);
	}

	/**
	 * Default constructor. From a collection of {@link StatsPredicateRow}s,
	 * build collectors that collects a value {@code function} per iteration and
	 * produces an average percentage over all iterations. The percentage
	 * denominator is specified by the {@link StatsPredicateRow} denominator.
	 * Each {@link StatsPredicateRow} acts on a subset of the scape, as
	 * determined by its {@link StatsPredicate}.
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
	public <T> StatsPredicateRowsPerIterationPercentage(String name,
			Scape iteratingScape, Collection<StatsPredicateRow<T>> rows,
			String columnHeading, StatsFunction<T> function) {

		for (StatsPredicateRow<T> row : rows) {
			stats.add(new CollectorFunctionPerIteration<T>(row.getName(),
					function, row.getPredicate(), row.getDenominator(),
					iteratingScape));
		}

		StatsPredicateRow<T> row = rows.iterator().next();
		this.columnHeading = columnHeading;
		this.name = name;
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
		String columnNames = columnHeading + ",Percent (over all iterations)";
		final Class<?>[] columnTypes =
				new Class[] { String.class, Double.class };

		CDataCacheContainer container =
				CDataCacheContainer.newInsertionOrdered(getName(),
						columnNames, columnTypes);

		for (CollectorFunctionPerIteration<?> sc : stats) {
			container.addSingleRow(new Object[] { sc.getName(),
					sc.getPercent() });
		}

		return container;
	}

}