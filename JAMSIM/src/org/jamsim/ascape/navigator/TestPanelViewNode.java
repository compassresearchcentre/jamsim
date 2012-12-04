package org.jamsim.ascape.navigator;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JInternalFrame;
import javax.swing.JTextField;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.ascape.runtime.swing.SwingEnvironment;
import org.ascape.runtime.swing.navigator.PanelViewNode;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.runtime.swing.navigator.PeformActionOnSelectedNode;
import org.ascape.view.vis.PanelView;
import org.jamsim.ascape.r.AscapeGD;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.ascape.r.TestPanelViewProvider;
import org.omancode.r.RFaceException;
import org.rosuda.javaGD.GDContainer;
import org.rosuda.javaGD.JGDPanel;

/**
 * Tree node that displays a {@link PanelView} when clicked on. For clicking to
 * work, the {@link PeformActionOnSelectedNode} listener must be installed on
 * the JTree.
 * 
 * @author Oliver Mannion
 * @version $Revision: 302 $
 */
public class TestPanelViewNode extends DefaultMutableTreeNode implements
		ActionListener {

	private static final long serialVersionUID = 6327681491877012320L;
	private JInternalFrame pvFrame;
	private PanelView panelView;
	private final TestPanelViewProvider provider;
	private final String name;
	private ScapeRInterface rInterface;
	private AscapeGD ascapeGD;

	/**
	 * Create a {@link PanelViewNode} that when clicked will display the
	 * {@link PanelView} supplied by a {@link PanelViewProvider}.
	 * 
	 * @param scape
	 *            scape to associate this instance with.
	 * @param provider
	 *            {@link PanelViewProvider} that provides the {@link PanelView}
	 *            to be displayed when this node is clicked.
	 */
	public TestPanelViewNode(TestPanelViewProvider provider) {
		super(provider);
		this.name = provider.getName();
		this.provider = provider;
	}

	/**
	 * Called by
	 * {@link PeformActionOnSelectedNode#valueChanged(TreeSelectionEvent)} when
	 * this node is selected.
	 * 
	 * @param e
	 *            event
	 */
	public void actionPerformed(ActionEvent e) {
		setupPanelView();
		displayPanelView();
	}

	public void setupPanelView(){
		
		rInterface = provider.getRInterface();
		
		try{
					
			rInterface.eval("JavaGD()");
			rInterface.eval("plot.new()");
			rInterface.eval("plot.window(xlim=c(0,1), ylim=c(5,10))");
			rInterface.eval("abline(a=6, b=3)");
			rInterface.eval("ascapeGD <- .getJavaGDObject(dev.cur())");
			rInterface.eval(".jcall(ascapeGD, \"V\", \"storeLastCreatedAscapeGD\", ascapeGD)");
			
			Object lastcreatedascapegdobject = ScapeRInterface.getLastMsScapeNode().getMsScape().getLastCreatedAscapeGDObject();
			
			if(lastcreatedascapegdobject !=null && lastcreatedascapegdobject instanceof AscapeGD){
		
				ascapeGD = (AscapeGD) lastcreatedascapegdobject;
					
				JGDPanel gdPanel = (JGDPanel) ascapeGD.c;
			
				provider.getPanelView().add(gdPanel);	
				
			}
		
		} catch (RFaceException e) {
			System.out.println("RFaceException caught");
		}	
	
	}
	
	/**
	 * Display the PanelView associated with this node.
	 */
	public void displayPanelView() {

		// check to see that panelView is not currently displayed
		if (pvFrame == null) {

			if (panelView == null) {
				panelView = provider.getPanelView();
			}

			// make panel visible
			panelView.setVisible(true);

			// tell panel to update it's graphics. if it has
			// been closed during iterations then it will need
			// to update itself now that we have opened it
			panelView.updateScapeGraphics();
			panelView.repaint();

			// create a frame for the PanelView
			// this displays it in the Ascape MDI environment
			// NB: normally you would add a PanelView as
			// a ScapeListener to a scape, which would then
			// create an MDI frame if the ScapeListener is a
			// ComponentView. Here we bypass adding the PanelView
			// as a ScapeListener and directly add the frame.
			SwingEnvironment.DEFAULT_ENVIRONMENT.createFrame(panelView);

			// install frame listener
			Container pvFrameImp = panelView.getViewFrame().getFrameImp();
			pvFrame = installFrameListener(pvFrameImp);

			// don't remove the scape listener when the ViewFrameBridge
			// internal frame is closed. important so that closed
			// chart views can still function.
			panelView.getViewFrame().setRemoveListenerOnDispose(false);

			// call added event on provider
			provider.panelViewAdded(pvFrameImp);
		}
	}

	private JInternalFrame installFrameListener(Container pvFrameImp) {

		// The pvFrameImp type will depend on what is selected by
		// the ViewFrameBridge.selectFrameImp method.
		// We are expecting the user to be using the Swing MDI
		// which will return a JInternalFrame
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

	private void closed() {
		
		ascapeGD.executeDevOff();
		pvFrame = null;
		provider.getPanelView().removeAll();
		provider.frameClosed();
		panelView = null;
	}

	@Override
	public String toString() {
		if (name == null) {
			return super.toString();
		} else {
			return name;
		}
	}

}
