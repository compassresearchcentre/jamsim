package org.jamsim.ascape.ui;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.ascape.util.swing.AscapeGUIUtil;
import org.jamsim.ascape.r.ScapeRInterface;
import org.omancode.r.RUICommand;

/**
 * Action that opens a {@link RUICommandPanel} displaying the specified
 * {@link RUICommand}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class RUICommandAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2872386207621881813L;

	private final RUICommand ruiCmd;
	private final ScapeRInterface scapeR;

	/**
	 * Construct a {@link RUICommandAction} from the {@link RUICommand}.
	 * 
	 * @param scapeR scape R interface
	 * @param ruiCmd RUI command
	 */
	public RUICommandAction(ScapeRInterface scapeR, RUICommand ruiCmd) {
		this.scapeR = scapeR;
		this.ruiCmd = ruiCmd;
		putValue(Action.NAME, ruiCmd.getName());
		putValue(Action.SHORT_DESCRIPTION, ruiCmd.getName());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			openRUICommandWindow();
		} catch (IOException e1) {
			AscapeGUIUtil.showErrorDialog(null, e1);
		}
	}

	private void openRUICommandWindow() throws IOException {
		JDialog diag = new JDialog(AscapeGUIUtil.getUserFrame());
		diag.setTitle(ruiCmd.getName());
		diag.setLocationRelativeTo(AnalysisMenu.getInstance().getJMenu());

		JPanel panel = new RUICommandPanel(scapeR, diag, ruiCmd);
		diag.add(panel);
		diag.pack();
		diag.setVisible(true);
	}

}
