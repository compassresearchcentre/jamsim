package org.jamsim.ascape.ui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.ascape.util.swing.AscapeGUIUtil;
import org.jamsim.ascape.r.ScapeRInterface;
import org.omancode.r.RFaceException;

import net.casper.io.file.in.CBuildFromFile;

/**
 * Action that is used by a component to display Save and Load dialogs.
 * Depending on the action specified in the constructor it displays a 'Save
 * Workspace' or 'Load Workspace' dialog to the user when the ActionEvent is
 * fired.
 * 
 * @author bmac055
 */
@SuppressWarnings("serial")
public class SaveLoadAction extends AbstractAction {
	/**
	 * The JFileChooser to display the save/load dialog to the user
	 */
	private JFileChooser fileChooser;
	/**
	 * A description to display in a tool-tip when the cursor is in the file
	 * menu
	 */
	private String desc;
	/**
	 * The type of action required e.g. "save" or "load"
	 */
	private String action;
	/**
	 * An R interface to make R calls
	 */
	private ScapeRInterface rInterface;

	/**
	 * Creates a {@link SaveLoadAction}, sets up a tooltip for the component
	 * firing the ActionEvent. Calls a method to set up the JFileChooser,
	 * passing a String (action) describing the type of JFileChooser required -
	 * that is, a SAVE_DIALOG or an OPEN_DIALOG.
	 * 
	 * @param name
	 *            Name of the SaveLoadAction
	 * @param desc
	 *            A description to display in a tool-tip when the cursor is in
	 *            the file menu
	 * @param action
	 *            The type of action required e.g. "save" or "load"
	 * @param rInterface
	 *            An R interface to make R calls
	 */
	public SaveLoadAction(String name, String desc, String action,
			ScapeRInterface rInterface) {
		super(name);
		this.desc = desc;
		this.action = action;
		this.rInterface = rInterface;
		putValue(SHORT_DESCRIPTION, this.desc);
		setupFileChooser(action);
	}

	/**
	 * Sets up a JFileChooser with a SAVE_DIALOG or an OPEN_DIALOG depending
	 * upon the action required by the component.
	 * 
	 * @param action
	 *            The type of action required e.g. "save" or "load"
	 */
	private void setupFileChooser(String action) {
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new RDataFileFilter());
		fileChooser.setAcceptAllFileFilterUsed(false);
		if (action.equals("save")) {
			fileChooser.setDialogType(fileChooser.SAVE_DIALOG);
		} else if (action.equals("load")) {
			fileChooser.setDialogType(fileChooser.OPEN_DIALOG);
		}
	}

	/**
	 * Displays the JFileChooser dialog depending on the action required. Gets
	 * the file name and file path specified by the user in the dialog and
	 * passes them to one of two R functions (saveWorkspace or loadWorkspace).
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int returnVal = 0;
		if (action.equals("save")) {
			returnVal = fileChooser.showSaveDialog(AscapeGUIUtil
					.getDesktopEnvironment().getUserFrame());
		} else if (action.equals("load")) {
			returnVal = fileChooser.showOpenDialog(AscapeGUIUtil
					.getDesktopEnvironment().getUserFrame());
		}
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fcPath = fileChooser.getSelectedFile().getAbsolutePath();

			if (!fcPath.endsWith(".RData")) {
				fcPath += ".RData";
			}

			final String path = fcPath.replace("\\", "/");
			

			if (action.equals("save")) {

				try {
					String expr = "saveWorkspace('" + path + "')";
					System.out.println(expr);
					rInterface.eval(expr);

				} catch (RFaceException exception) {
					exception.printStackTrace();
				}
			}

			if (action.equals("load")) {

//				AscapeGUIUtil.getDesktopEnvironment().getUserFrame()
//						.getMenuView().getReopenAction().actionPerformed(e);

				// invoke on AWT thread so occurs AFTER reopen action above
				Runnable doWorkRunnable = new Runnable() {
					public void run() {
						try {
							String expr = "loadWorkspace('" + path + "')";
							System.out.println(expr);
							rInterface.eval(expr);

						} catch (RFaceException exception) {
							exception.printStackTrace();
						}
					}
				};
				SwingUtilities.invokeLater(doWorkRunnable);

			}
		}
	}

	/**
	 * A file filter used by the JFileChooser to filter out files that are not
	 * of the type RData
	 * 
	 * @author bmac055
	 * 
	 */
	class RDataFileFilter extends FileFilter {

		public RDataFileFilter() {
		}

		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

			String extension = getExtension(f);
			if (extension != null) {
				if (extension.toLowerCase().equals("rdata")) {
					return true;
				} else {
					return false;
				}
			}

			return false;
		}

		@Override
		public String getDescription() {
			return ".RData";
		}

		public String getExtension(File f) {
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');

			if (i > 0 && i < s.length() - 1) {
				ext = s.substring(i + 1).toLowerCase();
			}
			return ext;
		}
	}
}
