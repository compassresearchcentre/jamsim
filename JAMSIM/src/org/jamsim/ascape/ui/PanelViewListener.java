package org.jamsim.ascape.ui;

import javax.swing.JInternalFrame;

import org.ascape.view.vis.PanelView;

/**
 * A listener of {@link PanelView} events.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface PanelViewListener {

	/**
	 * Invoked when a {@link PanelView} is about to close.
	 * 
	 * @param pv panelView that is closing
	 * @return a return value that can be passed to the {@link JInternalFrame}.
	 * The possible choices are:
     * <p>
     * <dl>
     * <dt><code>DO_NOTHING_ON_CLOSE</code> 
     * <dd> Do nothing.
     *      This requires the program to handle the operation
     *      in the <code>windowClosing</code> method
     *      of a registered <code>InternalFrameListener</code> object.
     * <dt><code>HIDE_ON_CLOSE</code>
     * <dd> Automatically make the internal frame invisible.
     * <dt><code>DISPOSE_ON_CLOSE</code>
     * <dd> Automatically dispose of the internal frame.
     * </dl>
     * <p>
     * The default value is <code>DISPOSE_ON_CLOSE</code>.
	 */ 
	int panelViewClosing(PanelView pv);
}
