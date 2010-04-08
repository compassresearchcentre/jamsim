package org.jamsim.ascape.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.util.swing.AscapeGUIUtil;
import org.ascape.util.swing.PanelViewUtil;
import org.ascape.view.vis.PanelView;
import org.jamsim.io.ParameterSet;
import org.omancode.swing.DoubleCellRenderer;

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

	public String getName() {
		return pset.getName();
	}

	public PanelView getPanelView() {

		TableCellRenderer dblRenderer = new DoubleCellRenderer();

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

	public static PanelView createPanelView(JTable table, Dimension maxSize) {
		// sizeAllColumnsToHeaderWidths(table);

		// tell the scroll pane enclosing the table to size its viewport
		// to the smaller of the table's preferred size or maxSize
		if (maxSize == null) {
			table
					.setPreferredScrollableViewportSize(table
							.getPreferredSize());
		} else {

			Dimension prefSize = table.getPreferredSize();

			table.setPreferredScrollableViewportSize(PanelViewUtil.min(
					prefSize, PanelViewUtil.subtract(maxSize,
							PanelViewUtil.TABLE_BORDER_EDGES)));
		}

		// allow sorting using the column headers
		table.setAutoCreateRowSorter(true);

		// do not resize the table column widths when the frame is resized;
		// instead show the scroll bars
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// create a new PanelView with the BorderLayout so that when
		// the panel view is resized so are the components on it
		// use the name of the table for the name of the Panel
		PanelView pv =
				PanelViewUtil.createPanelView(table.getName(),
						new BorderLayout());

		// In order for the table column headings to be visible,
		// it must be on a scroll pane
		JScrollPane scrollPane = new JScrollPane(table);

		pv.add(scrollPane, BorderLayout.CENTER);

		return pv;
	}

	public void panelViewAdded(Container pvFrameImp) {
		// nothing to do
	}

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
