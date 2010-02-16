package org.omancode.util;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * A Swing file chooser that checks the preferences before prompting the user to
 * save a file. Also retains the last directory selected across invocations in
 * the preferences.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class PrefsOrSaveFileChooser {

	public static final String DEFAULT_PROMPT = "Select file to save to";

	private static final String SAVE_LASTDIR_KEY =
			"save dialog last directory";

	private final JFileChooser chooser = new JFileChooser();

	private final Preferences prefs;

	/**
	 * Construct an instance using prefs from caller. Sets the JFileChooser
	 * current directory to the last directory saved in the caller's prefs.
	 * 
	 * @param prefs
	 *            the caller's Preferences
	 */
	public PrefsOrSaveFileChooser(Preferences prefs) {
		this.prefs = prefs;
		chooser
				.setCurrentDirectory(new File(prefs.get(SAVE_LASTDIR_KEY, "")));
	}

	/**
	 * Construct an instance using the preferences for the supplied class.
	 * 
	 * @param prefsClass
	 *            class to look up prefs for. If {@code null} uses prefs for
	 *            FileUtil.class.
	 */
	public PrefsOrSaveFileChooser(Class<?> prefsClass) {
		this(
				Preferences
						.userNodeForPackage(prefsClass == null ? PrefsOrSaveFileChooser.class
								: prefsClass));
	}

	/**
	 * Convenience method that prompts to overwrite for files of any type and
	 * saves the result to prefs. Uses the default prompt (
	 * {@link #DEFAULT_PROMPT}).
	 * 
	 * @param prefsKey
	 *            key to lookup stored filename.
	 * @return File specified in prefs, or selected in the dialog by user.
	 * @throws IOException
	 *             if no valid file is selected when prompted.
	 */
	public File getFile(String prefsKey) throws IOException {
		return getFile(prefsKey, DEFAULT_PROMPT, null, true, true);
	}

	/**
	 * Lookup the file from the prefs, or if it doesn't exist prompt the user
	 * via a save file dialog.
	 * 
	 * @param prefsKey
	 *            key to lookup stored filename.
	 * @param prompt
	 *            prompt message to ask user to select a file.
	 * @param filter
	 *            file filter used in the select file dialog. If {@code null},
	 *            then no filter applied and any type of file can be selected.
	 * @param saveFileSelectedToPrefs
	 *            if {@code true} save the file selected to the prefs
	 * @param promptForOverwrite
	 *            prompt user to overwrite if selected file exists
	 * @return File specified in prefs, or selected in the dialog by user.
	 * @throws IOException
	 *             if no valid file is selected when prompted.
	 */
	public File getFile(String prefsKey, String prompt, FileFilter filter,
			boolean saveFileSelectedToPrefs, boolean promptForOverwrite)
			throws IOException {

		File file = new File(prefs.get(prefsKey, ""));

		if (file.getParentFile() == null || !file.getParentFile().exists()) {
			// if the directory of the file specified in the prefs
			// doesn't exist we won't be able to create it, so we'll need to
			// select a new file
			file = showSaveDialogPromptOverwrite(prompt, null, filter);
			if (file == null) {
				throw new IOException("No valid file has been specified.");
			}
		}

		// save location of file selected in prefs
		if (saveFileSelectedToPrefs) {
			prefs.put(prefsKey, file.getPath());
		}

		return file;
	}

	/**
	 * Show a JFileChooser save file dialog, prompting to overwrite if the file
	 * already exists. Saves the directory last navigated to into the
	 * preferences.
	 * 
	 * @param dialogTitle
	 *            file chooser's dialog title
	 * @param parent
	 *            the parent component of the dialog; can be null
	 * @param filter
	 *            file filter used in the select file dialog. If {@code null},
	 *            then no filter applied and any type of file can be selected.
	 * @return selected File, or null if cancel selected.
	 */
	public File showSaveDialogPromptOverwrite(String dialogTitle,
			Component parent, FileFilter filter) {

		File file = null;

		int response = JOptionPane.CANCEL_OPTION;

		while (response != JOptionPane.YES_OPTION) {
			file = showSaveDialog(dialogTitle, parent, filter);

			if (file == null) {
				break;
			}

			if (file.exists()) {
				String fileExists =
						"The file " + file
								+ " exists. Do you wish to overwrite it?";
				response =
						JOptionPane.showConfirmDialog(null, fileExists,
								"Overwrite?", JOptionPane.YES_NO_OPTION);
			} else {
				response = JOptionPane.YES_OPTION;
			}
		}

		return file;

	}

	/**
	 * Show a JFileChooser save file dialog. Saves the directory last navigated
	 * to into the preferences.
	 * 
	 * @param dialogTitle
	 *            file chooser's dialog title
	 * @param parent
	 *            the parent component of the dialog; can be null
	 * @param filter
	 *            file filter used in the select file dialog. If {@code null},
	 *            then no filter applied and any type of file can be selected.
	 * @return selected File, or null if cancel selected.
	 */
	public File showSaveDialog(String dialogTitle, Component parent,
			FileFilter filter) {
		File selectedFile = null;
		chooser.setDialogTitle(dialogTitle);
		if (filter == null) {
			chooser.setFileFilter(chooser.getAcceptAllFileFilter());
		} else {
			chooser.setFileFilter(filter);
		}

		int returnVal = chooser.showSaveDialog(parent);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = chooser.getSelectedFile();

			// save the directory last selected in the JFileChooser
			prefs.put(SAVE_LASTDIR_KEY, chooser.getCurrentDirectory()
					.getPath());
		}

		return selectedFile;
	}

}