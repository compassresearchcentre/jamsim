package org.jamsim.ascape.r;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.ascape.model.Scape;
import org.ascape.util.swing.AscapeGUIUtil;
import org.ascape.view.vis.PanelView;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.jamsim.ascape.ui.JEditPanelView;
import org.jamsim.ascape.ui.PanelViewListener;
import org.jamsim.io.FileLoader;
import org.omancode.util.io.ExtFileFilter;
import org.omancode.util.io.FileUtil;
import org.omancode.util.swing.MRUFiles;

/**
 * Handles the opening, display, execution and saving of R files in the GUI.
 * Creates {@link JEditPanelView}s that contain the contents of R files.
 * Supplies menu options and keyboard shortcut keys for opening, executing and
 * saving R files.
 * 
 * @author Oliver Mannion
 * @version $Revision: 64
 */
public final class RFileInterface implements PanelViewListener,
		MRUFiles.Processor {

	private static final FileFilter RFILE_FILTER = new ExtFileFilter("r");

	private static final String R_EXT = ".r";

	private static final String MRU_PREFS_KEY = "MRU";

	private static final int MRU_SIZE = 5;

	private final MRUFiles mruFiles;

	private final ScapeRInterface scapeR;

	private final FileLoader fileloader;

	private final Scape scape;

	/**
	 * Construct a {@link RFileInterface}.
	 * 
	 * @param scape
	 *            scape to add {@link JEditPanelView}s as a listener to.
	 * @param scapeR
	 *            scape R
	 * @param fileloader
	 *            file loader
	 * @throws IOException
	 *             if problem reading MRU files from prefs
	 */
	private RFileInterface(Scape scape, ScapeRInterface scapeR,
			FileLoader fileloader) throws IOException {
		this.scape = scape;
		this.scapeR = scapeR;
		this.fileloader = fileloader;
		this.mruFiles =
				new MRUFiles(this, fileloader.getPrefs(), MRU_PREFS_KEY,
						MRU_SIZE);
		addRMenu();
		setupJEditModes();
	}

	private static Scape staticScape = null;
	private static ScapeRInterface staticScapeR = null;
	private static FileLoader staticFileloader = null;

	/**
	 * SingletonHolder is loaded, and the static initializer executed, on the
	 * first execution of Singleton.getInstance() or the first access to
	 * SingletonHolder.INSTANCE, not before.
	 */
	private static final class SingletonHolder {

		/**
		 * Singleton instance, with static initializer.
		 */
		private static final RFileInterface INSTANCE = createSingleton();

		/**
		 * Create singleton instance using static parameters from outer class.
		 * 
		 * @return instance
		 */
		private static RFileInterface createSingleton() {
			try {
				return new RFileInterface(staticScape, staticScapeR, // NOPMD
						staticFileloader);
			} catch (IOException e) {
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
		public static RFileInterface getInstance() {
			return SingletonHolder.INSTANCE;
		}

	}

	/**
	 * Return the singleton instance. The first time this is called the instance
	 * will be created using the supplied parameters.
	 * 
	 * @param scape
	 *            scape to add {@link JEditPanelView}s as a listener to.
	 * @param scapeR
	 *            scape R
	 * @param fileloader
	 *            file loader
	 * @return an {@link RFileInterface} singleton instance.
	 * @throws IOException
	 *             if problem reading MRU files from prefs
	 */
	public static RFileInterface getInstance(Scape scape,
			ScapeRInterface scapeR, FileLoader fileloader) throws IOException {
		RFileInterface.staticScape = scape;
		RFileInterface.staticScapeR = scapeR;
		RFileInterface.staticFileloader = fileloader;

		try {
			return SingletonHolder.getInstance();
		} catch (ExceptionInInitializerError e) {

			// re-throw exception that occurred in the initializer
			// so our caller can deal with it
			Throwable exceptionInInit = e.getCause();
			throw new IOException(exceptionInInit); // NOPMD
		}

	}

	/**
	 * Add the R menu to the menu bar.
	 * 
	 * @param scape
	 *            scape
	 */
	private void addRMenu() {
		JMenu rMenu = new JMenu("R");
		rMenu.setMnemonic(KeyEvent.VK_R);

		rMenu.add(new JMenuItem(getNewRFileAction()));
		rMenu.add(new JMenuItem(getOpenRFileAction()));
		rMenu.add(new JMenuItem(getSaveFileAction()));
		rMenu.add(new JMenuItem(getRunAction()));
		rMenu.add(new JMenuItem(getRHelpAction()));

		rMenu.add(mruFiles.getMenu("Recent R files"));

		AscapeGUIUtil.addMenu(rMenu);

	}

	/**
	 * Install the keyboard shortcuts. Calls {@link #executeSelectedRFile()}
	 * when triggered. NB: F6 and F8 need to be installed as a global shortcut,
	 * or removed from JSplitPane if installed as a JMenuItem shortcut. See
	 * http://forums.sun.com/thread.jspa?threadID=662036
	 */
	private void addRShortcut() {

		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(new KeyEventDispatcher() {
					@Override
					public boolean dispatchKeyEvent(final KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_F8
								&& e.getID() == KeyEvent.KEY_PRESSED) {

							executeSelectedRFile();

							return true;
						}
						return false;

					}
				});

	}

	/**
	 * Add the R file mode to the list of modes.
	 */
	private void setupJEditModes() {
		Mode rmode = new Mode("r");
		rmode.setProperty("file", "r.xml");
		rmode.setProperty("filenameGlob", "*.r");
		rmode.init();
		ModeProvider.instance.addMode(rmode);
	}

	/**
	 * Action that saves the contents of the currently selected R file buffer.
	 * 
	 * @return action
	 */
	private Action getSaveFileAction() {
		Action action = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				try {
					saveSelectedRFile();
				} catch (IOException e1) {
					AscapeGUIUtil.showErrorDialog(scape, e1);
				}
			}
		};
		action.putValue(Action.NAME, "Save R file");
		action.putValue(Action.SHORT_DESCRIPTION, "Save R file");
		action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_S, ActionEvent.CTRL_MASK));

		/*
		 * openAction.putValue(Action.SMALL_ICON, DesktopEnvironment
		 * .getIcon("OpenArrow"));
		 */
		return action;

	}

	/**
	 * Action that opens an empty buffer for a new R file.
	 * 
	 * @return action
	 */
	private Action getNewRFileAction() {
		Action action = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				try {
					newRFile();
				} catch (IOException e1) {
					AscapeGUIUtil.showErrorDialog(scape, e1);
				}
			}
		};
		action.putValue(Action.NAME, "New R file");
		action.putValue(Action.SHORT_DESCRIPTION, "New R file");
		action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		return action;

	}

	private void newRFile() throws IOException {
		JEditPanelView jeditPanel = new JEditPanelView("Untitled", R_EXT);
		addJEditPanel(jeditPanel);
	}

	/**
	 * Action that opens a dialog for selecting and displaying an R file.
	 * 
	 * @return action
	 */
	private Action getOpenRFileAction() {
		Action action = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				try {
					openRFileDialog();
				} catch (IOException e1) {
					AscapeGUIUtil.showErrorDialog(scape, e1);
				}
			}
		};
		action.putValue(Action.NAME, "Open R file");
		action.putValue(Action.SHORT_DESCRIPTION, "Open R file");
		action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
		/*
		 * openAction.putValue(Action.SMALL_ICON, DesktopEnvironment
		 * .getIcon("OpenArrow"));
		 */
		return action;

	}

	/**
	 * Display a dialog allowing selection of an R file. Display the R file.
	 * 
	 * @throws IOException
	 *             if can't read selected R file.
	 */
	private void openRFileDialog() throws IOException {

		File rFile =
				fileloader
						.showOpenDialog("Select R file", null, RFILE_FILTER);

		if (rFile != null) {
			openFileInPanel(rFile);
		}

	}

	private void openFileInPanel(File file) throws IOException {
		JEditPanelView jeditPanel =
				new JEditPanelView(file.getCanonicalPath(), file);
		addJEditPanel(jeditPanel);
		mruFiles.add(file);
	}

	/**
	 * Setup listeners and add {@link JEditPanelView} to the desktop
	 * environment.
	 * 
	 * @param jeditPanel
	 *            jedit panel view
	 */
	private void addJEditPanel(JEditPanelView jeditPanel) {
		jeditPanel.setPanelViewListener(this);
		// jeditPanel.addToSwingEnvironment();
		// scape.addView(jeditPanel);
		jeditPanel.display();
	}

	/**
	 * Action that runs the contents of the selected R file buffer. Will run a
	 * buffer selection if selected, otherwise runs the whole buffer.
	 * 
	 * @return action
	 */
	private Action getRunAction() {
		Action action = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				executeSelectedRFile();
			}
		};
		String desc = "Execute R code";

		action.putValue(Action.NAME, desc);
		action.putValue(Action.SHORT_DESCRIPTION, desc);
		action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_F5, 0));

		/*
		 * openAction.putValue(Action.SMALL_ICON, DesktopEnvironment
		 * .getIcon("OpenArrow"));
		 */
		return action;

	}

	/**
	 * Action that displays the R help for the currently selected buffer text,
	 * or the help contents if nothing selected.
	 * 
	 * @return action
	 */
	private Action getRHelpAction() {
		Action action = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				executeRHelp();
			}
		};
		String description = "R Help";
		action.putValue(Action.NAME, description);
		action.putValue(Action.SHORT_DESCRIPTION, description);
		action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
		action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_F1, 0));

		return action;

	}

	/**
	 * Save the currently selected R file.
	 * 
	 * @throws IOException
	 *             if problem saving the file.
	 */
	private void saveSelectedRFile() throws IOException {
		JEditPanelView jeditPanel = getSelectedPanel();

		if (jeditPanel != null) {
			saveJEditPanel(jeditPanel);
		}
	}

	/**
	 * Save the {@link JEditPanelView}. If it's a new file, provide a
	 * "Save as..." dialog box.
	 * 
	 * @param jeditPanel
	 * @return {@code true} if file saved, {@code false} if user cancelled save.
	 * @throws IOException
	 *             if problem saving
	 */
	private boolean saveJEditPanel(JEditPanelView jeditPanel)
			throws IOException {
		boolean saved = false;

		if (jeditPanel.isNewUnsavedFile()) {
			// new file that hasn't been saved yet

			File newFile =
					fileloader.showSaveDialogPromptOverwrite("Save as..",
							null, RFILE_FILTER);
			if (newFile != null) {

				// add R file ext if no ext specified
				if ("".equals(FileUtil.getExtenstion(newFile
						.getCanonicalPath()))) {
					newFile = new File(newFile.getCanonicalFile() + R_EXT);
				}

				jeditPanel.setFile(newFile);
				jeditPanel.saveBuffer();
				mruFiles.add(newFile);
				saved = true;
			}

		} else {
			jeditPanel.saveBuffer();
			saved = true;
		}

		return saved;
	}

	/**
	 * Execute the contents of the R file buffer. If text has been selected,
	 * execute that, otherwise execute the entire contents.
	 */
	private void executeSelectedRFile() {
		JEditPanelView jeditPanel = getSelectedPanel();

		if (jeditPanel != null) {
			// get current selection
			// if nothing selected use entire buffer
			String contents = jeditPanel.getCurrentSelection();

			if (contents == null) {
				contents = jeditPanel.getBufferContents();
			}

			scapeR.linefeed();
			scapeR.parseEvalPrint(contents);
			scapeR.printPrompt();
		}
	}

	/**
	 * Return the {@link JEditPanelView} that is currently selected.
	 * 
	 * @return {@link JEditPanelView} that is selected, or {@code null} if none.
	 */
	private JEditPanelView getSelectedPanel() {
		Component content =
				AscapeGUIUtil.getDesktopEnvironment().getSelectedComponent();

		if (content instanceof JEditPanelView) {
			return (JEditPanelView) content;
		}

		return null;
	}

	private void executeRHelp() {

		JEditPanelView jeditPanel = getSelectedPanel();

		if (jeditPanel != null) {
			// get current selection
			// if nothing selected use ""
			String contents = jeditPanel.getCurrentSelection();

			if (contents == null) {
				contents = "";
			}

			scapeR.help(contents);
		}
	}

	/**
	 * When closed, checks the buffer's dirty status and prompts the user to
	 * save.
	 * 
	 * @param pv
	 *            panel view
	 * @return JInternalFrame.DO_NOTHING_ON_CLOSE if panel is to be left open.
	 *         JInternalFrame.DISPOSE_ON_CLOSE if panel is to be closed.
	 */
	@Override
	public int panelViewClosing(PanelView pv) {

		JEditPanelView jeditPanel = (JEditPanelView) pv;

		int actionOnClose = JInternalFrame.DISPOSE_ON_CLOSE;

		if (jeditPanel.isDirty()) {
			int n =
					JOptionPane
							.showConfirmDialog(null, "Save modifications to "
									+ jeditPanel.getName(), "Save changes?",
									JOptionPane.YES_NO_CANCEL_OPTION);

			if (n == JOptionPane.CANCEL_OPTION) {

				actionOnClose = JInternalFrame.DO_NOTHING_ON_CLOSE;

			} else if (n == JOptionPane.YES_OPTION) {

				try {
					boolean saved = saveJEditPanel(jeditPanel);
					if (!saved) {
						actionOnClose = JInternalFrame.DO_NOTHING_ON_CLOSE;
					}

				} catch (IOException e1) {
					AscapeGUIUtil.showErrorDialog(scape, e1);
				}
			}
		}

		return actionOnClose;

	}

	@Override
	public void processFile(File file) {
		try {
			openFileInPanel(file);
		} catch (IOException e) {
			AscapeGUIUtil.showErrorDialog(scape, e);
		}
	}
}
