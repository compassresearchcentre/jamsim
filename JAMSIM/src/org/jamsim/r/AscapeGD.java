package org.jamsim.r;

import java.awt.Container;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.ascape.runtime.Runner;
import org.ascape.runtime.swing.DesktopEnvironment;
import org.ascape.runtime.swing.SwingEnvironment;
import org.ascape.util.swing.PanelViewUtil;
import org.ascape.view.vis.PanelView;
import org.rosuda.javaGD.GDInterface;
import org.rosuda.javaGD.JGDPanel;

/**
 * An R graphics device in the Ascape Swing MDI. Make sure the environment
 * variable JAVAGD_CLASS_NAME = "org/jamsim/r/AscapeGD". To create in R,
 * make sure the package JavaGD is loaded, then execute "JavaGD()".
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class AscapeGD extends GDInterface {

	private static final String WINDOW_TITLE = "R Graphics";

	private PanelView pv;

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
		SwingEnvironment.DEFAULT_ENVIRONMENT.createFrame(pv);
		//pv.build();

		// get the frame
		Container pvFrameImp = pv.getViewFrame().getFrameImp();

		// The pvFrameImp type will depend on what is selected by
		// the ViewFrameBridge.selectFrameImp method.
		// We are expecting the user to be using the Swing MDI
		// which will return a JInternalFrame
		if (pvFrameImp instanceof JInternalFrame) {

			pvFrame = (JInternalFrame) pvFrameImp;

			// when the frame closes, call closed()
			pvFrame.addInternalFrameListener(new InternalFrameAdapter() {
				@Override
				public void internalFrameClosed(InternalFrameEvent e) {
					closed();
				}
			});

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

			pvFrame.doDefaultCloseAction();

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
			setTitle(WINDOW_TITLE
					+ " "
					+ (getDeviceNumber() > 0 ? "(" + (getDeviceNumber() + 1)
							+ ")" : "") + " *active*");
		}
	}

	@Override
	public void gdDeactivate() {
		super.gdDeactivate();
		if (pv != null) {
			setTitle(WINDOW_TITLE
					+ " "
					+ (getDeviceNumber() > 0 ? "(" + (getDeviceNumber() + 1)
							+ ")" : ""));
		}
	}

	@Override
	public void gdNewPage(int devNr) { // new API: provides the device Nr.
		super.gdNewPage(devNr);
		if (pv != null) {
			setTitle(WINDOW_TITLE + " (" + (devNr + 1) + ")"
					+ (active ? " *active*" : ""));
		}
	}

	/**
	 * Set the title.
	 * 
	 * @param title
	 *            title.
	 */
	public void setTitle(String title) {
		pv.setName(title);
		pvFrame.setTitle(title);
	}

	/**
	 * Called when the frame is closed by the user clicking on the close button.
	 */
	public void closed() {
		if (c != null) {
			executeDevOff();
		}
	}
}