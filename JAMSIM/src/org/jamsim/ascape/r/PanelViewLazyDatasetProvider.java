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
import org.jamsim.ascape.output.REXPDatasetProvider;
import org.jamsim.ascape.output.Saveable;
import org.jamsim.ascape.output.SaveableDataset;
import org.jamsim.ascape.ui.UIUtil;
import org.omancode.r.RFaceException;
import org.rosuda.REngine.REXP;


/**
 * Fetches an REXP by evaluating a given R command, uses it to produce
 * an REXPDatasetProvider and uses that to create a table to display in a 
 * {@link PanelView} when {@link #getPanelView()} is first called. Provides
 * that PanelView each time getPanelView() is called.
 * 
 * @author Bede Maclaurin
 * @version $Revision$
 */
public class PanelViewLazyDatasetProvider implements PanelViewProvider, Saveable {

	private ScapeRInterface rInterface;
	private String rPlotCmd;
	private String nodeName;
	private PanelView pv;
	
	private REXP rexp;
	private REXPDatasetProvider dsProvider;

	/**
	 * Create a {@link PanelViewLazyDatasetProvider}.
	 * @param rInterface
	 * 			The R interface for evaluating R expressions
	 * @param rPlotCmd
	 * 			The R plot command, used to obtain an REXP object
	 * @param nodeName
	 * 			The name of the node
	 */
	public PanelViewLazyDatasetProvider(ScapeRInterface rInterface, String rPlotCmd, String nodeName) {
		this.rInterface = rInterface;
		this.rPlotCmd = rPlotCmd;
		this.nodeName = nodeName;
		
	}
	
	/**
	 * Creates a {@link TableModel} for a {@link JTable} using a CDatasetTableModel 
	 * and a CDataCacheContainer (See Casper datasets). Returns a PanelView of that
	 * table.
	 * 
	 * @param name
	 * 			The name of the table
	 * @param container
	 * 			A CDataCacheContainer (see Casper datsets)
	 * @return
	 * 			PanelView of a table
	 */
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
	
	/**
	 * Allows the dataset to be saved in CSV format.
	 */
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
		return dsProvider.getOutputDataset(0);
	}

	@Override
	public String getName() {
		return nodeName;
	}
	
	/**
	 * Returns the {@link PanelView} if it is not null (that is, it has already been created). Otherwise
	 * Uses the R interface to evaluate and R command to get an REXP object which is then used
	 * to get an REXPDatasetProvider. Calls createPanelView() to get the PanelView and then returns it. 
	 * 
	 * @return	{@link PanelView}
	 */
	@Override
	public PanelView getPanelView() {
		
		if(pv != null){
			return pv;
		}
		
		try{
			rexp = rInterface.parseEvalTry(rPlotCmd);
			dsProvider = new REXPDatasetProvider(nodeName, rexp);
		}catch(RFaceException e){
			System.out.println(e.getMessage());
		}
		
		try {	
			pv = createPanelView(nodeName, getDataset());
			return pv;
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
		//pv.removeAll();
		//rexp = null;
		//dsProvider = null;		
	}

}
