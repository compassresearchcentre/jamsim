package org.jamsim.io;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.ext.file.CDataFileDoubleArray;
import net.casper.io.file.CBuildFromFile;
import net.casper.io.file.CDataFile;
import net.casper.io.file.CDataFileDef;
import net.casper.io.file.CDataFileDefLoader;
import net.casper.io.file.util.ExtFileFilter;

import org.jamsim.casper.CDataFileIntervalsMap;
import org.jamsim.math.IntervalsIntMap;
import org.jamsim.math.IntervalsIntMapTableModel;
import org.jamsim.matrix.IndexedDenseDoubleMatrix2D;
import org.jamsim.matrix.IndexedMatrixTableModel;
import org.omancode.io.Output;
import org.omancode.io.OutputToPrintStream;
import org.omancode.swing.ArrayTableModel;
import org.omancode.swing.PrefsOrOpenFileChooser;
import org.omancode.swing.PrefsOrSaveFileChooser;

/**
 * File loader that loads datasets, and objects based on datasets, from files.
 * The location of files is determined by the preferences or a GUI prompt.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class FileLoader implements Output {

	private final Preferences prefs;

	private final PrefsOrOpenFileChooser openChooser;

	private final PrefsOrSaveFileChooser saveChooser;

	private final Output output;

	private final CDataFileDefLoader cdefLoader = new CDataFileDefLoader();

	/**
	 * Table model representations of datasets, matrices etc. for display in a
	 * Swing GUI.
	 */
	private final Map<String, TableModel> tmodels =
			new TreeMap<String, TableModel>();

	/**
	 * Get a map of {@link TableModel} representations of the loaded datasets,
	 * matrices etc. for display in a Swing JTable.
	 * 
	 * @return map of {@link TableModel}s.
	 */
	public Map<String, TableModel> getTableModels() {
		return tmodels;
	}

	/**
	 * Construct using defaults.
	 */
	public FileLoader() {
		this(null, null);
	}

	/**
	 * Construct with specified preferences class and default output object.
	 * 
	 * @param prefsClass
	 *            preferences node, if {@code null} uses the {@link FileLoader}
	 *            class node.
	 */
	public FileLoader(Class<?> prefsClass) {
		this(prefsClass, null);
	}

	/**
	 * Construct {@link FileLoader} that loads preferences from the node for
	 * class {@code prefsClass} and prints loading progress to {@code output}.
	 * 
	 * @param prefsClass
	 *            preferences node, if {@code null} uses the {@link FileLoader}
	 *            class node.
	 * @param output
	 *            output object, if {@code null} uses the default
	 *            {@link OutputToPrintStream} instance (ie: prints to
	 *            System.out).
	 */
	public FileLoader(Class<?> prefsClass, Output output) {
		this.output = output == null ? new OutputToPrintStream() : output;
		prefs =
				Preferences.userNodeForPackage(prefsClass == null ? this
						.getClass() : prefsClass);
		openChooser = new PrefsOrOpenFileChooser(prefs);
		saveChooser = new PrefsOrSaveFileChooser(prefs);
	}

	/**
	 * Convenience method which displays the chooser with the default dialog
	 * title {@link PrefsOrOpenFileChooser#DEFAULT_PROMPT}. The chooser has no
	 * file filters. The file selected is saved to {@code prefsKey}.
	 * 
	 * @param prefsKey
	 *            key to lookup stored filename.
	 * @return File specified in prefs, or selected in the dialog by user.
	 * @throws IOException
	 *             if no valid file is selected when prompted.
	 */
	public File getFile(String prefsKey) throws IOException {
		return openChooser.getFile(prefsKey);
	}

	/**
	 * Convenience method which displays the save chooser with the default
	 * dialog title {@link PrefsOrSaveFileChooser#DEFAULT_PROMPT}. The chooser
	 * has no file filters. The file selected is saved to {@code prefsKey}.
	 * 
	 * @param prefsKey
	 *            key to lookup stored filename.
	 * @return File specified in prefs, or selected in the dialog by user.
	 * @throws IOException
	 *             if no valid file is selected when prompted.
	 */
	public File getFileToSave(String prefsKey) throws IOException {
		return saveChooser.getFile(prefsKey);
	}

	/**
	 * Lookup the file from the prefs, or if it doesn't exist prompt the user
	 * via an open file dialog.
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
	 * @return File specified in prefs, or selected in the dialog by user.
	 * @throws IOException
	 *             if no valid file is selected when prompted.
	 */
	public File getFile(String prefsKey, String prompt, FileFilter filter,
			boolean saveFileSelectedToPrefs) throws IOException {
		return openChooser.getFile(prefsKey, prompt, filter,
				saveFileSelectedToPrefs);
	}

	/**
	 * Lookup the directory from the prefs, or if it doesn't exist prompt the
	 * user via an open file dialog.
	 * 
	 * @param prefsKey
	 *            key to lookup stored directory.
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
		return openChooser.getDirectory(prefsKey, prompt,
				saveDirSelectedToPrefs);
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
		return openChooser.showOpenDialog(dialogTitle, parent, filter);
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
		return saveChooser.showSaveDialogPromptOverwrite(dialogTitle, parent, filter);
	}

	/**
	 * Show a JFileChooser open file dialog that allows selection of directories
	 * only. Saves the directory last navigated to into the preferences.
	 * 
	 * @param dialogTitle
	 *            file chooser's dialog title
	 * @param parent
	 *            the parent component of the dialog; can be null
	 * @return selected directory, or null if cancel selected.
	 */
	public File showOpenDialogForDirectories(String dialogTitle,
			Component parent) {
		return openChooser.showOpenDialogForDirectories(dialogTitle, parent);
	}

	/**
	 * Provide the prefs to objects that want to load files themselves.
	 * 
	 * @return prefs
	 */
	public Preferences getPrefs() {
		return prefs;
	}

	/**
	 * Supplies a File to a {@link CDataFile} and loads the dataset. The file to
	 * load is specified by looking up the file location in the preferences. If
	 * the file is not set in the preferences, or does not exist, a dialog box
	 * is shown allowing the user to select the file location. During loading
	 * progress is displayed via this instance's {@link #output} object
	 * (supplied to the constructor).
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
				getFile(datasetName, "Select file containing dataset \""
						+ datasetName + "\"",
						CBuildFromFile.FileTypeFactories.getFilter(), false);

		// load the dataset
		print("Loading dataset \"" + datasetName + "\" from ["
				+ file.getPath() + "]. ");
		CDataCacheContainer cdcc = cdef.loadDataset(file);
		println("Done. ");

		// save location of file selected in prefs
		prefs.put(datasetName, file.getPath());

		return cdcc;
	}

	/**
	 * Convenience method for loading a casper dataset into a
	 * {@link IndexedDenseDoubleMatrix2D}. Dataset must be made up of all
	 * doubles.
	 * 
	 * @param cdef
	 *            dataset definition
	 * @return matrix
	 * @throws IOException
	 *             problem reading dataset, or dataset doesn't contain doubles.
	 */
	public IndexedDenseDoubleMatrix2D loadMatrix(CDataFile cdef)
			throws IOException {

		try {
			IndexedDenseDoubleMatrix2D matrix =
					new IndexedDenseDoubleMatrix2D(loadDataset(cdef));

			tmodels.put(cdef.getName(), new IndexedMatrixTableModel(matrix));
			return matrix;
		} catch (CDataGridException e) {
			throw new IOException(e);
		}

	}

	/**
	 * Loads a {@link CDataFile} from a JSON string in a text file.
	 * 
	 * @param cdefName
	 *            name of {@link CDataFile}. This is name used to look up the
	 *            prefs or prompt the user for the file location.
	 * @return the {@link CDataFile} contained in the file specified in the
	 *         prefs or selected by the user
	 * @throws IOException
	 *             if problem loading selected file.
	 */
	public CDataFileDef loadCDataFile(String cdefName) throws IOException {
		File file =
				getFile(cdefName, "Select JSON dataset definition for \""
						+ cdefName + "\"", new ExtFileFilter("txt",
						"JSON txt files"), false);

		CDataFileDef cdef = cdefLoader.fromJsonFile(file);

		// save location of file selected in prefs
		prefs.put(cdefName, file.getPath());

		return cdef;
	}

	/**
	 * Convenience method for loading a casper dataset into a
	 * {@link IntervalsIntMap}.
	 * 
	 * @param cdefmap
	 *            dataset definition
	 * @return matrix
	 * @throws IOException
	 *             problem reading dataset, or dataset columns of the wrong
	 *             type.
	 */
	public IntervalsIntMap loadIntervalsMap(CDataFileIntervalsMap cdefmap)
			throws IOException {
		loadDataset(cdefmap);

		IntervalsIntMap iimap = cdefmap.getIntervalsMap();

		tmodels.put(cdefmap.getName(), new IntervalsIntMapTableModel(iimap));

		return iimap;

	}

	/**
	 * Convenience method for loading a casper dataset into a primitive double
	 * array.
	 * 
	 * @param cdefdouble
	 *            dataset definition
	 * @return matrix
	 * @throws IOException
	 *             problem reading dataset, or dataset column is not of type
	 *             double.
	 */
	public double[] loadDoublesArray(CDataFileDoubleArray cdefdouble)
			throws IOException {
		loadDataset(cdefdouble);

		double[] array = cdefdouble.getDoublesArray();

		tmodels.put(cdefdouble.getName(), new ArrayTableModel(array));

		return cdefdouble.getDoublesArray();
	}

	@Override
	public void print(String message) {
		output.print(message);
	}

	@Override
	public void println(String message) {
		output.println(message);
	}

}
