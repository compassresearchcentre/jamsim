package org.jamsim.ascape.navigator;

import java.io.IOException;
import java.util.Collection;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.ext.swing.CDatasetTableModel;

import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.runtime.swing.navigator.PanelViewTable;
import org.ascape.util.data.StatCollector;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.output.ChartProvider;
import org.jamsim.ascape.output.MeansOnMultiRunDataset;
import org.jamsim.ascape.output.MultiRunOutputDatasetProvider;
import org.jamsim.ascape.output.OutputDatasetProvider;
import org.jamsim.ascape.output.OutputException;
import org.jamsim.ascape.output.SaveableDataset;
import org.jamsim.ascape.output.StatCollectorProvider;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.ascape.ui.UIUtil;

/**
 * Connects an {@link OutputDatasetProvider} to a scape by adding any
 * {@link StatCollector}s and/or charts to the scape and providing the dataset
 * results as a output node for the Navigator. Dataset results can be results
 * provided at the end of each iteration, and/or if the
 * {@link OutputDatasetProvider} is a {@link MultiRunOutputDatasetProvider} then
 * at the end of the simulation. Optionally outputs the dataset results to a
 * file.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */

public class OutputDatasetNodeProvider implements OutputNodeProvider {

	private final OutputDatasetProvider outDataset;
	private final MicroSimScape<?> msscape;

	/**
	 * Connection {@code outDataset} to the scape. If the
	 * {@link OutputDatasetProvider} is a {@link MultiRunOutputDatasetProvider}
	 * it will be wrapped in a {@link MeansOnMultiRunDataset}.
	 * 
	 * @param scape
	 *            microsim scape
	 * @param outDataset
	 *            output dataset provider
	 */
	public OutputDatasetNodeProvider(MicroSimScape<?> scape,
			OutputDatasetProvider outDataset) {
		this.msscape = scape;

		if (outDataset instanceof StatCollectorProvider) {
			addStatCollectors((StatCollectorProvider) outDataset);
		}

		if (outDataset instanceof ChartProvider) {
			msscape.addChart((ChartProvider) outDataset);
		}

		this.outDataset = wrapMeans(msscape.getScapeRInterface(), outDataset);

	}

	private OutputDatasetProvider wrapMeans(ScapeRInterface scapeR,
			OutputDatasetProvider provider) {

		// wrap in MeansOnMultiRunDataset if MultiRunOutputDatasetProvider
		if (provider instanceof MultiRunOutputDatasetProvider) {
			return new MeansOnMultiRunDataset(scapeR,
					(MultiRunOutputDatasetProvider) provider);
		} else {
			return provider;
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
			msscape.addStatCollector(sc);
		}
	}

	@Override
	public DefaultMutableTreeNode getEndOfSimOutputNode()
			throws OutputException {

		if (!(outDataset instanceof MultiRunOutputDatasetProvider)) {
			return null;
		}

		try {
			CDataCacheContainer allRuns =
					((MultiRunOutputDatasetProvider) outDataset)
							.getMultiRunDataset();

			// save to file
			if (msscape.isResultsToFile()) {
				writeCSV(allRuns);
			}

			// create and return node
			return createNode(allRuns.getCacheName(), allRuns);

		} catch (IOException e) {
			throw new OutputException(e);
		} catch (CDataGridException e) {
			throw new OutputException(e);
		}

	}

	@Override
	public String getName() {
		return outDataset.getName();
	}

	@Override
	public DefaultMutableTreeNode getOutputNode(int runNumber)
			throws OutputException {

		try {
			String runName =
					outDataset.getName() + " (Run " + runNumber + ")";
			CDataCacheContainer results =
					outDataset.getOutputDataset(runNumber);

			if (results == null) {
				// silently ignore null results
				return null;
			}

			if (msscape.isResultsToFile()) {
				writeCSV(runName, results);
			}

			return createNode(runName, results);

		} catch (IOException e) {
			throw new OutputException(e);
		} catch (CDataGridException e) {
			throw new OutputException(e);
		}
	}

	/**
	 * Create a Navigator tree node that represents the dataset.
	 * 
	 * @param nodeName
	 *            name of node
	 * @param container
	 *            dataset results
	 * @throws IOException
	 *             if problem creating {@link CDatasetTableModel}
	 */
	private DefaultMutableTreeNode createNode(String nodeName,
			CDataCacheContainer container) throws IOException {

		TableModel tmodel = new CDatasetTableModel(container);
		TableCellRenderer dblRenderer = UIUtil.getDoubleCellRenderer();
		JTable table = new JTable(tmodel);
		table.setName(nodeName);
		table.setDefaultRenderer(Double.class, dblRenderer);

		PanelViewProvider provider = new PanelViewTable(table);

		SaveableDataset saver = createSaveableDataset(nodeName, container);

		SaveablePanelViewNode saveablePVNode =
				new SaveablePanelViewNode(provider, saver);

		return saveablePVNode;

	}

	private void writeCSV(CDataCacheContainer container) throws IOException {
		writeCSV(container.getCacheName(), container);
	}

	private void writeCSV(String fileName, CDataCacheContainer container)
			throws IOException {

		createSaveableDataset(fileName, container).saveToCSV(msscape
				.getOutputDirectory());

	}

	private SaveableDataset createSaveableDataset(String fileName,
			CDataCacheContainer container) {
		String name = SaveableDataset.cleanDatedCSVName(fileName);
		return new SaveableDataset(name, container, false);
	}

}
