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

public class PanelViewTableBuilder implements PanelViewProvider, ActionListener{

	private MicroSimScape scape;
	
	//private JFrame saveFrame; 
	private PanelView pv;
		
	private Map<String, Map<String, String>> tableBuilderData;
	
	private Map<String, String> scenarioDescriptionToVarname;
	private Map<String, String> variableDescriptionToVarname;
	private Map<String, String> subgroupDescriptionToVarname;
	
	private String scenarioSelection;
	private String statisticSelection;
	private String variableSelection;
	private String subgroupSelection;
	
	private String[] scenarioNames;
	private String[] statisticNames;
	private String[] variableNames;
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
	
	public PanelViewTableBuilder(
			Map<String, Map<String, String>> tableBuilderData,
			MicroSimScape scape){
		
		this.scape = scape;
		rInterface = scape.getScapeRInterface();
		
		this.tableBuilderData = tableBuilderData;
		
		subgroupDescriptionToVarname = tableBuilderData.get("Subgroups");

		statisticNames = tableBuilderData.keySet().toArray(new String[tableBuilderData.size()]);
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
		setupVariableComboModels();
		subgroupCombo = new JComboBox(subgroupNames);
		
		pv = PanelViewUtil.createResizablePanelView("Table Builder");
		pv.setPreferredSize(new Dimension(600,450));
		
		BuildResult uiElements = SwingJavaBuilder.build(this);
		pv.add((Component) uiElements.get("pane"));
	}
	
	/*public JFrame getSaveFrame(){
		return saveFrame;
	}*/
	
	private void setupVariableComboModels(){
		for(int i = 0; i < statisticNames.length - 1; i++){
			variableComboModels.put(statisticNames[i], new DefaultComboBoxModel(tableBuilderData.get(statisticNames[i])
																								.keySet()
																								.toArray()));			
		}
	}
	
	private void setupScenarioToRExpression(){
		
		scenarioDescriptionToVarname = new LinkedHashMap<String, String>();
		scenarioDescriptionToVarname.put("Base", "Base");
		try{
			REXP rexp = scape.getScapeRInterface().eval("as.list(names(envs))");
			
			List<REXPString> list = rexp.asList();
			
			for(REXPString o: list){
				scenarioDescriptionToVarname.put(o.asString(),  o.asString());
			}
			
			scenarioCombo = new JComboBox(scenarioDescriptionToVarname.keySet().toArray());
			scenarioCombo.setSelectedItem("Base");
			scenarioSelected();

		}catch(RFaceException | REXPMismatchException e){
			e.printStackTrace();
		}
	}
	
	private void setupTablePane(String expr){
		
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

			
		}catch(IOException | CDataGridException e){
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
	
	public void changeOfButtonSelection(int indexOfStatisticNames){
		statisticSelection = statisticNames[indexOfStatisticNames];
		variableCombo.setModel(variableComboModels.get(statisticSelection));
		variableDescriptionToVarname = tableBuilderData.get(statisticSelection);
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

	private void okPressed(){
		// temporary test for Output Tables Node
		for(int i = 0; i < scape.getScapeNode().getOutputTablesNode().getChildCount(); i++){
			TreeNode child = scape.getScapeNode().getOutputTablesNode().getChildAt(i);
			System.out.println(child.toString());
		}
			
		if(scenarioSelection != null && 
		   statisticSelection != null && 
		   variableSelection != null){
			
			if(subgroupSelection == null){
				subgroupCombo.setSelectedItem("None");
			}
			
			setupTablePane(buildStoreOnLoadExpression());
			tablePane.setVisible(true);
		}
	}
	
	private void savePressed(){
		if(dsProvider != null){
			
			String expr = buildStoreOnLoadExpression();
			
			try{
				rInterface.assign("expr", expr);
				rInterface.parseEvalTry("storeOnLoadExpression(expr)");
			} catch (RFaceException e){
				e.printStackTrace();
			}
			
			scape.getScapeNode().addOutputNodeFromTableBuilder(dsProvider, scenarioSelection + "/" + statisticSelection);
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
