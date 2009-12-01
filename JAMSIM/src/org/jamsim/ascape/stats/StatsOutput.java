package org.jamsim.ascape.stats;

import java.io.IOException;
import java.util.Collection;
import java.util.TooManyListenersException;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.ext.CasperUtil;
import net.casper.ext.swing.CDatasetTableModel;

import org.ascape.model.event.DefaultScapeListener;
import org.ascape.model.event.ScapeEvent;
import org.ascape.runtime.swing.navigator.PanelViewNodes;
import org.ascape.util.data.StatCollector;
import org.ascape.view.vis.ChartView;
import org.jamsim.date.DateUtil;
import org.jamsim.io.FileUtil;
import org.jamsim.swing.DoubleCellRenderer;

/**
 * Display output from a {@link StatsOutputModel}. Adds stat collectors and
 * chart from a {@link StatsOutputModel} to the scape. When the scape stops
 * after each run, adds an output data table node in the Navigator that displays
 * the {@link StatsOutputModel} results in a table, and outputs the results to a
 * file.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class StatsOutput extends DefaultScapeListener {
	private static final long serialVersionUID = -5105471052036807288L;

	private final PanelViewNodes outputTablesNode;

	private final StatsOutputModel stats;

	private final String outputDirectory;

	private int runNumber = 1;

	/**
	 * Construct an {@link StatsOutput}.
	 * 
	 * @param outputTablesNode
	 *            navigator output tables tree node
	 * @param stats
	 *            {@link StatsOutputModel}
	 * @param outputDirectory
	 *            destination directory for results output file
	 */
	public StatsOutput(PanelViewNodes outputTablesNode,
			StatsOutputModel stats, String outputDirectory) {
		super(stats.getName());
		this.stats = stats;
		this.outputTablesNode = outputTablesNode;
		this.outputDirectory = FileUtil.addTrailingSlash(outputDirectory);
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
	 * {@link #createNavigatorOutputNode()} and output the results to a file.
	 * 
	 * @param scapeEvent
	 *            not used
	 */
	@Override
	public void scapeStopped(ScapeEvent scapeEvent) {

		// create the output node on the AWT event thread
		/*
		 * Runnable doWorkRunnable = new Runnable() { public void run() {
		 * createOutputNode(); } }; SwingUtilities.invokeLater(doWorkRunnable);
		 */

		try {
			String runName = name + " (Run " + runNumber++ + ")";
			CDataCacheContainer results = stats.getResults();

			createNavigatorOutputNode(runName, results);
			
			CasperUtil.writeToCSV(outputDirectory
					+ DateUtil.nowToSortableUniqueDateString() + " "
					+ runName + ".csv", results);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CDataGridException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Create a node on the Navigator tree that represents this output table.
	 * 
	 */
	private void createNavigatorOutputNode(String nodeName,
			CDataCacheContainer container) {
		TableModel tmodel;
		try {
			tmodel = new CDatasetTableModel(container, false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		TableCellRenderer dblRenderer = new DoubleCellRenderer();
		JTable table = new JTable(tmodel); 
		table.setName(nodeName);
		table.setDefaultRenderer(Double.class, dblRenderer);
		
		outputTablesNode.addChildTableNode(table);
		
		
	}

}