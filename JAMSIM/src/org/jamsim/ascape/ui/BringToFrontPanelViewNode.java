package org.jamsim.ascape.ui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.ascape.runtime.swing.SwingEnvironment;
import org.ascape.runtime.swing.navigator.PanelViewNode;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.runtime.swing.navigator.PeformActionOnSelectedNode;
import org.ascape.view.vis.PanelView;

public class BringToFrontPanelViewNode extends PanelViewNode implements
		ActionListener {

	private static final long serialVersionUID = 6327681491877012320L;
	private JInternalFrame pvFrame;
	private PanelView panelView;
	private final PanelViewProvider provider;
	private final String name;

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
	public BringToFrontPanelViewNode(PanelViewProvider provider) {
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
		displayPanelView();
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
		} else {
			// bring JInternalFrame to the front and give focus
			try {
				pvFrame.setSelected(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
			
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
		pvFrame = null;
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
