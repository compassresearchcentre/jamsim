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
public class StatsPredicateRowsPerIterationPercentage implements StatsOutputModel {

	private final Collection<CollectorFunctionPerIteration<?>> stats =
			new LinkedList<CollectorFunctionPerIteration<?>>();

	private final String rowColumnHeading;
	private final String functionName;
	private final String denominatorName;

	/**
	 * Default constructor. From a collection of {@link StatsPredicateRow}s,
	 * build collectors that collects a value {@code function} per iteration and
	 * produces an average percentage over all iterations. The percentage
	 * denominator is specified by the {@link StatsPredicateRow} denominator.
	 * Each {@link StatsPredicateRow} acts on a subset of the scape, as
	 * determined by its {@link StatsPredicate}.
	 * 
	 * @param scape
	 *            scape. Used to get the current iteration.
	 * @param rows
	 *            rows to create in the table
	 * @param function
	 *            value to collect from simulation.
	 * @param <T>
	 *            type of scape members
	 */
	public <T> StatsPredicateRowsPerIterationPercentage(Scape scape,
			Collection<StatsPredicateRow<T>> rows, String rowHeading,
			StatsFunction<T> function) {

		for (StatsPredicateRow<T> row : rows) {
			stats.add(new CollectorFunctionPerIteration<T>(row.getName(),
					function, row.getPredicate(), row.getDenominatorValue(),
					scape));
		}

		StatsPredicateRow<T> row = rows.iterator().next();
		functionName = function.getName();
		rowColumnHeading = rowHeading;
		denominatorName = row.getDenominatorName();
	}

	@Override
	public final String getName() {
		return "Average percent with " + functionName
				+ " in each iteration by " + rowColumnHeading + " ("
				+ denominatorName + ")";
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
	public CDataCacheContainer getResults() throws CDataGridException {

		// Set up new Casper container for the results
		String columnNames =
				rowColumnHeading + ",Percent (over all iterations)";
		final Class<?>[] columnTypes =
				new Class[] { String.class, Double.class };

		CDataCacheContainer container =
				CDataCacheContainer.newInsertionOrdered(getName(), columnNames, columnTypes);

		for (CollectorFunctionPerIteration<?> sc : stats) {
			container.addSingleRow(new Object[] { sc.getName(),
					sc.getPercent() });
		}

		return container;
	}

}