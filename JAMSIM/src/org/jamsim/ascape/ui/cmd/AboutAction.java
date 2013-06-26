package org.jamsim.ascape.ui.cmd;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.ascape.model.Scape;
import org.ascape.runtime.swing.DesktopEnvironment;

public class AboutAction extends AbstractAction {

	private final Scape scape;
	
	public AboutAction(Scape scape) {
		this.scape = scape;
		putValue(Action.NAME, "About");
		putValue(Action.SHORT_DESCRIPTION, "About this Model");
		putValue(Action.SMALL_ICON, DesktopEnvironment.getIcon("Inform"));
	}
	
    public void actionPerformed(ActionEvent e) {
        DesktopEnvironment.displayAboutDialog(scape);
    }

}
