package org.jamsim.ascape.r;

import java.awt.Container;
import java.io.IOException;

import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.ascape.runtime.Runner;
import org.ascape.runtime.swing.DesktopEnvironment;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.util.swing.PanelViewUtil;
import org.ascape.view.vis.PanelView;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.navigator.MicroSimScapeNode;
import org.omancode.r.RFaceException;
import org.rosuda.javaGD.JGDPanel;

/**
 * 
 * Executes a series of commands in R each time {@link #getPanelView()} is called.
 * Provides a {@link PanelView} with the Graphics Device resulting from the R commands as it
 * would have been output by R. 
 *
 * @author bmac055
 */

public class PanelViewJGraphicsDevice implements PanelViewProvider {
	private ScapeRInterface rInterface;
	private String rPlotCmd;
	private String nodeName;
	private PanelView pv;
	private AscapeGD ascapeGD;
	private MicroSimScape<?> scape;
	
	/**
	 * Create a {@link PanelViewJGraphicsDevice}
	 * @param rInterface
	 * 			scape R Interface
	 * @param rPlotCmd
	 * 			R command to plot the graph
	 * @param nodeName
	 * 			Name of the node
	 * @param scape
	 * 			The MicroSimScape object
	 */
	
	public PanelViewJGraphicsDevice(ScapeRInterface rInterface, String rPlotCmd, String nodeName, MicroSimScape<?> scape){
		this.rInterface = rInterface;
		this.rPlotCmd = rPlotCmd;
		this.nodeName = nodeName;
		this.scape = scape;
	}
	
	/**
	 * Delete the R graphics device corresponding to the ascapeGD object
	 */
	
	@Override
	public void frameClosed() {
		pv.removeAll();
		ascapeGD.executeDevOff();
	}

	@Override
	public String getName() {
		return nodeName;
	}

	
	public String getRPlotCmd(){
		return rPlotCmd;
	}
	
	public ScapeRInterface getRInterface(){
		return rInterface;
	}
	
	/**
	 * Makes calls to R using rInterface. The call to JavaGD in R creates the graphics device in R
	 * as well as an AscapeGD object.  The R graphics device can then have the graph written to it by calling the rPlotCmd.
	 * The AscapeGD object ascapeGD is then stored for retrieval in the MiroSimScape.
	 */
	
	@Override
	public PanelView getPanelView() {
		
		try{
			
			rInterface.eval("JavaGD()");
			rInterface.eval(rPlotCmd);
			rInterface.eval("ascapeGD <- .getJavaGDObject(dev.cur())");
			rInterface.eval(".jcall(ascapeGD, \"V\", \"storeLastCreatedAscapeGD\", ascapeGD)"); // or could add directly to MicroSimScape
	
			Object lastcreatedascapegdobject = scape.getLastCreatedAscapeGDObject();
			
			if(lastcreatedascapegdobject instanceof AscapeGD){
		
				ascapeGD = (AscapeGD) lastcreatedascapegdobject;
					
				pv = ascapeGD.getPanelView();
				pv.setName(nodeName);
			}
		
		} catch (RFaceException e) {
			System.out.println("RFaceException caught");
		}	
		
		return pv;
	}
	
	@Override
	public void panelViewAdded(Container pvFrameImp){
		// Nothing to do.
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Nothing to do.
	}

}
