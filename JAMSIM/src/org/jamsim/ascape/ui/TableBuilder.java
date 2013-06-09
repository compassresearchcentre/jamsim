package org.jamsim.ascape.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import net.casper.data.model.CDataGridException;
import net.casper.ext.swing.CDatasetTableModel;

import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.util.swing.AscapeGUIUtil;
import org.ascape.util.swing.PanelViewUtil;
import org.ascape.view.vis.PanelView;
import org.jamsim.ascape.DataDictionary;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.output.REXPDatasetProvider;
import org.jamsim.ascape.r.ScapeRInterface;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;
import org.omancode.r.RFaceException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;

import com.google.common.collect.ListMultimap;

/**
 * Provides a {@link PanelView} Containing various components for use in custom
 * building a table based on the user's selection from Scenario, Summary
 * Measure, Variable and Subgroup options. Provides the option to save the table
 * to the Navigator tree.
 * 
 * @author bmac055
 * 
 */
public class TableBuilder implements PanelViewProvider, ActionListener {

	private static final String NONE = "None";

	private MicroSimScape scape;

	private PanelView pv;

	private Map<String, String> scenarioDescriptionToVarname;

	private String scenarioSelection;
	private String statisticSelection;
	private String variableSelection;
	private String subgroupSelection;

	private String[] summaryMeasures = new String[] {"Frequencies", "Means", "Quintiles"};

	private JLabel scenarioLabel;
	private JLabel statisticLabel;
	private JLabel variableLabel;
	private JLabel subgroupLabel;
	private JLabel previewLabel;

	private JComboBox scenarioCombo;
	private JComboBox variableCombo;
	private JComboBox<Object> subgroupCombo;

	private JRadioButton frequenciesButton;
	private JRadioButton meansButton;
	private JRadioButton quintilesButton;
	private ButtonGroup statisticButtonGroup;

	private Map<String, ComboBoxModel> variableComboModels;

	private JScrollPane tablePane;

	private ScapeRInterface rInterface;

	private JTable table;
	private CDatasetTableModel tmodel;
	private REXPDatasetProvider dsProvider;
	private REXP rexp;

	private DataDictionary dict;

	private Map<String, DefaultComboBoxModel<Object>> subgroupComboBoxModels;

	/**
	 * Creates a {@link TableBuilder}. Sets up the combo boxes, combo
	 * box models radio buttons and maps used to apply the user's selections.
	 * Builds the Swing components using the Swing JavaBuilder library.
	 * 
	 * @param tableBuilderData
	 *            A Map of Maps to be used in setting up and applying the user's
	 *            selection
	 * @param scape
	 *            The scape
	 */
	public TableBuilder(TableBuilderConfig tableBuilderConfig,
			MicroSimScape scape) {

		this.scape = scape;
		this.dict = scape.getDictionary();

		rInterface = scape.getScapeRInterface();

		scenarioLabel = new JLabel();
		statisticLabel = new JLabel();
		variableLabel = new JLabel();
		subgroupLabel = new JLabel();
		previewLabel = new JLabel();

		frequenciesButton = new JRadioButton("Percentages");
		meansButton = new JRadioButton("Means");
		quintilesButton = new JRadioButton("Quantiles");

		statisticButtonGroup = new ButtonGroup();
		statisticButtonGroup.add(frequenciesButton);
		statisticButtonGroup.add(meansButton);
		statisticButtonGroup.add(quintilesButton);

		tablePane = new JScrollPane();
		tablePane.setPreferredSize(new Dimension(490, 150));
		tablePane.setVisible(false);

		variableComboModels = new LinkedHashMap<String, ComboBoxModel>();
		setupScenarioToRExpression();
		this.variableComboModels = setupVariableComboModels(tableBuilderConfig);
		this.subgroupComboBoxModels = createSubgroupComboModels(tableBuilderConfig.getSubgroupsByVariable());

		pv = PanelViewUtil.createResizablePanelView("Table Builder");
		pv.setPreferredSize(new Dimension(600, 450));

		BuildResult uiElements = SwingJavaBuilder.build(this);
		pv.add((Component) uiElements.get("pane"));
	}

	/**
	 * Sets up the map of combo box models for use in the variable combo box
	 */
	private Map<String, ComboBoxModel>  setupVariableComboModels(
			TableBuilderConfig tableBuilderConfig) {

		Map<String, ComboBoxModel> comboBoxModels = new HashMap<String, ComboBoxModel>();
		
		List<String> freqVars = dict.getDescriptions(tableBuilderConfig.getVariablesForFrequencies());
		List<String> meanVars = dict.getDescriptions(tableBuilderConfig.getVariablesForMeans());
		List<String> quintileVars = dict.getDescriptions(tableBuilderConfig.getVariablesForQuintiles());

		Collections.sort(freqVars);
		Collections.sort(meanVars);
		Collections.sort(quintileVars);
		
		comboBoxModels.put("Frequencies",
				new DefaultComboBoxModel(freqVars.toArray()));
		comboBoxModels.put("Means",
				new DefaultComboBoxModel(meanVars.toArray()));
		comboBoxModels.put("Quintiles",
				new DefaultComboBoxModel(quintileVars.toArray()));
		
		return comboBoxModels;
	}

	
	private Map<String, DefaultComboBoxModel<Object>> createSubgroupComboModels(
			ListMultimap<String, String> variablesToSubgroups) {

		Map<String, DefaultComboBoxModel<Object>> comboBoxModels = new HashMap<String, DefaultComboBoxModel<Object>>();
		
		for (Map.Entry<String, Collection<String>> variableToSubgroups : variablesToSubgroups.asMap().entrySet()) {
			String variableName = variableToSubgroups.getKey();
			String variableDesc = dict.getDescription(variableName);
			Collection<String> subgroupingVariableNames = variableToSubgroups.getValue();
			List<String> subgroupingVariableDescriptions = dict.getDescriptions(subgroupingVariableNames);
			Collections.sort(subgroupingVariableDescriptions);
			subgroupingVariableDescriptions.add(0, NONE);
			
			comboBoxModels.put(variableDesc, new DefaultComboBoxModel<Object>(subgroupingVariableDescriptions.toArray()));
		}
		
		return comboBoxModels;
	}

	
	/**
	 * Sets up the Map that corresponds to the combo box where the user selects
	 * the scenario they wish to examine. Uses the R interface to get a list of
	 * scenarios currently existing in the R environment
	 */
	private void setupScenarioToRExpression() {

		scenarioDescriptionToVarname = new LinkedHashMap<String, String>();
		scenarioDescriptionToVarname.put("Base", "Base");
		try {
			REXP rexp = scape.getScapeRInterface().eval(
					"as.list(names(envs)[-length(envs)])");

			List<REXPString> list = rexp.asList();

			for (REXPString o : list) {
				scenarioDescriptionToVarname.put(o.asString(), o.asString());
			}

			scenarioCombo = new JComboBox(scenarioDescriptionToVarname.keySet()
					.toArray());
			scenarioCombo.setSelectedItem("Base");
			scenarioSelected();

		} catch (RFaceException e) {
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		}
	}

	private String lookupVarname(String description) {
		return (NONE.equals(description) ? "" : dict.getVarname(description));
	}
	
	/**
	 * Creates tableBuilderExpression using the selections made by the user in
	 * the interface. Uses tableBuilderExpression to call the tableBuilder
	 * function in the R workspace and obtains an REXP object. Obtains an
	 * REXPDatasetProvider and uses this to create a table model for display in
	 * a table in the UI
	 */
	private void setupTablePane() {

		try {
			String tableBuilderExpression = "tableBuilder('"
					+ scenarioSelection
					+ "','"
					+ statisticSelection
					+ "','"
					+ lookupVarname(variableSelection)
					+ "','"
					+ lookupVarname(subgroupSelection)
							.toString() + "')";

			System.out.println(tableBuilderExpression);
			rexp = rInterface.parseEvalTry(tableBuilderExpression);

			dsProvider = new REXPDatasetProvider(variableSelection + " by "
					+ subgroupSelection + " - " + scenarioSelection, rexp);
			tmodel = new CDatasetTableModel(dsProvider.getOutputDataset(0));

			table = setupTable(dsProvider.getName(), tmodel);

			tablePane.setViewportView(table);
			table.setRowHeight(20);
			table.setIntercellSpacing(new Dimension(10, 4));

		} catch (IOException e) {
			e.printStackTrace();
		} catch (CDataGridException e) {
			e.printStackTrace();
		}
	}

	private JTable setupTable(String name, TableModel tmodel) {
		JTable table = UIUtil.createTable(tmodel, name);
		AscapeGUIUtil.sizeTable(table, AscapeGUIUtil.getDesktopSize());
		return table;
	}

	private void scenarioSelected() {
		scenarioSelection = scenarioCombo.getSelectedItem().toString();
	}

	/**
	 * Changes the combo box model in the variableCombo combo box when the
	 * summary measure is changed
	 * 
	 * @param indexOfStatisticNames
	 *            The index to use to obtain the correct summary measure from
	 *            the array of summary measures
	 */
	public void summaryMeasureChanged(int indexOfStatisticNames) {
		statisticSelection = summaryMeasures[indexOfStatisticNames];
		variableCombo.setModel(variableComboModels.get(statisticSelection));
		variableCombo.setSelectedIndex(0);
	}
	

	private void frequenciesSelected() {
		summaryMeasureChanged(0);
	}

	private void meansSelected() {
		summaryMeasureChanged(1);
	}

	private void quintilesSelected() {
		summaryMeasureChanged(2);
	}

	private void variableSelected() {
		variableSelection = variableCombo.getSelectedItem().toString();
		populateSubgroupCombo(variableSelection);
	}

	private void subgroupSelected() {
		subgroupSelection = subgroupCombo.getSelectedItem().toString();
	}

	private void populateSubgroupCombo(String variableDescription) {
		ComboBoxModel<Object> cbm = subgroupComboBoxModels.get(variableDescription);
		subgroupCombo.setModel(cbm);
		subgroupCombo.setSelectedIndex(0);
		
	}

	/**
	 * Creates an expression to be passed to R that stores the table built by
	 * the user so that it may be recreated when the model is reloaded or when
	 * the workspace is loaded at another time.
	 * 
	 * @return a String expression for the R workspace to store and use
	 */
	private String buildStoreOnLoadExpression() {

		String expr = "\"addLazyTableNode('tableBuilder('" + scenarioSelection
				+ "', '" + statisticSelection + "', '"
				+ lookupVarname(variableSelection) + "', '"
				+ lookupVarname(subgroupSelection)
				+ "')', '" + variableSelection + " by " + subgroupSelection
				+ " - " + scenarioSelection + "', " + "'nameOfParentNode', "
				+ "'path')\"";

		return expr;
	}

	/**
	 * Checks that a scenario, summary measure, and variable at least have been
	 * selected and sets up the table pane.
	 */
	private void okPressed() {

		if (scenarioSelection != null && statisticSelection != null
				&& variableSelection != null) {

			if (subgroupSelection == null) {
				subgroupCombo.setSelectedItem(NONE);
			}
			setupTablePane();
			tablePane.setVisible(true);
		}
	}

	/**
	 * Checks that the dataset provider exists and uses that to save the custom
	 * table to the Navigator tree. Stores an expression in the R environment
	 * that may be used to restore the table when this JAMSIM workspace is
	 * loaded at a later time
	 */
	private void savePressed() {
		if (dsProvider != null) {

			String expr = buildStoreOnLoadExpression();

			try {
				rInterface.assign("expr", expr);
				rInterface.parseEvalTry("storeOnLoadExpression(expr)");
			} catch (RFaceException e) {
				e.printStackTrace();
			}

			scape.getScapeNode().addOutputNodeFromTableBuilder(dsProvider,
					"User/" + scenarioSelection + "/" + statisticSelection);
			dsProvider = null;
		}
	}

	public PanelView getPanelView() {
		return pv;
	}

	public String getName() {
		return "PanelViewCreateTables";
	}

	public void panelViewAdded(Container pvFrameImp) {
	}

	public void frameClosed() {
	}

	public void actionPerformed(ActionEvent e) {
	}
}
