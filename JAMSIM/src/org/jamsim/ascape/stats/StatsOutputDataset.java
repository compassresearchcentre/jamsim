package org.jamsim.ascape.stats;

import java.util.Collection;
import java.util.TooManyListenersException;

import org.ascape.model.event.ScapeEvent;
import org.ascape.runtime.swing.navigator.PanelViewNodes;
import org.ascape.util.data.StatCollector;
import org.ascape.view.vis.ChartView;

/**
 * A {@link OutputDataset} that adds charts and stats collectors to the scape
 * when added.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class StatsOutputDataset extends OutputDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3095815955607094558L;
	
	private final StatsOutputModel stats;

	/**
	 * Construct an {@link StatsOutputDataset}.
	 * 
	 * @param outputTablesNode
	 *            navigator output tables tree node
	 * @param stats
	 *            {@link StatsOutputModel}
	 * @param outputDirectory
	 *            destination directory for results output file
	 */
	public StatsOutputDataset(PanelViewNodes outputTablesNode,
			StatsOutputModel stats, String outputDirectory) {
		super(outputTablesNode, stats, outputDirectory);
		this.stats = stats;
	}

	/**
	 * Add the view to the scape, registering it as a listener, and ensuring
	 * that it hasn't been added to any other scapes.
	 * 
	 * @param scapeEvent
	 *            the event for this scape to make this view the observer of
	 * @throws TooManyListenersException
	 *             the too many listeners exception
	 * @exception TooManyListenersException
	 *                on attempt to add a scape when one is allready added
	 */
	@Override
	public void scapeAdded(ScapeEvent scapeEvent)
			throws TooManyListenersException {
		super.scapeAdded(scapeEvent);
		initializeListener();
	}

	/**
	 * Called once at the beginning after the listener has been added to the
	 * scape. At this point the scape instance variable will have been set.
	 */
	public void initializeListener() {
		addStatCollectors(stats.getStatCollectors());

		int chartType = stats.getChartViewType();

		if (chartType != StatsOutputModel.NO_CHART) {
			ChartView chart = new ChartView(chartType);

			scape.addView(chart);

			// setup the chart AFTER adding it to the scape
			for (String seriesName : stats.getChartSeries()) {
				chart.addSeries(seriesName);
			}
		}
	}

	/**
	 * Add {@link StatCollector}s on the scape to record data during the
	 * simulation.
	 * 
	 * @param stats
	 *            collection of {@link StatCollector}s to add to the scape.
	 */
	private void addStatCollectors(Collection<? extends StatCollector> stats) {
		for (StatCollector sc : stats) {
			scape.addStatCollector(sc);
		}
	}

}