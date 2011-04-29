package org.jamsim.ascape.ui;

import java.awt.Component;
import java.awt.Container;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.commons.lang.ArrayUtils;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
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
 * Provides a {@link PanelView} which allows a combo box selection from a set of
 * {@link WeightCalculator}s. The selected {@link WeightCalculator} will be
 * displayed below the combo box in a table as well as a update and
 * reset/defaults button.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class PanelViewWeightCalculators implements PanelViewProvider {

	private final Map<String, WeightCalculator> wcalcs;
	private final String[] wcalcNames;
	private final MicroSimScape<?> scape;

	private final PanelView pv;
	private final JComboBox selector;
	private final JScrollPane tablePane;

	private WeightCalculator currentwc;

	/**
	 * Construct {@link PanelViewWeightCalculators} from
	 * {@link WeightCalculator}s.
	 * 
	 * @param wcalcs
	 *            parameter sets
	 * @param scape
	 *            scape
	 */
	public PanelViewWeightCalculators(Map<String, WeightCalculator> wcalcs,
			MicroSimScape<?> scape) {
		this.wcalcs = wcalcs;
		this.wcalcNames = wcalcs.keySet().toArray(new String[wcalcs.size()]);
		this.scape = scape;

		// create GUI elements
		pv = PanelViewUtil.createResizablePanelView("Weightings");
		selector = new JComboBox(wcalcNames);
		tablePane = new JScrollPane();
		BuildResult uiElements = SwingJavaBuilder.build(this);

		// add YAML panel
		pv.add((Component) uiElements.get("panel"));
	}

	@Override
	public String getName() {
		return "Weightings";
	}

	@Override
	public PanelView getPanelView() {
		// set current weight calculator
		currentwc = scape.getWeightCalculator();
		setTablePane(currentwc);

		int sIndex = ArrayUtils.indexOf(wcalcNames, currentwc.getName());

		if (sIndex == -1) {
			throw new RuntimeException("Can't find wcalc named "
					+ currentwc.getName());
		}

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
	private void update() {
		doUpdate("Weights updated.");
	}

	@SuppressWarnings("unused")
	private void reset() {
		currentwc.resetDefaults();
		doUpdate("Weights reset to base.");
	}

	private void doUpdate(String updateMsg) {
		try {
			if (scape.getWeightCalculator() != currentwc) {
				scape.setWeightCalculator(currentwc);
			} else {
				currentwc.validateAndNotify();
			}

			currentwc.saveState(scape.getPrefs());
			JOptionPane.showMessageDialog(pv, updateMsg);
		} catch (InvalidDataException e) {
			// display message box
			JOptionPane.showMessageDialog(pv, e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private void selectorChanged() {
		Object selected = selector.getSelectedItem();
		currentwc = wcalcs.get(selected);
		setTablePane(currentwc);
	}

	private void setTablePane(WeightCalculator wc) {
		JTable table = createTable(wc);
		tablePane.setViewportView(table);
	}

	private JTable createTable(ParameterSet pset) {
		JTable table =
				UIUtil.createTable(pset.getTableModel(), pset.getName());
		AscapeGUIUtil.sizeTable(table, AscapeGUIUtil.getDesktopSize());
		return table;
	}

}
