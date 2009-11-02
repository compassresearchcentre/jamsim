package org.jamsim.io;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.casper.io.file.CBuildFromFile;
import net.casper.io.file.CDataFileDef;

/**
 * Utility class of static functions related to files.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class FileUtil {

	/**
	 * 
	 */
	private final JFileChooser OPEN_CHOOSER = new JFileChooser();
	private final JFileChooser SAVE_CHOOSER = new JFileChooser();
	private static final String OPEN_LASTDIR_KEY =
			"open dialog last directory";
	private static final String SAVE_LASTDIR_KEY =
			"save dialog last directory";

	protected Preferences prefs;

	/**
	 * Construct an instance using prefs from caller. Sets the JFileChooser
	 * current directory to the last directory saved in the caller's prefs.
	 * 
	 * @param prefs
	 *            the caller's Preferences
	 */
	public FileUtil(Preferences prefs) {
		super();
		this.prefs = prefs;
		OPEN_CHOOSER.setCurrentDirectory(new File(prefs.get(OPEN_LASTDIR_KEY,
				"")));
		SAVE_CHOOSER.setCurrentDirectory(new File(prefs.get(SAVE_LASTDIR_KEY,
				"")));

	}

	public void addOpenFileFilter(FileFilter filter) {
		OPEN_CHOOSER.addChoosableFileFilter(filter);
	}

	/**
	 * Lookup the file from the prefs, or if it doesn't exist prompt the user
	 * via a open file dialog.
	 * 
	 * @param prefsKey
	 *            key to lookup stored filename.
	 * @param prompt
	 *            prompt message to ask user to select a file.
	 * @param filter
	 *            file filter used in the select file dialog
	 * @param saveFileSelectedToPrefs
	 *            if {@code true} save the file selected to the prefs
	 * @return File specified in prefs, or selected in the dialog by user.
	 * @throws IOException
	 *             if no valid file is selected when prompted.
	 */
	public File getFileFromPrefsOrPrompt(String prefsKey, String prompt,
			FileFilter filter, boolean saveFileSelectedToPrefs)
			throws IOException {
		File file = new File(prefs.get(prefsKey, ""));

		if (!file.exists()) {
			file = showOpenDialog(prompt, null, filter);
		}

		if (file == null) {
			throw new IOException("No valid file has been specified.");
		}

		// save location of file selected in prefs
		if (saveFileSelectedToPrefs) {
			prefs.put(prefsKey, file.getPath());
		}

		return file;
	}

	/**
	 * Convenience method that prompts for files of any type and saves the
	 * result to prefs.
	 * 
	 * @param prefsKey
	 * @return file
	 * @throws IOException
	 */
	public File getSaveFileFromPrefsOrPrompt(String prefsKey)
			throws IOException {
		return getSaveFileFromPrefsOrPrompt(prefsKey,
				"Select file to save to", null, true, true);
	}

	/**
	 * Lookup the file from the prefs, or if it doesn't exist prompt the user
	 * via a open file dialog.
	 * 
	 * @param prefsKey
	 *            key to lookup stored filename.
	 * @param prompt
	 *            prompt message to ask user to select a file.
	 * @param filter
	 *            file filter used in the select file dialog
	 * @param saveFileSelectedToPrefs
	 *            if {@code true} save the file selected to the prefs
	 * @param promptForOverwrite
	 *            prompt user to overwrite if selected file exists
	 * @return File specified in prefs, or selected in the dialog by user.
	 * @throws IOException
	 *             if no valid file is selected when prompted.
	 */
	public File getSaveFileFromPrefsOrPrompt(String prefsKey, String prompt,
			FileFilter filter, boolean saveFileSelectedToPrefs,
			boolean promptForOverwrite) throws IOException {

		File file = new File(prefs.get(prefsKey, ""));

		int response = JOptionPane.CANCEL_OPTION;

		while (response != JOptionPane.YES_OPTION) {
			if (file.getParentFile() == null
					|| !file.getParentFile().exists()
					|| response == JOptionPane.NO_OPTION) {
				file = showSaveDialog(prompt, null, filter);
			}

			if (file == null) {
				throw new IOException("No valid file has been specified.");
			}

			if (file.exists() && promptForOverwrite) {
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

		// save location of file selected in prefs
		if (saveFileSelectedToPrefs) {
			prefs.put(prefsKey, file.getPath());
		}

		return file;
	}

	public File getFileFromPrefsOrPrompt(CDataFileDef defn)
			throws IOException {
		return getFileFromPrefsOrPrompt(defn.getName(),
				"Select file containing dataset \"" + defn.getName() + "\"",
				CBuildFromFile.FileTypeFactories.getFilter(), true);
	}

	public File getFileFromPrefsOrPrompt(String prefsKey) throws IOException {
		return getFileFromPrefsOrPrompt(prefsKey, "Select file to load",
				CBuildFromFile.FileTypeFactories.getFilter(), true);
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
	 *            the FileFilter to set on the FileChooser
	 * @return selected File, or null if cancel selected.
	 */
	public File showSaveDialog(String dialogTitle, Component parent,
			FileFilter filter) {
		File selectedFile = null;
		SAVE_CHOOSER.setDialogTitle(dialogTitle);
		if (filter == null) {
			SAVE_CHOOSER.setFileFilter(SAVE_CHOOSER.getAcceptAllFileFilter());
		} else {
			SAVE_CHOOSER.setFileFilter(filter);
		}

		int returnVal = SAVE_CHOOSER.showSaveDialog(parent);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = SAVE_CHOOSER.getSelectedFile();

			// save the directory last selected in the JFileChooser
			prefs.put(SAVE_LASTDIR_KEY, SAVE_CHOOSER.getCurrentDirectory()
					.getPath());
		}

		return selectedFile;
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
	 *            the FileFilter to set on the FileChooser
	 * @return selected File, or null if cancel selected.
	 */
	public File showOpenDialog(String dialogTitle, Component parent,
			FileFilter filter) {
		File selectedFile = null;
		OPEN_CHOOSER.setDialogTitle(dialogTitle);
		if (filter == null) {
			OPEN_CHOOSER.setFileFilter(OPEN_CHOOSER.getAcceptAllFileFilter());
		} else {
			OPEN_CHOOSER.setFileFilter(filter);
		}

		int returnVal = OPEN_CHOOSER.showOpenDialog(parent);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = OPEN_CHOOSER.getSelectedFile();

			// save the directory last selected in the JFileChooser
			prefs.put(OPEN_LASTDIR_KEY, OPEN_CHOOSER.getCurrentDirectory()
					.getPath());
		}

		return selectedFile;
	}

	/**
	 * A FileFilter to show .CSV files only.
	 * 
	 * @author Oliver Mannion
	 * 
	 */
	public static class CSVFileFilter extends FileFilter {

		/**
		 * CSV file extension.
		 */
		private static final String CSV = "csv";

		// Accept all directories and all CSV files.
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

			String extension = getExtension(f);
			if (extension != null) {
				return extension.equals(CSV);
			}

			return false;
		}

		// The description of this filter
		@Override
		public String getDescription() {
			return "CSV files";
		}

	}

	/**
	 * Get the extension of a file.
	 * 
	 * @param file
	 *            to get extension of.
	 * @return a String containing the file's extension.
	 */
	public static String getExtension(File file) {
		String ext = null;
		String fileName = file.getName();
		int indexOfDot = fileName.lastIndexOf('.');

		if (indexOfDot > 0 && indexOfDot < fileName.length() - 1) {
			ext = fileName.substring(indexOfDot + 1);
		}
		return ext;
	}

	/**
	 * Get the name of a file without its extension.
	 * 
	 * @param file
	 *            to get name of.
	 * @return a String containing the file's name without its extension.
	 */
	public static String getNameNoExt(File file) {
		String ext = null;
		String fileName = file.getName();
		int indexOfDot = fileName.lastIndexOf('.');

		if (indexOfDot > 0 && indexOfDot < fileName.length() - 1) {
			ext = fileName.substring(0, indexOfDot);
		}
		return ext;
	}

}
