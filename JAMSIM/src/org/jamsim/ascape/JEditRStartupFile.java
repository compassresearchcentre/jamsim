package org.jamsim.ascape;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.ascape.model.Scape;
import org.ascape.util.swing.AscapeGUIUtil;
import org.jamsim.swing.JEditPanelView;
import org.jamsim.swing.PanelViewListener;

/**
 * Creates a {@link JEditPanelView} that contains the contents of the R startup
 * file. Supplies a menu for displaying the panel, and shortcut key (F8) for
 * executing code in the panel.
 * 
 * @author Oliver Mannion
 * @version $Revision: 64
 */
public class JEditRStartupFile {

	private JEditPanelView jeditPanel;

	private final File startUpFile;
	private final ScapeRInterface scapeR;

	public JEditRStartupFile(Scape scape, ScapeRInterface scapeR, File file) {
		this.startUpFile = file;
		this.scapeR = scapeR;
		addRMenu(scape);
		addRShortcut();
	}

	/**
	 * Add the R menu to the menu bar.
	 * 
	 * @param scape
	 *            scape
	 */
	private void addRMenu(Scape scape) {
		JMenu rMenu = new JMenu("R");

		rMenu.add(new JMenuItem(getOpenStartupFileAction()));
		rMenu.add(new JMenuItem(getSaveStartupFileAction()));
		rMenu.add(new JMenuItem(getRunStartupFileAction()));

		AscapeGUIUtil.addMenu(scape, rMenu);
	}

	/**
	 * Action that saves the contents of the buffer to the R startup file.
	 * 
	 * @return action
	 */
	private Action getSaveStartupFileAction() {
		Action openAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				try {
					saveJEditStartupFile();
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
			}
		};
		openAction.putValue(Action.NAME, "Save R startup file");
		openAction.putValue(Action.SHORT_DESCRIPTION, "Save R startup file");
		/*
		 * openAction.putValue(Action.SMALL_ICON, DesktopEnvironment
		 * .getIcon("OpenArrow"));
		 */
		return openAction;

	}

	/**
	 * Action that opens a window displaying the R startup file.
	 * 
	 * @return action
	 */
	private Action getOpenStartupFileAction() {
		Action openAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				try {
					createJEditStartupFile();
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
			}
		};
		openAction.putValue(Action.NAME, "Open R startup file");
		openAction.putValue(Action.SHORT_DESCRIPTION, "Open R startup file");
		/*
		 * openAction.putValue(Action.SMALL_ICON, DesktopEnvironment
		 * .getIcon("OpenArrow"));
		 */
		return openAction;

	}

	/**
	 * Action that runs the contents of the R startup file buffer. Will run a
	 * selection if selected, otherwise runs the whole buffer.
	 * 
	 * @return action
	 */
	private Action getRunStartupFileAction() {
		Action openAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				executeRStartupFileBuffer();
			}
		};
		openAction.putValue(Action.NAME, "Run R startup file (F8)");
		openAction.putValue(Action.SHORT_DESCRIPTION,
				"Run R startup file (F8)");
		/*
		 * openAction.putValue(Action.SMALL_ICON, DesktopEnvironment
		 * .getIcon("OpenArrow"));
		 */
		return openAction;

	}

	/**
	 * Install the F8 key pressed event listener. Calls
	 * {@link #executeRStartupFileBuffer()} when triggered.
	 */
	private void addRShortcut() {

		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(new KeyEventDispatcher() {
					@Override
					public boolean dispatchKeyEvent(final KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_F8
								&& e.getID() == KeyEvent.KEY_PRESSED) {

							executeRStartupFileBuffer();

							return true;
						}
						return false;

					}
				});

	}

	private void saveJEditStartupFile() throws IOException {
		jeditPanel.saveBuffer();
	}

	/**
	 * Create {@link JEditPanelView} that contains contents of
	 * {@link #startUpFile} and add it to the GUI.
	 * 
	 * @throws IOException
	 *             if problem reading {@link #startUpFile}.
	 */
	private void createJEditStartupFile() throws IOException {
		if (jeditPanel == null) {
			jeditPanel =
					new JEditPanelView("R startup file: "
							+ startUpFile.getCanonicalPath(), startUpFile);
			jeditPanel.addToSwingEnvironment();
			jeditPanel.setPanelViewListener(new PanelViewListener() {

				@Override
				public void panelViewClosing() {
					jeditPanel = null;
				}
			});
		}
	}

	/**
	 * Execute the contents of the R startup file buffer. If text has been
	 * selected, execute that, otherwise execute the entire contents. If the R
	 * startup file buffer hasn't been loaded, exits silently.
	 */
	private void executeRStartupFileBuffer() {
		if (jeditPanel != null) {
			// get current selection
			// if nothing selected use entire buffer
			String contents = jeditPanel.getCurrentSelection();

			if (contents == null) {
				contents = jeditPanel.getBufferContents();
			}

			scapeR.linefeed();
			scapeR.tryParseAndEvalPrintError(contents);
			scapeR.printPrompt();
		}
	}

}
