package org.jamsim.ascape.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import org.apache.commons.lang.ArrayUtils;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.runtime.swing.navigator.PanelViewTable;
import org.ascape.util.swing.AscapeGUIUtil;
import org.ascape.util.swing.PanelViewUtil;
import org.ascape.view.vis.PanelView;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.weights.WeightCalculator;
import org.jamsim.io.ParameterSet;
import org.jamsim.shared.InvalidDataException;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

/**
 * Provides a {@link PanelView} based on a {@link ParameterSet}. The
 * {@link PanelView} contains a table of the {@link ParameterSet} as well as
 * update and reset/default buttons.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class NewPanelView implements PanelViewProvider, ActionListener {

	private final Map<String, Map<String, WeightCalculator>> wcalcsvarmaps;
	private final String[] wcalcvarnames;
	private final MicroSimScape<?> scape;
	private final String[] groupNames;
	private PanelView pv;
	private final JLabel grouplabel;
	private final JComboBox grouper;
	private final Dimension groupdim;
	private final Dimension selectdim;
	private final JLabel selectlabel;
	private final JComboBox selector;
	// private final JScrollPane tablePane;
	// private final JRadioButton nonebutton;
	// private final JRadioButton sesbutton;
	// private final JRadioButton ethbutton;
	// private final JPanel radioPanel;
	private final JTabbedPane yeartabs;
	private final JScrollPane year1;
	private final JScrollPane year2;
	private final JScrollPane year3;
	private final JScrollPane year4;
	private final JScrollPane year5;
	private final JScrollPane year6;
	private final JScrollPane year7;
	private final JScrollPane year8;
	private final JScrollPane year9;
	private final JScrollPane year10;
	private final JScrollPane year11;
	private final JScrollPane year12;
	private final JScrollPane year13;

	private Map<String, WeightCalculator> currentwcmap;

	// static String noneradio = "None";
	// static String sesradio = "SES at birth";
	// static String ethradio = "Ethnicity";

	public NewPanelView(
			Map<String, Map<String, WeightCalculator>> wcalcsvarmaps,
			MicroSimScape<?> scape) {
		this.wcalcsvarmaps = wcalcsvarmaps;
		this.wcalcvarnames = wcalcsvarmaps.keySet().toArray(
				new String[wcalcsvarmaps.size()]);
		this.scape = scape;

		groupNames = new String[] { "None", "SES at birth", "Ethnicity" };
		// create GUI elements
		pv = PanelViewUtil.createResizablePanelView("Scenario Weightings");
		grouplabel = new JLabel();
		grouper = new JComboBox(groupNames);
		groupdim = new Dimension(100, 40);
		grouper.setMaximumSize(groupdim);
		selectlabel = new JLabel();
		selector = new JComboBox(wcalcvarnames);
		selectdim = new Dimension(420, 40);
		selector.setMaximumSize(selectdim);
		// nonebutton = new JRadioButton(noneradio);
		// sesbutton = new JRadioButton(sesradio);
		// ethbutton = new JRadioButton(ethradio);
		// ButtonGroup breakdown = new ButtonGroup();
		// breakdown.add(nonebutton);
		// breakdown.add(sesbutton);
		// breakdown.add(ethbutton);
		// radioPanel = new JPanel(new GridLayout(0, 1));
		// radioPanel.add(nonebutton);
		// radioPanel.add(sesbutton);
		// radioPanel.add(ethbutton);
		yeartabs = new JTabbedPane();
		year1 = new JScrollPane();
		year2 = new JScrollPane();
		year3 = new JScrollPane();
		year4 = new JScrollPane();
		year5 = new JScrollPane();
		year6 = new JScrollPane();
		year7 = new JScrollPane();
		year8 = new JScrollPane();
		year9 = new JScrollPane();
		year10 = new JScrollPane();
		year11 = new JScrollPane();
		year12 = new JScrollPane();
		year13 = new JScrollPane();

		BuildResult uiElements = SwingJavaBuilder.build(this);
		// nonebutton.setSelected(true);

		// ButtonModel subgroup = breakdown.getSelection();
		// add YAML panel
		pv.add((Component) uiElements.get("panel"));
		// create GUI elements
		// pv = PanelViewUtil.createResizablePanelView("Scenario Weightings");
		// selector = new JComboBox(wcalcNames);
		// tablePane = new JScrollPane();
		// BuildResult uiElements = SwingJavaBuilder.build(this);

		// add YAML panel
		// pv.add((Component) uiElements.get("panel"));
	}

	protected final JComponent makeTextPanel(String text) {
		JPanel panel = new JPanel(false);
		JLabel filler = new JLabel(text);
		filler.setHorizontalAlignment(JLabel.CENTER);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(filler);
		return panel;
	}

	@Override
	public String getName() {
		return "New Panel View";
	}

	@Override
	public PanelView getPanelView() {
		// set current weight calculator
		currentwcmap = wcalcsvarmaps.values().iterator().next();
		setTablePane(currentwcmap.get("Year 1"), year1);
		setTablePane(currentwcmap.get("Year 2"), year2);
		setTablePane(currentwcmap.get("Year 3"), year3);
		setTablePane(currentwcmap.get("Year 4"), year4);
		setTablePane(currentwcmap.get("Year 5"), year5);
		setTablePane(currentwcmap.get("Year 6"), year6);
		setTablePane(currentwcmap.get("Year 7"), year7);
		setTablePane(currentwcmap.get("Year 8"), year8);
		setTablePane(currentwcmap.get("Year 9"), year9);
		setTablePane(currentwcmap.get("Year 10"), year10);
		setTablePane(currentwcmap.get("Year 11"), year11);
		setTablePane(currentwcmap.get("Year 12"), year12);
		setTablePane(currentwcmap.get("Year 13"), year13);

		int sIndex = ArrayUtils.indexOf(wcalcvarnames, currentwcmap.values()
				.iterator().next().getName());

		if (sIndex == -1) {
			throw new RuntimeException("Can't find wcalc named "
					+ currentwcmap.keySet().toArray(new String[0]).toString());
		}

		grouper.setSelectedIndex(0);
		selector.setSelectedIndex(sIndex);

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

	@SuppressWarnings("unused")
	private void selectorChanged() {
		Object selected = selector.getSelectedItem();
		currentwcmap = wcalcsvarmaps.get(selected);
		setTablePane(currentwcmap.get("Year 1"), year1);
		setTablePane(currentwcmap.get("Year 2"), year2);
		setTablePane(currentwcmap.get("Year 3"), year3);
		setTablePane(currentwcmap.get("Year 4"), year4);
		setTablePane(currentwcmap.get("Year 5"), year5);
		setTablePane(currentwcmap.get("Year 6"), year6);
		setTablePane(currentwcmap.get("Year 7"), year7);
		setTablePane(currentwcmap.get("Year 8"), year8);
		setTablePane(currentwcmap.get("Year 9"), year9);
		setTablePane(currentwcmap.get("Year 10"), year10);
		setTablePane(currentwcmap.get("Year 11"), year11);
		setTablePane(currentwcmap.get("Year 12"), year12);
		setTablePane(currentwcmap.get("Year 13"), year13);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("update".equals(cmd)) {
			update();
		} else if ("reset".equals(cmd)) {
			reset();
		}
	}

	private void setTablePane(WeightCalculator wc, JScrollPane scrollPane) {
		JTable table = createTable(wc);
		scrollPane.setViewportView(table);
		table.setRowHeight(20);
		table.setIntercellSpacing(new Dimension(10, 4));
	}

	private JTable createTable(ParameterSet pset) {
		JTable table = UIUtil.createTable(pset.getTableModel(), pset.getName());
		AscapeGUIUtil.sizeTable(table, AscapeGUIUtil.getDesktopSize());
		return table;
	}

	@SuppressWarnings("unused")
	private void update() {
		doUpdate("Weights updated.");
	}

	private void doUpdate(String updateMsg) {
		try {
			for (Map<String, WeightCalculator> wcalcsyearsmap : wcalcsvarmaps
					.values()) {
				for (WeightCalculator wcalc : wcalcsyearsmap.values()) {
					wcalc.validateAndNotify();
				}
			}
			JOptionPane.showMessageDialog(pv, updateMsg);
		} catch (InvalidDataException e) {
			// display message box
			JOptionPane.showMessageDialog(pv, e.getMessage());
		}
	}

	private void reset() {
		JOptionPane.showMessageDialog(pv, "Reset to defaults.");
	}

	// @Override
	// public PanelView getPanelView() {
	// // TODO Auto-generated method stub
	// return null;
	// }
}