package org.jamsim.ascape.output;

import java.util.Collection;

import org.ascape.view.vis.ChartView;

/**
 * Provides a chart type and series to create a {@link ChartView}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface ChartProvider {
	
	/**
	 * Line graph chart type.
	 */
	int TIME_SERIES = ChartView.TIME_SERIES;

	/**
	 * Histogram chart type.
	 */
	int HISTOGRAM = ChartView.HISTOGRAM;

	/**
	 * Pie chart type.
	 */
	int PIE = ChartView.PIE;

	/**
	 * Get the chart type.
	 * 
	 * @return chart type
	 */
	int getChartViewType();

	/**
	 * Names of the series displayed on the chart.
	 * 
	 * @return collection of series names
	 */
	Collection<String> getChartSeries();
	
	/**
	 * Name of chart.
	 * 
	 * @return name of chart.
	 */
	String getName();

}
