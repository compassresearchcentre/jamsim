package org.jamsim.ascape.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.ascape.util.swing.AscapeGUIUtil;
import org.jamsim.ascape.r.ScapeRInterface;
import org.omancode.r.RInterfaceException;
import org.omancode.r.RObjectTreeBuilder;

public class AnalysisMenu {

	private final ScapeRInterface scapeR;

	private AnalysisMenu(ScapeRInterface scapeR) {
		this.scapeR = scapeR;
		addMenu();
	}

	private static ScapeRInterface staticScapeR = null;

	/**
	 * SingletonHolder is loaded, and the static initializer executed, on the
	 * first execution of Singleton.getInstance() or the first access to
	 * SingletonHolder.INSTANCE, not before.
	 */
	private static final class SingletonHolder {

		/**
		 * Singleton instance, with static initializer.
		 */
		private static final AnalysisMenu INSTANCE = createSingleton();

		/**
		 * Create singleton instance using static parameters from outer class.
		 * 
		 * @return instance
		 */
		private static AnalysisMenu createSingleton() {
			try {
				return new AnalysisMenu(staticScapeR); // NOPMD
			} catch (Exception e) {
				// a static initializer cannot throw exceptions
				// but it can throw an ExceptionInInitializerError
				throw new ExceptionInInitializerError(e);
			}
		}

		/**
		 * Prevent instantiation.
		 */
		private SingletonHolder() {
		}

		/**
		 * Get singleton instance.
		 * 
		 * @return singleton instance.
		 */
		public static AnalysisMenu getInstance() {
			return SingletonHolder.INSTANCE;
		}

	}

	/**
	 * Return the singleton instance. The first time this is called the instance
	 * will be created using the supplied parameters.
	 * 
	 * @param scapeR
	 *            scape R
	 * @return an {@link AnalysisMenu} singleton instance.
	 */
	public static AnalysisMenu getInstance(ScapeRInterface scapeR) {
		AnalysisMenu.staticScapeR = scapeR;
		return SingletonHolder.getInstance();
	}

	/**
	 * Add the Analysis menu to the menu bar.
	 * 
	 * @param scape
	 *            scape
	 */
	private void addMenu() {
		JMenu rMenu = new JMenu("Analysis");

		rMenu.add(new JMenuItem(getOpenAnalysisWindowAction()));

		AscapeGUIUtil.addMenu(rMenu);

	}

	/**
	 * Action that opens an empty buffer for a new R file.
	 * 
	 * @return action
	 */
	private Action getOpenAnalysisWindowAction() {
		Action analysisWindowAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				try {
					openAnalysisWindow();
				} catch (RInterfaceException e1) {
					AscapeGUIUtil.showErrorDialog(null, e1);
				}
			}
		};
		analysisWindowAction.putValue(Action.NAME, "Analysis");
		analysisWindowAction.putValue(Action.SHORT_DESCRIPTION, "Analysis");
		return analysisWindowAction;

	}

	private void openAnalysisWindow() throws RInterfaceException {

		RObjectTreeBuilder rotb = scapeR.getRObjectTreeBuilder();

		JPanel panel = new AnalysisPanel(rotb.getTree());
		JDialog diag = new JDialog(AscapeGUIUtil.getUserFrame());
		diag.add(panel);
		diag.pack();
		diag.setVisible(true);
	}

}
