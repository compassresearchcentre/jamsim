package org.jamsim.ascape.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.ascape.runtime.swing.navigator.PanelViewNode;
import org.ascape.runtime.swing.navigator.PanelViewProvider;

/**
 * Action that displays a {@link org.ascape.view.vis.PanelView} when performed.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class PanelViewAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -982398520409026159L;

	private final PanelViewNode pvNode;

	/**
	 * Construct {@link PanelViewAction} from a {@link PanelViewProvider}.
	 * 
	 * @param provider
	 *            provider of panel view when action performed.
	 * @param name
	 *            action name
	 * @param description
	 *            action short description
	 */
	public PanelViewAction(PanelViewProvider provider, String name,
			String description) {

		pvNode = new PanelViewNode(provider);

		putValue(Action.NAME, name);
		putValue(Action.SHORT_DESCRIPTION, description);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		pvNode.actionPerformed(e);
	}

}
