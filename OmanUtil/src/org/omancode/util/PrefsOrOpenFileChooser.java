package org.omancode.util;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * A Swing file chooser that checks the preferences before prompting the user to
 * select a file. Also retains the last directory selected across invocations in
 * the preferences.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class PrefsOrOpenFileChooser {

	public static final String DEFAULT_PROMPT = "Select file to load: ";

	private static final String OPEN_LASTDIR_KEY =
			"open dialog last directory";

	private final JFileChooser chooser = new JFileChooser();

	private final Preferences prefs;

	/**
	 * Construct an instance using prefs from caller. Sets the JFileChooser
	 * current directory to the last directory saved in the caller's prefs.
	 * 
	 * @param prefs
	 *            the caller's Preferences
	 */
	public PrefsOrOpenFileChooser(Preferences prefs) {
		this.prefs = prefs;
		chooser
				.setCurrentDirectory(new File(prefs.get(OPEN_LASTDIR_KEY, "")));
	}

	/**
	 * Construct an instance using the preferences for the supplied class.
	 * 
	 * @param prefsClass
	 *            class to look up prefs for. If {@code null} uses prefs for
	 *            FileUtil.class.
	 */
	public PrefsOrOpenFileChooser(Class<?> prefsClass) {
		this(
				Preferences
						.userNodeForPackage(prefsClass == null ? PrefsOrOpenFileChooser.class
								: prefsClass));
	}

	/**
	 * Convenience method which displays the chooser with the dialog title
	 * {@link #DEFAULT_PROMPT} + {@code prefsKey}. The chooser has no file
	 * filters. The file selected is saved to {@code prefsKey}.
	 * 
	 * @param prefsKey
	 *            key to lookup stored filename.
	 * @return File specified in prefs, or selected in the dialog by user.
	 * @throws IOException
	 *             if no valid file is selected when prompted.
	 */
	public File getFile(String prefsKey) throws IOException {
		return getFile(prefsKey, DEFAULT_PROMPT + prefsKey, null,
				true);
	}

	/**
	 * Lookup the file from the prefs, or if it doesn't exist prompt the user
	 * via a open file dialog.
	 * 
	 * @param prefsKey
	 *            key to lookup stored filename. If {@code null} then prefs is
	 *            not looked up and the user is prompted for the file.
	 * @param prompt
	 *            prompt message to ask user to select a file.
	 * @param filter
	 *            file filter used in the select file dialog. If {@code null},
	 *            then no filter applied and any type of file can be selected.
	 * @param saveFileSelectedToPrefs
	 *            if {@code true} save the file selected to the prefs
	 * @return File specified in prefs, or selected in the dialog by user.
	 * @throws IOException
	 *             if no valid file is selected when prompted.
	 */
	public File getFile(String prefsKey, String prompt, FileFilter filter,
			boolean saveFileSelectedToPrefs) throws IOException {
		File file = new File(prefs.get(prefsKey == null ? "" : prefsKey, ""));

		if (!file.exists()) {
			file = showOpenDialog(prompt, null, filter);
		}

		if (file == null) {
			throw new IOException("No valid file has been specified.");
		}

		// save location of file selected in prefs
		if (saveFileSelectedToPrefs && prefsKey != null) {
			prefs.put(prefsKey, file.getPath());
		}

		return file;
	}

	/**
	 * Lookup the directory from the prefs, or if it doesn't exist prompt the
	 * user via a open file dialog.
	 * 
	 * @param prefsKey
	 *            key to lookup stored directory. If {@code null} then prefs is
	 *            not looked up and the user is prompted for the directory.
	 * @param prompt
	 *            prompt message to ask user to select a directory.
	 * @param saveDirSelectedToPrefs
	 *            if {@code true} save the file selected to the prefs
	 * @return File specified in prefs, or selected in the dialog by user.
	 * @throws IOException
	 *             if no valid directory is selected when prompted.
	 */
	public File getDirectory(String prefsKey, String prompt,
			boolean saveDirSelectedToPrefs) throws IOException {
		
		int oldSelectionMode = chooser.getFileSelectionMode();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File file = getFile(prefsKey, prompt, null, saveDirSelectedToPrefs);
		chooser.setFileSelectionMode(oldSelectionMode);
		return file;
	}

	/**
	 * Show a JFileChooser open file dialog. Saves the directory last navigated
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
	public File showOpenDialog(String dialogTitle, Component parent,
			FileFilter filter) {
		File selectedFile = null;
		chooser.setDialogTitle(dialogTitle);
		if (filter == null) {
			chooser.setFileFilter(chooser.getAcceptAllFileFilter());
		} else {
			chooser.setFileFilter(filter);
		}

		int returnVal = chooser.showOpenDialog(parent);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = chooser.getSelectedFile();

			// save the directory last selected in the JFileChooser
			prefs.put(OPEN_LASTDIR_KEY, chooser.getCurrentDirectory()
					.getPath());
		}

		return selectedFile;
	}

	/**
	 * Show a JFileChooser open file dialog containing directories only. Saves
	 * the directory last navigated to into the preferences.
	 * 
	 * @param dialogTitle
	 *            file chooser's dialog title
	 * @param parent
	 *            the parent component of the dialog; can be null
	 * @return selected File, or null if cancel selected.
	 */
	public File showOpenDialogForDirectories(String dialogTitle,
			Component parent) {
		int oldSelectionMode = chooser.getFileSelectionMode();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File file = showOpenDialog(dialogTitle, parent, null);
		chooser.setFileSelectionMode(oldSelectionMode);
		return file;
	}

}