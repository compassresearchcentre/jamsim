package org.jamsim.ascape.r;

import java.awt.Container;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.ascape.runtime.Runner;
import org.ascape.runtime.swing.DesktopEnvironment;
import org.ascape.runtime.swing.navigator.PanelViewNode;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.util.swing.AscapeGUIUtil;
import org.ascape.util.swing.PanelViewUtil;
import org.ascape.view.vis.PanelView;
import org.jamsim.ascape.MicroSimScape;
import org.rosuda.javaGD.GDInterface;
import org.rosuda.javaGD.JGDPanel;

/**
 * An R graphics device in the Ascape Swing MDI. Make sure the environment
 * variable JAVAGD_CLASS_NAME = "org/jamsim/ascape/r/AscapeGD". To create in R,
 * make sure the package JavaGD is loaded, then execute "JavaGD()".
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class AscapeGD extends GDInterface implements PanelViewProvider {

	private static final String WINDOW_TITLE = "R Graphics";

	private PanelView pv;

	private String name = WINDOW_TITLE;

	private PanelViewNode node;

	/**
	 * The frame within the Ascape Swing MDI that holds the PanelView.
	 */
	private JInternalFrame pvFrame;

	/**
	 * Create a new graphics device.
	 * 
	 * @param w
	 *            width
	 * @param h
	 *            height
	 */
	@Override
	public void gdOpen(double w, double h) {
		if (pv != null) {
			gdClose();
		}

		// We are expecting the user to be using the Swing MDI
		// which will return a JInternalFrame
		if (Runner.isMultiWinEnvironment()
				|| DesktopEnvironment.getDefaultDesktop().getViewMode() != DesktopEnvironment.MDI_VIEW_MODE) {
			throw new RuntimeException(
					"Support for MultiWin environments and "
							+ "non MDI desktop environments has not been "
							+ "implemented yet.");
		}

		// Create a new panel view
		pv = PanelViewUtil.createResizablePanelView(WINDOW_TITLE);

		// create a Swing JGDPanel that R will draw on
		// and add it to PanelView
		c = new JGDPanel(w, h);
		pv.add((JGDPanel) c);

		// create a frame for the PanelView
		// this displays it in the Ascape MDI environment
		// NB: normally you would add a PanelView as
		// a ScapeListener to a scape, which would then
		// create an MDI frame if the ScapeListener is a
		// ComponentView. Here we bypass adding the PanelView
		// as a ScapeListener and directly add the frame.
		// SwingEnvironment.DEFAULT_ENVIRONMENT.createFrame(pv);
		// pv.build();

		// get the frame. NB: must have been added to
		// environment first!
		// Container pvFrameImp = pv.getViewFrame().getFrameImp();
		// pvFrame = installFrameListener(pvFrameImp);
	}

	/**
	 * Add this GD to the navigator. Exposed so can be called from R, after
	 * {@link #setName(String)} has been called (from which we get a node
	 * title).
	 * 
	 * @param subFolderName
	 *            of navigator subfolder under "Graphs" to create node, or empty
	 *            string to create node directly under "Graphs"
	 */
	public void addToNavigator(String subFolderName) {
		// add as node under graphs
		MicroSimScape<?> msscape = ScapeRInterface.LAST_INSTANCE.getMsScape();
		node =
				msscape.addGraphNode(this, "".equals(subFolderName) ? null
						: subFolderName);
	}

	/**
	 * Select the Navigator node for this {@link AscapeGD} if it exists,
	 * otherwise exit silently.
	 */
	public void selectNode() {
		if (node != null) {
			AscapeGUIUtil.selectNavigatorNode(node);
		}
	}

	@Override
	public void panelViewAdded(Container pvFrameImp) {
		pvFrame = (JInternalFrame) pvFrameImp;
		// pvFrame = installFrameListener(pvFrameImp);
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

	@Override
	public void gdClose() {
		super.gdClose();
		if (pv != null) {
			c = null;

			if (pvFrame != null) {
				// if frame is open on desktop, close it
				pvFrame.doDefaultCloseAction();
			}

			// pv.removeAll();
			// pv.dispose();

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