package org.jamsim.ascape.r;

import java.awt.Container;
import java.io.IOException;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.data.model.CDataRuntimeException;
import net.casper.ext.swing.CDatasetTableModel;

import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.runtime.swing.navigator.PanelViewTable;
import org.ascape.view.vis.PanelView;
import org.jamsim.ascape.output.OutputDatasetProvider;
import org.jamsim.ascape.output.Saveable;
import org.jamsim.ascape.output.SaveableDataset;
import org.jamsim.ascape.ui.UIUtil;

/**
 * Fetches the dataset for run 0 from a {@link OutputDatasetProvider} each time
 * {@link #getPanelView()} is called. Provides a {@link PanelView} with the
 * dataset.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class PanelViewDatasetProvider implements PanelViewProvider, Saveable {

	private final OutputDatasetProvider dsprovider;
	private final String nodeName;

	/**
	 * Create a {@link PanelViewDatasetProvider} with default font.
	 * 
	 * @param outDataset
	 *            output dataset provider
	 */
	public PanelViewDatasetProvider(OutputDatasetProvider outDataset) {
		this.nodeName = outDataset.getName();
		this.dsprovider = outDataset;
	}

	private PanelView createPanelView(String name,
			CDataCacheContainer container) {

		try {
			TableModel tmodel = new CDatasetTableModel(container);

			JTable table = UIUtil.createTable(tmodel, name);

			return PanelViewTable.createPanelView(table);

		} catch (IOException e) {
			throw new CDataRuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void saveToCSV(String directory) throws IOException {
		try {
			SaveableDataset
					.saveToCSV(directory, nodeName, getDataset(), true);
		} catch (CDataGridException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	/**
	 * Fetches the dataset from run 0 of the dataset provider.
	 * 
	 * @return casper container
	 */
	private CDataCacheContainer getDataset() throws CDataGridException {
		return dsprovider.getOutputDataset(0);
	}

	@Override
	public String getName() {
		return nodeName;
	}

	@Override
	public PanelView getPanelView() {
		try {
			return createPanelView(nodeName, getDataset());
		} catch (CDataGridException e) {
			throw new CDataRuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void panelViewAdded(Container pvFrameImp) {
		// nothing to do
	}

	@Override
	public void frameClosed() {
		// nothing to do
	}

}
