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
import org.jamsim.ascape.ui.UIUtil;

/**
 * Fetches the dataset for run 0 from a {@link OutputDatasetProvider} each time
 * {@link #getPanelView()} is called. Provides a {@link PanelView} with the
 * dataset.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class PanelViewDatasetProvider implements PanelViewProvider {

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

	/**
	 * Fetches the dataset from run 0 of a {@link OutputDatasetProvider} and
	 * display a {@link PanelView} with the table output.
	 * 
	 * @param rcmd
	 *            R command to execute
	 * @return panel view
	 */
	private PanelView createPanelView(String name,
			OutputDatasetProvider outDataset) {

		try {
			CDataCacheContainer container = outDataset.getOutputDataset(0);
			TableModel tmodel = new CDatasetTableModel(container);

			JTable table = UIUtil.createTable(tmodel, name);

			return PanelViewTable.createPanelView(table);

		} catch (IOException e) {
			throw new CDataRuntimeException(e);
		} catch (CDataGridException e) {
			throw new CDataRuntimeException(e);
		}

	}

	@Override
	public String getName() {
		return nodeName;
	}

	@Override
	public PanelView getPanelView() {
		return createPanelView(nodeName, dsprovider);
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
