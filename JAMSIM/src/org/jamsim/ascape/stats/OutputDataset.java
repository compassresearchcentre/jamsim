package org.jamsim.ascape.stats;

import java.io.IOException;

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
import org.jamsim.date.DateUtil;
import org.jamsim.io.FileUtil;
import org.jamsim.swing.DoubleCellRenderer;

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

	private final PanelViewNodes outputTablesNode;

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
	public OutputDataset(PanelViewNodes outputTablesNode,
			OutputDatasetProvider outDataset, String outputDirectory) {
		super(outDataset.getName());
		this.outDataset = outDataset;
		this.outputTablesNode = outputTablesNode;
		this.outputDirectory = FileUtil.addTrailingSlash(outputDirectory);
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

			createNavigatorOutputNode(runName, results);

			CasperUtil.writeToCSV(outputDirectory
					+ DateUtil.nowToSortableUniqueDateString() + " "
					+ runName + ".csv", results);

			runNumber++;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CDataGridException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void scapeClosing(ScapeEvent scapeEvent) {

		
		if (outDataset instanceof MultiRunOutputDatasetProvider
				&& !multiRunNodeCreated) {
			
			// create multi-run node
			try {
				CDataCacheContainer allRuns =
						((MultiRunOutputDatasetProvider) outDataset)
								.getMultiRunDataset();

				createNavigatorOutputNode(allRuns.getCacheName(), allRuns);

				multiRunNodeCreated = true;

			} catch (CDataGridException e) {
				throw new RuntimeException(e);
			}
		}

	}

	/**
	 * Create a node on the Navigator tree that represents this dataset.
	 * 
	 */
	private void createNavigatorOutputNode(String nodeName,
			CDataCacheContainer container) {
		TableModel tmodel;
		try {
			tmodel = new CDatasetTableModel(container, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		createNavigatorOutputNode(nodeName, tmodel);
	}

	/**
	 * Create a node on the Navigator tree that represents this table model.
	 * 
	 */
	private void createNavigatorOutputNode(String nodeName, TableModel tmodel) {

		TableCellRenderer dblRenderer = new DoubleCellRenderer();
		JTable table = new JTable(tmodel);
		table.setName(nodeName);
		table.setDefaultRenderer(Double.class, dblRenderer);

		outputTablesNode.addChildTableNode(table);

	}

}