package org.jamsim.ascape.r;

import java.awt.Container;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.ext.swing.CDatasetTableModel;

import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.util.swing.AscapeGUIUtil;
import org.ascape.util.swing.PanelViewUtil;
import org.ascape.view.vis.PanelView;
import org.jamsim.ascape.output.OutputDatasetProvider;
import org.jamsim.ascape.output.ROutput;
import org.jamsim.ascape.ui.UIUtil;

/**
 * Fetches the dataset from a {@link OutputDatasetProvider} each time
 * {@link #getPanelView()} is called. Provides a {@link PanelView} with the
 * dataset.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class PanelViewDataset implements PanelViewProvider {

	private final OutputDatasetProvider dsprovider;
	private final String nodeName;

	/**
	 * Create a {@link PanelViewDataset} with default font.
	 * 
	 * @param outDataset
	 *            output dataset provider
	 */
	public PanelViewDataset(OutputDatasetProvider outDataset) {
		this.nodeName = outDataset.getName();
		this.dsprovider = outDataset;
	}

	/**
	 * Fetches the dataset from a {@link ROutput} and display a
	 * {@link PanelView} with the table output.
	 * 
	 * @param rcmd
	 *            R command to execute
	 * @return panel view
	 */
	private PanelView createPanelView(String name,
			OutputDatasetProvider outDataset) {

		try {
			CDataCacheContainer container = outDataset.getOutputDataset(0);
			TableModel tmodel = new CDatasetTableModel(container, true);

			TableCellRenderer dblRenderer = UIUtil.getDoubleCellRenderer();
			JTable table = new JTable(tmodel);
			table.setName(name);
			table.setDefaultRenderer(Double.class, dblRenderer);

			Dimension desktopSize = AscapeGUIUtil.getDesktopSize();
			return PanelViewUtil.createPanelView(table, desktopSize);

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CDataGridException e) {
			throw new RuntimeException(e);
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
