package org.jamsim.ascape.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.ascape.runtime.swing.navigator.PanelViewNode;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.jamsim.io.ParameterSet;

/**
 * Action that displays a {@link PanelViewParameterSet} when performed.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ParameterSetAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -982398520409026159L;

	private final PanelViewNode pvNode;

	/**
	 * Construct {@link ParameterSetAction} from {@link ParameterSet}.
	 * 
	 * @param pset
	 *            parameter set to display when action performed.
	 * @param name
	 *            action name
	 * @param description
	 *            action short description
	 */
	public ParameterSetAction(ParameterSet pset, String name,
			String description) {
		PanelViewProvider provider = new PanelViewParameterSet(pset);
		pvNode = new PanelViewNode(provider);

		putValue(Action.NAME, name);
		putValue(Action.SHORT_DESCRIPTION, description);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		pvNode.actionPerformed(e);
	}

}
