package org.jamsim.ascape.navigator;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.ascape.runtime.swing.navigator.PeformActionOnSelectedNode;
import org.ascape.util.swing.AscapeGUIUtil;
import org.ascape.util.swing.PanelViewUtil;
import org.ascape.view.vis.PanelView;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.r.RInterfaceException;

/**
 * Tree node that displays details of an R dataframe when clicked on. For
 * clicking to work, the {@link PeformActionOnSelectedNode} listener must be
 * installed on the JTree.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class DataFrameNode extends DefaultMutableTreeNode implements
		ActionListener {

	private static final long serialVersionUID = 2742891366929184039L;
	private PanelView panelView;
	private final MicroSimScape<?> scape;
	private final String dataFrameName;
	private final Font font;

	/**
	 * Create a {@link DataFrameNode} that when clicked will display information
	 * on the R dataframe.
	 * 
	 * 
	 * @param scape
	 *            scape to associate this instance with.
	 * @param dataFrameName
	 *            name of the dataframe in R
	 * @param font
	 *            font to use in textarea that displays dataframe details
	 */
	public DataFrameNode(MicroSimScape<?> scape, String dataFrameName,
			Font font) {
		super(dataFrameName);
		this.scape = scape;
		this.dataFrameName = dataFrameName;
		this.font = font;
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
		// (i.e: has been added to the scape)
		if (panelView == null
				|| scape.getScapeListeners().indexOf(panelView) == -1) {

			panelView = createDFPanelView(dataFrameName);

			// add the panelView to the scape,
			// which will display it in the GUI
			scape.addView(panelView);

		}
	}

	/**
	 * Create a panel view with a textarea displaying the dataframe details.
	 * Dataframe details are provided by {@link #getDFDetails(String)}.
	 * 
	 * @param dataFrameName
	 * @return
	 */
	private PanelView createDFPanelView(String dataFrameName) {
		String text = getDFDetails(dataFrameName);
		Dimension desktopSize = AscapeGUIUtil.getDesktopSize(scape);
		return PanelViewUtil.createPanelView(dataFrameName, text,
				desktopSize, font);
	}

	/**
	 * Return the details of the dataframe. This is the response from executing
	 * {@code str(dataframe, max.level=1) } in the R console.
	 * 
	 * @param dataFrameName
	 *            data frame name
	 * @return details of dataframe.
	 */
	private String getDFDetails(String dataFrameName) {
		ScapeRInterface scapeR = scape.getScapeRInterface();

		try {
			return scapeR.evalCaptureOutput("str(" + dataFrameName
					+ ", max.level=1)");

		} catch (RInterfaceException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public String toString() {
		if (dataFrameName == null) {
			return super.toString();
		} else {
			return dataFrameName;
		}
	}

}
