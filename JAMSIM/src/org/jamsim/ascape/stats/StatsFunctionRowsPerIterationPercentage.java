package org.jamsim.ascape.stats;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

import org.ascape.model.Scape;
import org.ascape.util.data.StatCollector;

/**
 * Collection of {@link StatsFunctionRow}s that collects a value per iteration
 * and produces an average percentage over all iterations.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class StatsFunctionRowsPerIterationPercentage implements
		StatsOutputModel {

	private final Collection<CollectorFunctionPerIteration<?>> stats =
			new LinkedList<CollectorFunctionPerIteration<?>>();

	private final String rowColumnHeading;

	private final String name;

	/**
	 * Default constructor. From a collection of {@link StatsFunctionRow}s,
	 * build collectors that collects a value {@code function} per iteration and
	 * produces an average percentage over all iterations. The percentage
	 * denominator is specified by the {@link StatsFunctionRow} denominator.
	 * Each {@link StatsFunctionRow} acts on a subset of the scape, as
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
	public <T> StatsFunctionRowsPerIterationPercentage(String name, Scape scape,
			Collection<StatsFunctionRow<T>> rows, String rowHeading,
			StatsPredicate<T> predicate) {

		for (StatsFunctionRow<T> row : rows) {
			stats.add(new CollectorFunctionPerIteration<T>(row.getName(), row
					.getFunction(), predicate, row.getDenominatorValue(),
					scape));
		}

		StatsFunctionRow<T> row = rows.iterator().next();
		rowColumnHeading = rowHeading;
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
	public CDataCacheContainer getResults() throws CDataGridException {

		// Set up new Casper container for the results
		String columnNames =
				rowColumnHeading + ",Percent (over all fortnights)";
		final Class<?>[] columnTypes =
				new Class[] { String.class, Double.class };

		// order by insertion because getName() is a string and we don't want
		// alphanumeric order for the category number strings
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