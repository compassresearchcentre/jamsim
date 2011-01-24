package org.jamsim.ascape.ui;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.ascape.util.swing.AscapeGUIUtil;
import org.jamsim.ascape.r.ScapeRCommand;
import org.jamsim.ascape.r.ScapeRInterface;

/**
 * Action that opens a {@link ScapeRCommandPanel} displaying the specified
 * {@link ScapeRCommand}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ScapeRCommandAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2872386207621881813L;

	private final ScapeRCommand rCmd;
	private final ScapeRInterface scapeR;

	/**
	 * Construct a {@link ScapeRCommandAction} from the {@link ScapeRCommand}.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param rCmd
	 *            scape R command
	 */
	public ScapeRCommandAction(ScapeRInterface scapeR, ScapeRCommand rCmd) {
		this.scapeR = scapeR;
		this.rCmd = rCmd;
		putValue(Action.NAME, rCmd.getName());
		putValue(Action.SHORT_DESCRIPTION, rCmd.getName());
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
		diag.setTitle(rCmd.getName());
		diag.setLocationRelativeTo(AnalysisMenu.INSTANCE.getJMenu());

		JPanel panel = new ScapeRCommandPanel(scapeR, diag, rCmd);
		diag.add(panel);
		diag.pack();
		diag.setVisible(true);
	}

}
