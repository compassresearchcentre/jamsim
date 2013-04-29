package org.jamsim.ascape.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.casper.data.model.CDataGridException;
import net.casper.ext.swing.CDatasetTableModel;

import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.util.swing.AscapeGUIUtil;
import org.ascape.util.swing.PanelViewUtil;
import org.ascape.view.vis.PanelView;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.RootScape;
import org.jamsim.ascape.output.REXPDatasetProvider;
import org.jamsim.ascape.r.ScapeRInterface;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;
import org.omancode.r.RFaceException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;

/**
 * Provides a {@link PanelView} Containing various components for use in
 * custom building a table based on the user's selection from Scenario, 
 * Summary Measure, Variable and Subgroup options.
 * Provides the option to save the table to the Navigator tree.
 * 
 * @author bmac055
 *
 */
public class PanelViewTableBuilder implements PanelViewProvider, ActionListener{

	private MicroSimScape scape;
	
	private PanelView pv;
		
	private Map<String, Map<String, String>> summaryMeasuresToVariables;
	
	private Map<String, String> scenarioDescriptionToVarname;
	private Map<String, String> variableDescriptionToVarname;
	private Map<String, String> subgroupDescriptionToVarname;
	
	private String scenarioSelection;
	private String statisticSelection;
	private String variableSelection;
	private String subgroupSelection;
	
	private String[] statisticNames;
	private String[] subgroupNames;
	
	private JLabel scenarioLabel;
	private JLabel statisticLabel;
	private JLabel variableLabel;
	private JLabel subgroupLabel;
	
	private JComboBox scenarioCombo;
	private JComboBox variableCombo;
	private JComboBox subgroupCombo;
	
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
	
	/**
	 * Creates a {@link PanelViewTableBuilder}. Sets up the combo boxes, combo box models
	 * radio buttons and maps used to apply the user's selections.
	 * Builds the Swing components using the Swing JavaBuilder library.	 
	 * @param tableBuilderData
	 * 			A Map of Maps to be used in setting up and applying the user's selection
	 * @param scape
	 * 			The scape
	 */
	public PanelViewTableBuilder(
			Map<String, Map<String, String>> summaryMeasuresToVariables,
			Map<String, String> subgroupsToExpressions,
			Map<String, String> variablesToSubgroups,
			MicroSimScape scape){
		
		this.scape = scape;
		rInterface = scape.getScapeRInterface();
		
		this.summaryMeasuresToVariables = summaryMeasuresToVariables;
		
		subgroupDescriptionToVarname = subgroupsToExpressions;

		statisticNames = summaryMeasuresToVariables.keySet().toArray(new String[summaryMeasuresToVariables.size()]);
		subgroupNames = subgroupDescriptionToVarname.keySet().toArray(new String[subgroupDescriptionToVarname.size()]);
				
		scenarioLabel = new JLabel();
		statisticLabel = new JLabel();
		variableLabel = new JLabel();
		subgroupLabel = new JLabel();
		
		frequenciesButton = new JRadioButton(statisticNames[0]);
		meansButton = new JRadioButton(statisticNames[1]);
		quintilesButton = new JRadioButton(statisticNames[2]);
		
		statisticButtonGroup = new ButtonGroup();
		statisticButtonGroup.add(frequenciesButton);
		statisticButtonGroup.add(meansButton);
		statisticButtonGroup.add(quintilesButton);
		
		tablePane = new JScrollPane();
		tablePane.setPreferredSize(new Dimension(490,150));
		tablePane.setVisible(false);
		
		variableComboModels = new LinkedHashMap<String, ComboBoxModel>();
		setupScenarioToRExpression();		
		setupVariableComboModels(summaryMeasuresToVariables);
		subgroupCombo = new JComboBox(subgroupNames);
		variableCombo = new JComboBox();
		
		pv = PanelViewUtil.createResizablePanelView("Table Builder");
		pv.setPreferredSize(new Dimension(600,450));
		
		BuildResult uiElements = SwingJavaBuilder.build(this);
		pv.add((Component) uiElements.get("pane"));
	}
	
	/**
	 * Sets up the map of combo box models for use in the variable combo box
	 */
	private void setupVariableComboModels(Map<String, Map<String, String>> summaryMeasuresToVariables){
	
		variableComboModels.put("Frequencies", new DefaultComboBoxModel(summaryMeasuresToVariables.get("Frequencies")
																								  .keySet()
																								  .toArray()));
		variableComboModels.put("Means", new DefaultComboBoxModel(summaryMeasuresToVariables.get("Means")
																						    .keySet()
																							.toArray()));
		variableComboModels.put("Quintiles", new DefaultComboBoxModel(summaryMeasuresToVariables.get("Quintiles")
																							    .keySet()
																							    .toArray()));
	}
	
	/**
	 * Sets up the Map that corresponds to the combo box where the user selects the
	 * scenario they wish to examine. Uses the R interface to get a list of scenarios
	 * currently existing in the R environment 
	 */
	private void setupScenarioToRExpression(){
		
		scenarioDescriptionToVarname = new LinkedHashMap<String, String>();
		scenarioDescriptionToVarname.put("Base", "Base");
		try{
			REXP rexp = scape.getScapeRInterface().eval("as.list(names(envs)[-length(envs)])");
			
			List<REXPString> list = rexp.asList();
			
			for(REXPString o: list){
				scenarioDescriptionToVarname.put(o.asString(),  o.asString());
			}
			
			scenarioCombo = new JComboBox(scenarioDescriptionToVarname.keySet().toArray());
			scenarioCombo.setSelectedItem("Base");
			scenarioSelected();

		}catch(RFaceException e){ 
			e.printStackTrace();
		}catch(REXPMismatchException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates tableBuilderExpression using the selections made by the user in the interface.
	 * Uses tableBuilderExpression to call the tableBuilder function in the R workspace and obtains 
	 * an REXP object. Obtains an REXPDatasetProvider and uses this to create a table model for 
	 * display in a table in the UI
	 */
	private void setupTablePane(){
		
		try{
			String tableBuilderExpression = "tableBuilder('" +
					   						scenarioSelection + "','" +
					   						statisticSelection + "','" +
					   						variableDescriptionToVarname.get(variableSelection).toString() + "','" +
					   						subgroupDescriptionToVarname.get(subgroupSelection).toString() + "')";

			rexp = rInterface.parseEvalTry(tableBuilderExpression);
			
			dsProvider = new REXPDatasetProvider(variableSelection + " by " +
												 subgroupSelection + " - " +
												 scenarioSelection
												 , rexp);
			tmodel = new CDatasetTableModel(dsProvider.getOutputDataset(0));
			
			table = setupTable(dsProvider.getName(), tmodel);
			
			tablePane.setViewportView(table);
			table.setRowHeight(20);
			table.setIntercellSpacing(new Dimension(10,4));

			
		}catch(IOException e){
			e.printStackTrace();
		}catch(CDataGridException e){
			e.printStackTrace();
		}
	}
	
	private JTable setupTable(String name, TableModel tmodel){
		JTable table = UIUtil.createTable(tmodel, name);
		AscapeGUIUtil.sizeTable(table, AscapeGUIUtil.getDesktopSize());
		return table;
	}
	
	private void scenarioSelected(){
		scenarioSelection = scenarioCombo.getSelectedItem().toString();
	}
	
	/**
	 * Changes the combo box model in the variableCombo combo box when the
	 * summary measure is changed
	 * @param indexOfStatisticNames
	 * 			The index to use to obtain the correct summary measure from the array
	 * 			of summary measures
	 */
	public void changeOfButtonSelection(int indexOfStatisticNames){
		statisticSelection = statisticNames[indexOfStatisticNames];
		variableCombo.setModel(variableComboModels.get(statisticSelection));
		variableDescriptionToVarname = summaryMeasuresToVariables.get(statisticSelection);
		
		variableCombo.setSelectedIndex(0);
	}
	
	private void frequenciesSelected(){
		changeOfButtonSelection(0);
	}
	private void meansSelected(){
		changeOfButtonSelection(1);
	}
	private void quintilesSelected(){
		changeOfButtonSelection(2);
	}
	
	private void variableSelected(){
		variableSelection = variableCombo.getSelectedItem().toString();
	}

	private void subgroupSelected(){
		subgroupSelection = subgroupCombo.getSelectedItem().toString();
	}
	
	/**
	 * Creates an expression to be passed to R that stores the table built by the user
	 * so that it may be recreated when the model is reloaded or when the workspace 
	 * is loaded at another time.
	 * @return
	 * 		a String expression for the R workspace to store and use
	 */
	private String buildStoreOnLoadExpression(){
		
		String expr = "\"addLazyTableNode('tableBuilder('" + 
					  scenarioSelection + "', '" + 
					  statisticSelection + "', '" + 
					  variableDescriptionToVarname.get(variableSelection).toString() + "', '" +
					  subgroupDescriptionToVarname.get(subgroupSelection) + "')', '" +
					  variableSelection + " by " + subgroupSelection + " - " + scenarioSelection + "', " + 
					  "'nameOfParentNode', " + 
					  "'path')\"";

		return expr;
	}
	/**
	 * Checks that a scenario, summary measure, and variable at least have been selected
	 * and sets up the table pane.
	 */
	private void okPressed(){

		if(scenarioSelection != null && 
		   statisticSelection != null && 
		   variableSelection != null){
			
			if(subgroupSelection == null){
				subgroupCombo.setSelectedItem("None");
			}
			setupTablePane();
			tablePane.setVisible(true);
		}
	}
	
	/**
	 * Checks that the dataset provider exists and uses that to save the
	 * custom table to the Navigator tree.
	 * Stores an expression in the R environment that may be used to restore
	 * the table when this JAMSIM workspace is loaded at a later time
	 */
	private void savePressed(){
		if(dsProvider != null){
			
			String expr = buildStoreOnLoadExpression();
			
			try{
				rInterface.assign("expr", expr);
				rInterface.parseEvalTry("storeOnLoadExpression(expr)");
			} catch (RFaceException e){
				e.printStackTrace();
			}
			
			scape.getScapeNode().addOutputNodeFromTableBuilder(dsProvider, "User/" + 
																		   scenarioSelection + "/" + 
																		   statisticSelection);
			dsProvider = null;
		}
	}
	
	public PanelView getPanelView(){
		return pv;
	}
	public String getName() {
		return "PanelViewCreateTables";
	}
	public void panelViewAdded(Container pvFrameImp) {}
	public void frameClosed() {}	
	public void actionPerformed(ActionEvent e){}
}
