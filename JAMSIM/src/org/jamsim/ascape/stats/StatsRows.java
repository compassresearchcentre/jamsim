package org.jamsim.ascape.stats;

import java.util.Collection;
import java.util.LinkedList;

import org.ascape.model.Scape;
import org.ascape.util.data.StatCollector;
import org.jamsim.ascape.output.AbstractMultiRunOutputDataset;
import org.jamsim.ascape.output.StatCollectorProvider;

/**
 * Collection of {@link CollectorFunction}s created from
 * {@link StatsFunctionRow}s or {@link StatsPredicateRow}s.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class StatsRows extends AbstractMultiRunOutputDataset implements
		StatCollectorProvider {

	/**
	 * Default ratio multiplier used when none is specified by a constructor.
	 */
	public static final int DEFAULT_RATIO_MULTIPLIER = 100;

	private final Collection<CollectorFunction<?>> stats =
			new LinkedList<CollectorFunction<?>>();

	private final double ratioMultiplier;

	private String[] valueNames;

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

		this.valueNames = calcValueNames();
	}

	/**
	 * Construct from a collection of {@link StatsFunctionRow}s with the default
	 * ratio multiplier.
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
	 *            predicate to apply to all rows. If {@code null}, collects data
	 *            for all members.
	 * @param <T>
	 *            type of scape members
	 */
	public <T> StatsRows(String shortName, String name, Scape iteratingScape,
			Collection<StatsFunctionRow<T>> rows, String columnHeading,
			StatsPredicate<T> predicate) {
		this(shortName, name, iteratingScape, rows, columnHeading, predicate,
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
	 *            predicate to apply to all rows. If {@code null}, collects data
	 *            for all members.
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

		this.valueNames = calcValueNames();

	}

	/**
	 * Construct from a single {@link CollectorFunction}.
	 * 
	 * @param shortName
	 *            short name
	 * @param name
	 *            name
	 * @param function
	 *            collector function
	 * @param columnHeading
	 *            column heading
	 * @param ratioMultiplier
	 *            amount to multiple the ratio by (eg: 100 to get percentage)
	 * @param <T>
	 *            type of scape members
	 */
	public <T> StatsRows(String shortName, String name,
			CollectorFunction<T> function, String columnHeading,
			double ratioMultiplier) {
		this(shortName, name, columnHeading, ratioMultiplier);

		stats.add(function);

		this.valueNames = calcValueNames();
	}

	private StatsRows(String shortName, String name, String columnHeading,
			double ratioMultiplier) {
		super(shortName, name, columnHeading);
		this.ratioMultiplier = ratioMultiplier;
	}

	@Override
	public Collection<? extends StatCollector> getStatCollectors() {
		return stats;
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
			values[index++] = fetchValue(cf);
		}
		return values;
	}

	@Override
	public String[] getValueNames() {
		return valueNames;
	}

	/**
	 * Return array of the collector function names.
	 * 
	 * @return collector function names
	 */
	private String[] calcValueNames() {
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
	private double fetchValue(CollectorFunction<?> cfunc) {
		return cfunc.getRatio() * ratioMultiplier;
	}

}