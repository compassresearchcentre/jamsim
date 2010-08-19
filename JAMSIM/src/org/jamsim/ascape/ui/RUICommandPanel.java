package org.jamsim.ascape.ui;

import java.awt.Window;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JTree;

import net.casper.ext.swing.CDataRuntimeException;

import org.ascape.util.swing.AscapeGUIUtil;
import org.jamsim.ascape.output.OutputDatasetProvider;
import org.jamsim.ascape.output.ROutput;
import org.jamsim.ascape.r.ScapeRInterface;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;
import org.omancode.r.RUICommand;

/**
 * A panel that displays a {@link RUICommand}. When OK is clicked a new user
 * node is added to the navigator.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class RUICommandPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1697208275538033643L;

	private final ScapeRInterface scapeR;
	@SuppressWarnings("unused")
	private final JTree rObjects; // NOPMD
	private final Window window;
	private final RUICommand ruiCmd;
	private final BuildResult uiElements;

	/**
	 * Construct {@link RUICommandPanel}.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param window
	 *            window
	 * @param ruiCmd
	 *            RUI command
	 * @throws IOException
	 *             if problem creating panel
	 */
	public RUICommandPanel(ScapeRInterface scapeR, Window window,
			RUICommand ruiCmd) throws IOException {
		this.scapeR = scapeR;
		this.rObjects = scapeR.createRObjectTreeBuilder().getTree();
		this.window = window;
		this.ruiCmd = ruiCmd;
		this.uiElements = SwingJavaBuilder.build(this, ruiCmd.getYAML());
	}

	@SuppressWarnings("unused")
	private void ok() {
		ruiCmd.setUIElements(uiElements);
		// JOptionPane.showMessageDialog(this, rCmd);
		// System.out.println(rCmd);

		try {
			if (!ruiCmd.isChart()) {
			scapeR.getMsScape().addUserNode(convert(ruiCmd),
					ruiCmd.getName());
			}
		} catch (CDataRuntimeException e) {
			AscapeGUIUtil.showErrorDialog(null, e);
		}

		closeFrame();
	}

	private OutputDatasetProvider convert(RUICommand ruiCmd) {
		return new ROutput(ruiCmd.getVariableName(),
				ruiCmd.getVariableName(), scapeR, ruiCmd.getRCommand());
	}

	@SuppressWarnings("unused")
	private void cancel() {
		closeFrame();
	}

	private void closeFrame() {
		window.setVisible(false);
	}
}
