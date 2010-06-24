package org.jamsim.ascape.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.util.swing.AscapeGUIUtil;
import org.ascape.util.swing.PanelViewUtil;
import org.ascape.view.vis.PanelView;
import org.jamsim.io.ParameterSet;

/**
 * Provides a {@link PanelView} based on a {@link ParameterSet}. The
 * {@link PanelView} contains a table of the {@link ParameterSet} as well as a
 * update button.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class PanelViewParameterSet implements PanelViewProvider,
		ActionListener {

	private final ParameterSet pset;

	/**
	 * Construct {@link PanelViewParameterSet} from {@link ParameterSet}.
	 * 
	 * @param pset
	 *            parameter set
	 */
	public PanelViewParameterSet(ParameterSet pset) {
		this.pset = pset;
	}

	@Override
	public String getName() {
		return pset.getName();
	}

	@Override
	public PanelView getPanelView() {

		TableCellRenderer dblRenderer = UIUtil.getDoubleCellRenderer();

		JTable table = new JTable(pset.getTableModel()); // NOPMD
		table.setName(pset.getName());
		table.setDefaultRenderer(Double.class, dblRenderer);

		// Create a PanelView from the Table
		Dimension desktopSize = AscapeGUIUtil.getDesktopSize();
		PanelView pv = PanelViewUtil.createPanelView(table, desktopSize);

		// set FlowLayout so button can be seen
		pv.setLayout(new FlowLayout());

		JButton btn = new JButton("Update");
		btn.setActionCommand("update");
		btn.addActionListener(this);
		pv.add(btn);

		JButton btn2 = new JButton("Defaults");
		btn2.setActionCommand("reset");
		btn2.addActionListener(this);
		pv.add(btn2);

		return pv;
	}


	@Override
	public void panelViewAdded(Container pvFrameImp) {
		// nothing to do
	}

	@Override
	public void frameClosed() {
		// nothing to do
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("update".equals(e.getActionCommand())) {
			pset.update();
		} else {
			pset.resetDefaults();
		}
	}

}
