package org.jamsim.io;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import net.casper.data.model.CDataCacheContainer;
import net.casper.io.file.CBuildFromFile;
import net.casper.io.file.CDataFile;

/**
 * Dataset file loader that gets the file to load from preferences or a GUI
 * prompt. The file to load is specified by looking up the file location in the
 * preferences. If the file is not set in the preferences, or does not exist, a
 * dialog box is shown allowing the user to select the file location.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class PrefsOrPromptFileLoader implements DatasetFileLoader {

	private final Preferences prefs;

	private final FileUtil fileutil;

	private final Output output;

	/**
	 * Construct using defaults.
	 */
	public PrefsOrPromptFileLoader() {
		this(null, null);
	}

	/**
	 * Construct with specified preferences class and default output object.
	 */
	public PrefsOrPromptFileLoader(Class<?> prefsClass) {
		this(prefsClass, null);
	}

	/**
	 * Construct {@link PrefsOrPromptFileLoader} that loads preferences from the
	 * node for class {@code prefsClass} and prints loading progress to {@code
	 * output}.
	 * 
	 * @param prefsClass
	 *            preferences node, if {@code null} uses the
	 *            {@link PrefsOrPromptFileLoader} class node.
	 * @param output
	 *            output object, if {@code null} uses the default
	 *            {@link OutputToPrintStream} instance (ie: prints to
	 *            System.out).
	 */
	public PrefsOrPromptFileLoader(Class<?> prefsClass, Output output) {
		this.output = output == null ? new OutputToPrintStream() : output;
		prefs =
				Preferences.userNodeForPackage(prefsClass == null ? this
						.getClass() : prefsClass);
		fileutil = new FileUtil(prefs);
	}

	/**
	 * Supplies a File to a {@link CDataFile} and loads the dataset. The file to
	 * load is specified by looking up the file location in the preferences. If
	 * the file is not set in the preferences, or does not exist, a dialog box
	 * is shown allowing the user to select the file location.
	 * 
	 * @param cdef
	 *            {@link CDataFile} object describing the dataset to load.
	 * @throws IOException
	 *             if there is a problem loading the file.
	 * @return a casper container
	 */
	public CDataCacheContainer loadDataset(CDataFile cdef) throws IOException {

		String datasetName = cdef.getName();

		// lookup the file from the prefs, or if it doesn't exist
		// prompt the user
		File file =
				fileutil.getFileFromPrefsOrPrompt(datasetName,
						"Select file containing dataset \"" + datasetName
								+ "\"", CBuildFromFile.FileTypeFactories
								.getFilter(), false);

		// load the dataset
		output.print("Loading dataset \"" + datasetName + "\" from ["
				+ file.getPath() + "]. ");
		CDataCacheContainer cdcc = cdef.loadDataset(file);
		output.println("Done. ");

		// save location of file selected in prefs
		prefs.put(datasetName, file.getPath());

		return cdcc;
	}
}
