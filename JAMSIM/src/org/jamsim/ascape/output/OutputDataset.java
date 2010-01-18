package org.jamsim.ascape.output;

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
import org.ascape.runtime.swing.navigator.RunResultsNode;
import org.ascape.util.data.StatCollector;
import org.ascape.view.vis.ChartView;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.date.DateUtil;
import org.jamsim.io.FileUtil;
import org.jamsim.r.RDataFrame;
import org.jamsim.r.RInterfaceException;
import org.jamsim.r.RInterfaceHL;
import org.jamsim.r.UnsupportedTypeException;
import org.jamsim.swing.DoubleCellRenderer;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

/**
 * Display output from a {@link OutputDatasetProvider}. When the scape stops
 * after each run, adds an output data table node in the Navigator that displays
 * the {@link OutputDatasetProvider} results in a table, and outputs the results
 * to a file.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class OutputDataset extends DefaultScapeListener {
	private static final long serialVersionUID = -5105471052036807288L;

	private final RunResultsNode outputTablesNode;

	private final OutputDatasetProvider outDataset;

	private final String outputDirectory;

	private int runNumber = 1;

	private boolean multiRunNodeCreated = false;

	/**
	 * Master constructor.
	 * 
	 * @param outputTablesNode
	 *            navigator output tables tree node
	 * @param outDataset
	 *            {@link OutputDatasetProvider}
	 * @param outputDirectory
	 *            destination directory for results output file
	 */
	public OutputDataset(RunResultsNode outputTablesNode,
			OutputDatasetProvider outDataset, String outputDirectory) {
		super(outDataset.getName());
		this.outDataset = outDataset;
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
	 *                on attempt to add a scape when one is already added
	 */
	@Override
	public void scapeAdded(ScapeEvent scapeEvent)
			throws TooManyListenersException {
		super.scapeAdded(scapeEvent);

		if (outDataset instanceof StatCollectorProvider) {
			addStatCollectors((StatCollectorProvider) outDataset);
		}

		if (outDataset instanceof ChartProvider) {
			addChart((ChartProvider) outDataset);
		}


	}

	/**
	 * Add a chart to the scape.
	 * 
	 * @param source
	 *            a chart provider
	 */
	private void addChart(ChartProvider source) {
		int chartType = source.getChartViewType();

		ChartView chart = new ChartView(chartType);

		scape.addView(chart);

		// setup the chart AFTER adding it to the scape
		for (String seriesName : source.getChartSeries()) {
			chart.addSeries(seriesName);
		}
	}

	/**
	 * Add {@link StatCollector}s on the scape to record data during the
	 * simulation.
	 * 
	 * @param stats
	 *            collection of {@link StatCollector}s to add to the scape.
	 */
	private void addStatCollectors(StatCollectorProvider statsProvider) {

		Collection<? extends StatCollector> stats =
				statsProvider.getStatCollectors();

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
			String runName = name + " (Run " + runNumber + ")";
			CDataCacheContainer results =
					outDataset.getOutputDataset(runNumber);

			createNavigatorOutputNode(runNumber, runName, results);

			writeCSV(runName, results);

			runNumber++;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CDataGridException e) {
			throw new RuntimeException(e);
		}

	}

	private void writeCSV(CDataCacheContainer container) throws IOException {
		writeCSV(container.getCacheName(), container);
	}

	private void writeCSV(String runName, CDataCacheContainer container)
			throws IOException {
		CasperUtil.writeToCSV(outputDirectory
				+ DateUtil.nowToSortableUniqueDateString() + " " + runName
				+ ".csv", container);
	}

	/**
	 * Create the multi-run dataset node when the simulation has finished, if
	 * the {@link OutputDatasetProvider} is a
	 * {@link MultiRunOutputDatasetProvider}.
	 */
	@Override
	public void scapeClosing(ScapeEvent scapeEvent) {

		if (outDataset instanceof MultiRunOutputDatasetProvider
				&& !multiRunNodeCreated) {

			// create multi-run node
			try {
				CDataCacheContainer allRuns =
						((MultiRunOutputDatasetProvider) outDataset)
								.getMultiRunDataset();

				// createNavigatorOutputNode(allRuns.getCacheName(), allRuns);

				multiRunNodeCreated = true;

				RInterfaceHL rInterface =
						((MicroSimScape<?>) scape).getRInterface();

				try {
					String dfName = outDataset.getShortName();
					rInterface.assignDataFrame(dfName, allRuns);
					rInterface.printlnToConsole("Created dataframe " + dfName
							+ "(" + outDataset.getName() + ")");

					REXP rexp =
							rInterface.parseAndEval("meanOfRuns(" + dfName
									+ ")");
					RDataFrame df =
							new RDataFrame(allRuns.getCacheName(), rexp);

					CDataCacheContainer meanOfRuns =
							new CDataCacheContainer(df);

					createNavigatorOutputNode(RunResultsNode.ALLRUNS,
							meanOfRuns.getCacheName(), meanOfRuns);

					writeCSV(meanOfRuns);

				} catch (RInterfaceException e) {
					throw new RuntimeException(e);
				} catch (REXPMismatchException e) {
					throw new RuntimeException(e);
				} catch (UnsupportedTypeException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			} catch (CDataGridException e) {
				throw new RuntimeException(e);
			}
		}

	}

	/**
	 * Create a node on the Navigator tree that represents this dataset.
	 * 
	 */
	private void createNavigatorOutputNode(int runNumber, String nodeName,
			CDataCacheContainer container) {
		TableModel tmodel;
		try {
			tmodel = new CDatasetTableModel(container, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		createNavigatorOutputNode(runNumber, nodeName, tmodel);
	}

	/**
	 * Create a node on the Navigator tree that represents this table model.
	 * 
	 */
	private void createNavigatorOutputNode(int runNumber, String nodeName,
			TableModel tmodel) {

		TableCellRenderer dblRenderer = new DoubleCellRenderer();
		JTable table = new JTable(tmodel);
		table.setName(nodeName);
		table.setDefaultRenderer(Double.class, dblRenderer);

		outputTablesNode.addChildTableNode(runNumber, table);

	}

}