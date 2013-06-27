package org.jamsim.ascape.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.commons.lang.ArrayUtils;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.util.swing.AscapeGUIUtil;
import org.ascape.util.swing.PanelViewUtil;
import org.ascape.view.vis.PanelView;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.RootScape;
import org.jamsim.ascape.weights.CategoricalVarAdjustment;
import org.jamsim.ascape.weights.WeightCalculator;
import org.jamsim.io.ParameterSet;
import org.jamsim.shared.InvalidDataException;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

/**
 * Provides a {@link PanelView} based on a {@link ParameterSet}. The
 * {@link PanelView} contains a table of the {@link ParameterSet} as well as
 * update and reset/default buttons, Combo boxes and buttons for the user to 
 * build a subgroup formula and a Base Simulation Results table for categorical
 * variables.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ScenarioBuilder implements PanelViewProvider, ActionListener {

	private static final String NONE = "None";

	private static final String[] RUNS_COMBO_ITEMS = new String[] { "1 run", "2 runs",
			"3 runs", "4 runs", "5 runs", "6 runs", "7 runs", "8 runs",
			"9 runs", "10 runs" };

	private final Map<String, Map<String, WeightCalculator>> allvariablesweightcalcs;

	private final Map<String, RExpression> subgroupsToOptions;
	
	private final String[] wcalcvarnames;
	
	private final String[] subgroupdescriptions;
	
	private Map<String, ComboBoxModel> optionsComboModels;
	
	private final MicroSimScape<?> scape;

	private PanelView pv;
	
	private final JLabel selectlabel;
	private final JLabel subgrouplabel;
	private final JLabel subgroupselectlabel;
	private final JLabel optionslabel;
	private final JLabel scenarioproportionslabel;
	private final JLabel basesimulationresultslabel;
	private final JLabel namelabel;
	
	private final JTextField subgroupbox;
	private final JTextField namebox;
	private final JComboBox selector;
	private final JComboBox subgroupselect;
	private final JComboBox optionsCombo;
	private final JComboBox runsCombo;
	private final JScrollPane yearpane;
	private JScrollPane baseSimulationResultsPane;

	private Map<String, WeightCalculator> currentvariableallyears;
	private String currentselection;
	
	/**
	 * Creates a {@link ScenarioBuilder}. Sets up the combo boxes, combo box models
	 * and maps used to apply the user's selections.
	 * Builds the Swing components using the Swing JavaBuilder library.
	 * 
	 * @param wcalcsvarmaps
	 * 			Weight Calculators used to create the Scenario Proportions table
	 * @param subgroupsToOptions
	 * 			Descriptions and corresponding R expressions used for the combo boxes
	 * 			and to build the user's R expression
	 * @param scape
	 * 			The scape
	 */
	public ScenarioBuilder(
			Map<String, Map<String, WeightCalculator>> wcalcsvarmaps,
			Map<String, RExpression> subgroupsToOptions,
			MicroSimScape<?> scape) {
		
		this.allvariablesweightcalcs = wcalcsvarmaps;
		this.wcalcvarnames = wcalcsvarmaps.keySet().toArray(
				new String[wcalcsvarmaps.size()]);
		
		this.scape = scape;
		
		this.subgroupsToOptions = subgroupsToOptions;	
		subgroupdescriptions = this.subgroupsToOptions.keySet().toArray(
								new String[subgroupsToOptions.size()]);
		optionsComboModels = setupComboBoxModels();		

		pv = PanelViewUtil.createResizablePanelView("Scenario Builder");
		pv.setPreferredSize(new Dimension(700,693));

		selectlabel = new JLabel();
		selector = new JComboBox(wcalcvarnames);
		subgrouplabel = new JLabel();
		subgroupbox = new JTextField(20);
		subgroupselectlabel = new JLabel();
		subgroupselect = new JComboBox(subgroupdescriptions);
		runsCombo = new JComboBox(RUNS_COMBO_ITEMS);
		runsCombo.setSelectedIndex(1);
		namebox = new JTextField(10);
		namelabel = new JLabel();
		
		optionsCombo = new JComboBox();
		optionsCombo.setModel(optionsComboModels.get(""));	//start with list of options for the "none" subgroup
		optionslabel = new JLabel();
		scenarioproportionslabel = new JLabel();
		basesimulationresultslabel = new JLabel();	
		yearpane = new JScrollPane();
		baseSimulationResultsPane = new JScrollPane();


		BuildResult uiElements = SwingJavaBuilder.build(this);
		pv.add((Component) uiElements.get("pane"));
	}
	
	/**
	 * Gets the variable selected and updates the Scenario Proportions table pane.
	 * If it is a categorical variable also updates the Base Simulation Results table pane.
	 */
	@SuppressWarnings("unused")
	private void selectorChanged() {
		Object selected = selector.getSelectedItem();
		currentvariableallyears = allvariablesweightcalcs.get(selected);
		for (Entry<String, WeightCalculator> wcalcentry : currentvariableallyears
				.entrySet()) {
			setTablePane(wcalcentry.getValue());
			if(wcalcentry.getValue() instanceof CategoricalVarAdjustment){
				
				basesimulationresultslabel.setVisible(true);
				baseSimulationResultsPane.setVisible(true);
				setBaseSimulationResultsTablePane(wcalcentry.getValue());
				
			} else {
				basesimulationresultslabel.setVisible(false);
				baseSimulationResultsPane.setVisible(false);
			}
		}
	}
	
	/**
	 * Updates the Subgroup Formula textbox based on the user's selection
	 * @param s
	 */
	private void updateSubgroupFormula(String s){
		subgroupbox.setText(subgroupbox.getText() + s);
	}
	
	private void addLeftBracket(){
		updateSubgroupFormula("(");
		subgroupbox.requestFocus();
	}
	
	private void andPressed(){
		updateSubgroupFormula("&");
		subgroupbox.requestFocus();
	}
	
	private void addRightBracket(){
		updateSubgroupFormula(")");
		subgroupbox.requestFocus();
	}
	
	private void orPressed(){
		updateSubgroupFormula("|");
		subgroupbox.requestFocus();
	}
	
	private void bsPressed(){
		subgroupbox.setText(subgroupbox.getText().substring(0, subgroupbox.getText().length()-1));
		subgroupbox.requestFocus();
	}
	
	private void lePressed(){
		updateSubgroupFormula("<=");
		subgroupbox.requestFocus();
	}
	
	private void ltPressed(){
		updateSubgroupFormula("<");
		subgroupbox.requestFocus();
	}
	
	private void gePressed(){
		updateSubgroupFormula(">=");
		subgroupbox.requestFocus();
	}
	
	private void gtPressed(){
		updateSubgroupFormula(">");
		subgroupbox.requestFocus();
	}
	
	private void eqPressed(){
		updateSubgroupFormula("==");
		subgroupbox.requestFocus();
	}
	
	private void zeroPressed(){
		updateSubgroupFormula("0");
		subgroupbox.requestFocus();
	}
	
	private void onePressed(){
		updateSubgroupFormula("1");
		subgroupbox.requestFocus();
	}
	
	private void twoPressed(){
		updateSubgroupFormula("2");
		subgroupbox.requestFocus();
	}
	
	private void threePressed(){
		updateSubgroupFormula("3");
		subgroupbox.requestFocus();
	}
	
	private void fourPressed(){
		updateSubgroupFormula("4");
		subgroupbox.requestFocus();
	}
	
	private void fivePressed(){
		updateSubgroupFormula("5");
		subgroupbox.requestFocus();
	}
	
	private void sixPressed(){
		updateSubgroupFormula("6");
		subgroupbox.requestFocus();
	}
	
	private void sevenPressed(){
		updateSubgroupFormula("7");
		subgroupbox.requestFocus();
	}
	
	private void eightPressed(){
		updateSubgroupFormula("8");
		subgroupbox.requestFocus();
	}
	
	private void ninePressed(){
		updateSubgroupFormula("9");
		subgroupbox.requestFocus();
	}

	/**
	 * Gets the variable selected by the user. Gets the Weight Calculator corresponding to that variable.
	 * Uses the subgroup formula built by the user to update the table panes accordingly
	 */
	private void setFormula(){
		
		Object selected = selector.getSelectedItem();
		currentvariableallyears = allvariablesweightcalcs.get(selected);
		for (Entry<String, WeightCalculator> wcalcentry : currentvariableallyears.entrySet()) {
			
			if(wcalcentry.getValue() instanceof CategoricalVarAdjustment){	
				((CategoricalVarAdjustment)wcalcentry.getValue()).setBaseSimulationResultsTableModel(subgroupbox.getText());
				setBaseSimulationResultsTablePane(wcalcentry.getValue()); //needed?
			}
		
		}
		subgroupbox.requestFocus();	
	}

	
	private void clearFormula(){
		subgroupselect.setSelectedIndex(0);
		subgroupbox.setText("");
		subgroupbox.requestFocus();
	}
	
	/**
	 * Gets the R expression corresponding to the user's choice of subgroup option.
	 * Updates the subgroup formula.
	 */
	private void optionSelected(){
		if (!optionsCombo.getSelectedItem().toString().equals(NONE)) {
			currentselection = subgroupsToOptions.get(subgroupselect.getSelectedItem())
					 .getSubExpressions()
					 .get(optionsCombo.getSelectedItem().toString())
					 .getRExpression();

			updateSubgroupFormula(currentselection);
			subgroupbox.requestFocus();
		}
	}
	
	/**
	 * Gets the R expression corresponding to the user's choice of subgroup.
	 * updates the subgroup formula.
	 */
	private void subgroupSelected(){
		Object selected = subgroupselect.getSelectedItem();
		currentselection = subgroupsToOptions.get(selected).getRExpression();
		
		updateSubgroupFormula(currentselection);
		changeOptions(subgroupsToOptions.get(selected).rExpression);
		
		subgroupbox.requestFocus();
	}
	
	/**
	 * Changes the combo box model for the 'options' combo box depending on the
	 * user's selection of subgroup.
	 * @param optiontype
	 * 			The subgroup variable type - range, categorical, binary
	 */
	@SuppressWarnings("unchecked")
	private void changeOptions(String optiontype){
		optionsCombo.setModel(optionsComboModels.get(optiontype));
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
	
	/**
	 * Creates and sets up the Base Simulation Results table pane depending on the user's
	 * selection of variable
	 * @param wc
	 * 			The user's variable selection
	 */
	private void setBaseSimulationResultsTablePane(WeightCalculator wc) {
		JTable table = createExistingProportionsTable((CategoricalVarAdjustment) wc);
		baseSimulationResultsPane.setViewportView(table);
		baseSimulationResultsPane.setPreferredSize(new Dimension(660, 150));
		table.setRowHeight(20);
		table.setIntercellSpacing(new Dimension(10,4));
	}
	
	/**
	 * Creates and sets up the Scenario Proportions table pane depending on the user's
	 * selection of variable
	 * @param wc
	 * 			The user's variable selection
	 */
	private void setTablePane(WeightCalculator wc) {
		JTable table = createTable(wc);
		yearpane.setViewportView(table);
		yearpane.setPreferredSize(new Dimension(660, 150));
		table.setRowHeight(20);
		table.setIntercellSpacing(new Dimension(10, 4));
	}
	
	/**
	 * Creates the table for use in the Base Simulation Results table pane
	 * @param catvaradj
	 * 			The user's variable selection
	 * @return
	 * 			JTable
	 */
	private JTable createExistingProportionsTable(CategoricalVarAdjustment catvaradj){
		JTable table = UIUtil.createTable(catvaradj.getBaseSimulationResultsTableModel(), catvaradj.getName(), 110);
		AscapeGUIUtil.sizeTable(table, AscapeGUIUtil.getDesktopSize());
		return table;
	}
	
	/**
	 * Creates the table for use in the Scenario Proportions table pane
	 * @param pset
	 * 			The user's variable selection
	 * @return
	 * 			JTable
	 */
	private JTable createTable(ParameterSet pset) {
		JTable table = UIUtil.createTable(pset.getTableModel(), pset.getName(), 110);
		AscapeGUIUtil.sizeTable(table, AscapeGUIUtil.getDesktopSize());
		return table;
	}
	
	/**
	 * Sets up a map of ComboBoxModels used to change the options available to the 
	 * user based on their subgroup selection
	 * @return
	 * 			Map
	 */
	@SuppressWarnings("unchecked")
	private Map<String, ComboBoxModel> setupComboBoxModels() {

		Map<String, ComboBoxModel> optionsComboModels = new LinkedHashMap<String, ComboBoxModel>();

		for (String element : subgroupsToOptions.keySet()) {
				
			LinkedHashSet<String> subExpressions = new LinkedHashSet();
			
			subExpressions.add(NONE);
			subExpressions.addAll(subgroupsToOptions.get(element).getSubExpressions().keySet());
			
			optionsComboModels.put(subgroupsToOptions.get(element).rExpression, new DefaultComboBoxModel(
					subExpressions.toArray()));
		}

		return optionsComboModels;
	}

	@SuppressWarnings("unused")
	private void update() {
		doUpdate("Scenario set.");
	}

	private void run() {
		int numberRuns = runsCombo.getSelectedIndex()+1;
		RootScape rootScape = (RootScape) scape.getRoot();
		rootScape.setNumberRuns(numberRuns);
		rootScape.start();
	}
	/**
	 * Updates the current variable based on the changes made by the user.
	 * The next time a scenario is simulated it will be based on these changes.
	 * @param updateMsg
	 */
	private void doUpdate(String updateMsg) {
		try {
			scape.setGlobalSubgroupFilterExpression(subgroupbox.getText());
			for (Map<String, WeightCalculator> wcalcsyearsmap : allvariablesweightcalcs
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
	
	/**
	 * Restore default settings to the variables, removes any changes made to them
	 * so far by the user.
	 */
	private void reset() {
		for (WeightCalculator wcalc : currentvariableallyears.values()) {
			wcalc.resetDefaults();
		}
		JOptionPane.showMessageDialog(pv, "Current variable scenario removed.", "Defaults", JOptionPane.INFORMATION_MESSAGE);
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
	
	/**
	 * Sets up the PanelView
	 */
	@Override
	public PanelView getPanelView() {
		// set current weight calculator
		currentvariableallyears = allvariablesweightcalcs.values().iterator().next();

		int sIndex = ArrayUtils.indexOf(wcalcvarnames, currentvariableallyears
				.values().iterator().next().getName());

		if (sIndex == -1) {
			throw new RuntimeException("Can't find wcalc named "
					+ currentvariableallyears);
		}

		//grouper.setSelectedIndex(0);
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
}