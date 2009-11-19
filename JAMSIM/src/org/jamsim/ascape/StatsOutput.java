package org.jamsim.ascape;

import java.io.IOException;
import java.util.Collection;
import java.util.TooManyListenersException;

import javax.swing.table.TableModel;

import net.casper.ext.swing.CDatasetTableModel;

import org.ascape.model.event.DefaultScapeListener;
import org.ascape.model.event.ScapeEvent;
import org.ascape.runtime.swing.navigator.PanelViewNodes;
import org.ascape.util.data.StatCollector;
import org.ascape.view.vis.ChartView;

/**
 * Display output from a {@link StatsOutputModel}. Adds stat collectors and
 * chart from a {@link StatsOutputModel} to the scape. When the scape stops
 * after each run, adds an output data table node in the Navigator and displays
 * the {@link StatsOutputModel} results in a table.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class StatsOutput extends DefaultScapeListener {
	private static final long serialVersionUID = -5105471052036807288L;

	private final PanelViewNodes outputTablesNode;

	private final StatsOutputModel stats;

	private int runNumber = 1;

	/**
	 * Construct an {@link StatsOutput}.
	 * 
	 * @param outputTablesNode
	 *            navigator output tables tree node
	 * @param stats
	 *            {@link StatsOutputModel}
	 */
	public StatsOutput(PanelViewNodes outputTablesNode,
			StatsOutputModel stats) {
		super(stats.getName());
		this.stats = stats;
		this.outputTablesNode = outputTablesNode;
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

	/**
	 * Perform operations required when the simulation has stopped. Here we
	 * write create an output node on the Navigator tree via a call to
	 * {@link #createNavigatorOutputNode()}.
	 * 
	 * @param scapeEvent
	 *            scape event
	 */
	@Override
	public void scapeStopped(ScapeEvent scapeEvent) {
		// writeStatsToFile(directory + "output.csv", stats);

		// create the output node on the AWT event thread
		/*
		 * Runnable doWorkRunnable = new Runnable() { public void run() {
		 * createOutputNode(); } }; SwingUtilities.invokeLater(doWorkRunnable);
		 */
		createNavigatorOutputNode();

	}

	/**
	 * Create a node on the Navigator tree that represents this output table.
	 * 
	 */
	private void createNavigatorOutputNode() {
		TableModel tmodel;
		try {
			tmodel = new CDatasetTableModel(stats.getResults());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String nodeName = name + " (Run " + runNumber++ + ")";
		outputTablesNode.addChildTableNode(nodeName, tmodel);
	}

}