package org.jamsim.ascape.output;

import java.util.Collection;
import java.util.LinkedList;

import org.ascape.util.data.StatCollector;
import org.jamsim.ascape.stats.StatsRows;

/**
 * A {@link ChartProvider} derived from a {@link StatsRows} object.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class StatsRowsChartProvider implements ChartProvider {

	private final int chartType;
	private final String aggregation;
	private final StatsRows stats;

	/**
	 * Construct a {@link ChartProvider} from a {@link StatsRows}.
	 * 
	 * @param chartType
	 *            chart type, eg {@link ChartProvider#HISTOGRAM},
	 *            {@link ChartProvider#PIE}, {@link ChartProvider#TIME_SERIES}.
	 * @param aggregation
	 *            series type, eg: "Count", "Min", "Max", "Var", "StD", "Sum",
	 *            "Avg"
	 * @param stats
	 *            stats rows that provide the stat collectors for the chart
	 */
	public StatsRowsChartProvider(int chartType, String aggregation,
			StatsRows stats) {
		this.chartType = chartType;
		this.aggregation = aggregation;
		this.stats = stats;
	}

	@Override
	public Collection<String> getChartSeries() {
		Collection<String> series = new LinkedList<String>();

		// add the sum value from all the StatCollectors to the chart
		for (StatCollector sc : stats.getStatCollectors()) {
			series.add(aggregation + " " + sc.getName());
		}

		return series;
	}

	@Override
	public int getChartViewType() {
		return chartType;
	}

}
