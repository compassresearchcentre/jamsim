package org.jamsim.ascape.r;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;

import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.util.swing.AscapeGUIUtil;
import org.ascape.util.swing.PanelViewUtil;
import org.ascape.view.vis.PanelView;
import org.omancode.r.RInterfaceException;

/**
 * Executes an command in R each time {@link #getPanelView()} is called.
 * Provides a {@link PanelView} with the text results of the R command as it
 * would have been output to the R console.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class PanelViewRTextCommand implements PanelViewProvider {

	/**
	 * Default font for text area text : Monospaced plain 12 pt.
	 */
	public static final Font DEFAULT_FONT =
			new Font("Monospaced", Font.PLAIN, 12);

	private static final long serialVersionUID = 2742891366929184039L;
	private final ScapeRInterface scapeR;
	private final String rcmd;
	private final String nodeName;
	private final Font font;

	/**
	 * Create a {@link PanelViewRTextCommand} with default font.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param nodeName
	 *            name of node
	 * @param rcmd
	 *            R command to execute
	 */
	public PanelViewRTextCommand(ScapeRInterface scapeR, String nodeName,
			String rcmd) {
		this(scapeR, nodeName, rcmd, null);
	}

	/**
	 * Create a {@link PanelViewRTextCommand} that will execute an R command and
	 * return the result in a {@link PanelView}.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param nodeName
	 *            name of node
	 * @param rcmd
	 *            R command to execute
	 * @param font
	 *            font to use in textarea that displays dataframe details. If
	 *            {@code null} uses {@link #DEFAULT_FONT}.
	 */
	public PanelViewRTextCommand(ScapeRInterface scapeR, String nodeName,
			String rcmd, Font font) {
		this.scapeR = scapeR;
		this.nodeName = nodeName;
		this.rcmd = rcmd;
		this.font = (font == null) ? DEFAULT_FONT : font;
	}

	/**
	 * Execute {@code rcmd} then create a panel view with a textarea displaying
	 * the output. Rcmd output is provided by {@link #getRText(String)}.
	 * 
	 * @param rcmd
	 *            R command to execute
	 * @return panel view
	 */
	private PanelView createPanelView(String name, String rcmd) {
		String text = getRText(rcmd);
		Dimension desktopSize = AscapeGUIUtil.getDesktopSize();
		return PanelViewUtil.createPanelView(name, text, desktopSize, font);
	}

	/**
	 * Return the console output of an R command.
	 * 
	 * @param rcmd
	 *            R command to execute an retrieve console output from
	 * @return console output of {@code rcmd}
	 */
	private String getRText(String rcmd) {
		try {
			return scapeR.evalCaptureOutput(rcmd);

		} catch (RInterfaceException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public String getName() {
		return nodeName;
	}

	@Override
	public PanelView getPanelView() {
		return createPanelView(nodeName, rcmd);
	}

	@Override
	public void panelViewAdded(Container pvFrameImp) {
		// nothing to do
	}

	@Override
	public void frameClosed() {
		// nothing to do
	}

}
