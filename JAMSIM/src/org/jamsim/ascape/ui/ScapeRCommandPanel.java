package org.jamsim.ascape.ui;

import java.awt.Window;
import java.io.IOException;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTree;

import net.casper.ext.swing.CDataRuntimeException;

import org.ascape.util.swing.AscapeGUIUtil;
import org.jamsim.ascape.r.ScapeRCommand;
import org.jamsim.ascape.r.ScapeRInterface;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;
import org.omancode.r.RInterfaceException;
import org.omancode.r.RObjectNode;

/**
 * A panel that displays a {@link ScapeRCommand}. When OK is clicked a new user
 * node is added to the navigator.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ScapeRCommandPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1697208275538033643L;

	private final ScapeRInterface scapeR;
	@SuppressWarnings("unused")
	private final JTree rObjects; // NOPMD
	private final Window window;
	private final ScapeRCommand rCmd;
	private final BuildResult uiElements;

	/**
	 * Construct {@link ScapeRCommandPanel}.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param window
	 *            window
	 * @param rCmd
	 *            RUI command
	 * @throws IOException
	 *             if problem creating panel
	 */
	public ScapeRCommandPanel(ScapeRInterface scapeR, Window window,
			ScapeRCommand rCmd) throws IOException {
		this.scapeR = scapeR;
		this.rObjects = scapeR.createRObjectTreeBuilder().getTree();
		this.window = window;
		this.rCmd = rCmd;
		this.uiElements = SwingJavaBuilder.build(this, rCmd.getYAML());
	}

	@SuppressWarnings("unused")
	private void ok() {
		try {
			if (rCmd.isChart()) {
				scapeR.parseEvalTry(rCmd.generateCmdText(uiElements));
			} else {
				scapeR.getMsScape().addUserNode(
						rCmd.generateROutput(uiElements), rCmd.getName());
			}
		} catch (CDataRuntimeException e) {
			AscapeGUIUtil.showErrorDialog(null, e);
		} catch (RInterfaceException e) {
			AscapeGUIUtil.showErrorDialog(null, e);
		}

		closeFrame();
	}

	@SuppressWarnings("unused")
	private void cancel() {
		closeFrame();
	}

	private void closeFrame() {
		window.setVisible(false);
	}
	
	public static String getSelectedNodeName(Map<String, Object> uiElements) {
		JTree rObjects = (JTree) uiElements.get("rObjects");
		RObjectNode node =
				(RObjectNode) rObjects.getLastSelectedPathComponent();
		return node.getName();
	}

}