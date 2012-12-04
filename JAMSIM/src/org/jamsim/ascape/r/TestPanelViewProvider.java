package org.jamsim.ascape.r;

import java.awt.Container;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.ascape.runtime.Runner;
import org.ascape.runtime.swing.DesktopEnvironment;
import org.ascape.runtime.swing.navigator.PanelViewNode;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.util.swing.AscapeGUIUtil;
import org.ascape.util.swing.PanelViewUtil;
import org.ascape.view.vis.PanelView;
import org.jamsim.ascape.navigator.MicroSimScapeNode;
import org.jamsim.ascape.navigator.TestPanelViewNode;
import org.rosuda.javaGD.GDContainer;
import org.rosuda.javaGD.GDInterface;
import org.rosuda.javaGD.JGDPanel;

public class TestPanelViewProvider extends GDInterface implements PanelViewProvider {
	
	private static final String WINDOW_TITLE = "R Graphics";
	
	private PanelView pv;
	
	private String name = WINDOW_TITLE;
	
	private PanelViewNode node;

	private JInternalFrame pvFrame;
	
	private ScapeRInterface rinterface = ScapeRInterface.getLastMsScapeNode().getMsScape().getScapeRInterface();
	
	public void gdOpen(double w, double h){
		if (pv != null){
			gdClose();
		}
		
		if (Runner.isMultiWinEnvironment()
				|| DesktopEnvironment.getDefaultDesktop().getViewMode() != DesktopEnvironment.MDI_VIEW_MODE) {
			throw new RuntimeException(
					"Support for MultiWin environments and "
							+ "non MDI desktop environments has not been "
							+ "implemented yet.");
		}
		
		pv = PanelViewUtil.createResizablePanelView(name);
		
		//c = new JGDPanel(w, h);
		//pv.add((JGDPanel) c);
	}
	
	public ScapeRInterface getRInterface(){
		return rinterface;
	}
	
	public GDContainer getGDContainer(){
		return c;
	}
	
	public void addToNavigator(String path){
		MicroSimScapeNode scapeNode = ScapeRInterface.getLastMsScapeNode();
		node = scapeNode.addGraphNode(this, "".equals(path) ? null : path);
	}
	
	public void selectNode(){
		if (node != null){
			AscapeGUIUtil.selectNavigatorNode(node);
		}
	}
	
	@Override
	public void panelViewAdded(Container pvFrameImp){
		pvFrame = (JInternalFrame) pvFrameImp;
	}

	private JInternalFrame installFrameListener(Container pvFrameImp){
		
		if (pvFrameImp instanceof JInternalFrame) {

			JInternalFrame pvFrame = (JInternalFrame) pvFrameImp;

			// when the frame closes, call closed()
			pvFrame.addInternalFrameListener(new InternalFrameAdapter() {
				@Override
				public void internalFrameClosed(InternalFrameEvent e) {
					closed();
				}
			});

			return pvFrame;
		} else {
			throw new RuntimeException("Unknown frame type "
					+ pvFrameImp.getClass().getCanonicalName());
		}
	}
	
	public void gdClose(){
		super.gdClose();
		if(pv != null){
			c = null;
			
			if(pvFrame != null){
				pvFrame.doDefaultCloseAction();
			}
			
			pv = null;
		}
	}
	

	@Override
	public void gdActivate() {
		super.gdActivate();
		if (pv != null) {
			pv.requestFocus();
		}
	}

	@Override
	public void gdDeactivate() {
		super.gdDeactivate();
	}

	@Override
	public void gdNewPage(int devNr) { // new API: provides the device Nr.
		super.gdNewPage(devNr);
	}

	/**
	 * Set the title.
	 * 
	 * @param title
	 *            title.
	 */
	private void setTitle(String title) {
		pv.setName(title);
		if (pvFrame != null) {
			pvFrame.setTitle(title);
		}
	}

	/**
	 * Set name. Called from R after creating device.
	 * 
	 * @param name
	 *            name
	 */
	public void setName(String name) {
		this.name = name;
		setTitle(name);
	}

	
	public void setNode(DefaultMutableTreeNode n){
		if(n instanceof PanelViewNode){
			node = (PanelViewNode) n;
		}
	}
	
	public PanelViewNode getNode(){
		return node;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public PanelView getPanelView() {
		return pv;
	}

	/**
	 * Close the device in R.
	 */
	public void closed() {
		if (c != null) {
			executeDevOff();
		}
	}

	@Override
	public void frameClosed() {
		// nothing to do
	}
}