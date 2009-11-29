package org.jamsim.ascape;

import java.util.Collection;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

import org.ascape.util.data.StatCollector;
import org.ascape.view.vis.ChartView;

/**
 * Defines output from a group of {@link StatCollector}s. Output includes a
 * collection of {@link StatCollector}s, a chart, and a results dataset.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 */
public interface StatsOutputModel {

	/**
	 * No chart displayed by the {@link StatsOutputModel}.
	 */
	int NO_CHART = -1;

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
	 * Get name of this group of stat collectors.
	 * 
	 * @return name
	 */
	String getName();

	/**
	 * Get this group of stat collectors.
	 * 
	 * @return stat collectors
	 */
	Collection<? extends StatCollector> getStatCollectors();

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
	 * Create a Casper dataset with the output results of each illness category
	 * (from the StatCollectors), its frequency and percent.
	 * 
	 * @return a casper dataset of illness occurrence data
	 * @throws CDataGridException if problem creating dataset
	 */
	CDataCacheContainer getResults() throws CDataGridException;

}