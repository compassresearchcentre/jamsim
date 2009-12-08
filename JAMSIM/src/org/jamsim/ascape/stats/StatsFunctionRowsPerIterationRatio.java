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
public class StatsFunctionRowsPerIterationRatio implements StatsOutputModel {

	private final Collection<CollectorFunctionPerIteration<?>> stats =
			new LinkedList<CollectorFunctionPerIteration<?>>();

	private final String columnHeading;

	private final String name;

	private final double ratioMultipler;

	/**
	 * Invoke the constructor with a {@code ratioMultipler} of 100.
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
	public <T> StatsFunctionRowsPerIterationRatio(String name, Scape scape,
			Collection<StatsFunctionRow<T>> rows, String columnHeading,
			StatsPredicate<T> predicate) {
		this(name, scape, rows, columnHeading, predicate, 100);
	}

	/**
	 * Default constructor. From a collection of {@link StatsFunctionRow}s,
	 * build collectors that collects a value {@code function} per iteration and
	 * produces an average percentage over all iterations. The percentage
	 * denominator is specified by the {@link StatsFunctionRow} denominator.
	 * Each {@link StatsFunctionRow} acts on a subset of the scape, as
	 * determined by its {@link StatsPredicate}.
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
	 * @param ratioMultipler
	 *            amount to multiple the ratio by (eg: 100 to get percentage)
	 * @param <T>
	 *            type of scape members
	 */
	public <T> StatsFunctionRowsPerIterationRatio(String name, Scape scape,
			Collection<StatsFunctionRow<T>> rows, String columnHeading,
			StatsPredicate<T> predicate, double ratioMultipler) {

		for (StatsFunctionRow<T> row : rows) {
			stats.add(new CollectorFunctionPerIteration<T>(row.getName(), row
					.getFunction(), predicate, row.getDenominator(), scape));
		}

		StatsFunctionRow<T> row = rows.iterator().next();
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
	public CDataCacheContainer getResults() throws CDataGridException {

		// Set up new Casper container for the results
		String columnNames =
				columnHeading + ",Percent (over all fortnights)";
		final Class<?>[] columnTypes =
				new Class[] { String.class, Double.class };

		// order by insertion because getName() is a string and we don't want
		// alphanumeric order for the category number strings
		CDataCacheContainer container =
				CDataCacheContainer.newInsertionOrdered(getName(),
						columnNames, columnTypes);

		for (CollectorFunctionPerIteration<?> sc : stats) {
			container.addSingleRow(new Object[] { sc.getName(),
					sc.getRatio() * ratioMultipler });
		}

		return container;
	}

}