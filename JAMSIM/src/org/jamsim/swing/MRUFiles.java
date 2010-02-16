package org.jamsim.swing;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.commons.lang.StringUtils;
import org.jamsim.util.MRUSet;

/**
 * Provide a menu of MRU files and loading/saving the file list from
 * {@link Preferences}.
 * 
 * @author Oliver Mannion
 * @version $Revision: 64
 */
public class MRUFiles implements ListDataListener {

	private final Preferences prefs;

	private final String mruPrefsKey;

	private final MRUSet<Action> mruSet;

	private final Processor processor;

	/**
	 * Constructor. Builds the MRU set from the filenames string specified in
	 * the preferences. Filenames in the string that do not exist are silently
	 * ignored.
	 * 
	 * @param processor
	 *            action to execute on the file when its menu item is selected
	 * @param prefs
	 *            prefs
	 * @param mruPrefsKey
	 *            key that holds the MRU string
	 * @param mruSize
	 *            size of the MRU set
	 * @throws IOException
	 *             if problem with filenames in the MRU string
	 */
	public MRUFiles(Processor processor, Preferences prefs,
			String mruPrefsKey, int mruSize) throws IOException {
		this.processor = processor;
		this.prefs = prefs;
		this.mruPrefsKey = mruPrefsKey;
		this.mruSet = createMRUSet(prefs, mruPrefsKey, mruSize);
	}

	/**
	 * Load an {@link MRUSet} of {@link FileAction}s from the prefs, or create a
	 * new empty set of prefs are empty.
	 * 
	 * @param prefs
	 *            preferences
	 * @return mru set
	 * @throws IOException
	 *             if problem creating mru set
	 */
	private MRUSet<Action> createMRUSet(Preferences prefs,
			String mruPrefsKey, int mruSize) throws IOException {
		String mruFileNames = prefs.get(mruPrefsKey, "");
		MRUSet<Action> set = new MRUSet<Action>(mruSize);

		if (mruFileNames.length() > 0) {
			fillFromString(set, mruFileNames);
		}

		set.addListDataListener(this);
		return set;
	}

	/**
	 * Add a file.
	 * 
	 * @param file
	 *            file to add
	 * @throws IOException
	 *             problem reading file name
	 */
	public void add(File file) throws IOException {
		mruSet.add(new FileAction(file));
	}

	/**
	 * Create a {@link MRUSet} of {@link FileAction}s from a string
	 * representation of file names. Filenames in the string that do not exist
	 * are silently ignored.
	 * 
	 * @param set
	 *            {@link MRUSet} to fill
	 * @param mruFileNames
	 *            string of file names separated by {@link MRUSet#SEPARATOR}
	 * @return the passed in {@link MRUSet}.
	 * @throws IOException
	 *             if problem creating {@link FileAction} from file names.
	 */
	private MRUSet<Action> fillFromString(MRUSet<Action> set,
			String mruFileNames) throws IOException {
		String[] fileNames =
				StringUtils.split(mruFileNames, MRUSet.SEPARATOR);

		for (String name : fileNames) {
			String fileName = name.trim();

			if (fileName.length() > 0) {
				File file = new File(fileName);

				if (file.exists()) {
					set.add(new FileAction(file));
				}
			}
		}

		return mruSet;
	}

	/**
	 * Supplier of a function to execute on afile when its menu item is
	 * selected.
	 */
	public interface Processor {

		/**
		 * Process the file.
		 * 
		 * @param file
		 *            file
		 */
		void processFile(File file);
	}

	/**
	 * Action that calls the {@link Processor} to do something with its
	 * specified file.
	 */
	private class FileAction extends AbstractAction {

		private final File file;
		private final String filePath;

		public FileAction(File file) throws IOException {
			this.file = file;
			this.filePath = file.getCanonicalPath();

			putValue(Action.NAME, filePath);
			putValue(Action.SHORT_DESCRIPTION, filePath);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			processor.processFile(file);
		}

		@Override
		public String toString() {
			return filePath;
		}

		@Override
		public int hashCode() {
			return filePath.hashCode();
		}

		/**
		 * Compare this to another object. Equals if the other object is a
		 * {@link FileAction} object containing a file object with the same file
		 * path.
		 */
		@Override
		public boolean equals(Object obj) {

			if (obj instanceof FileAction) {
				return filePath.equals(((FileAction) obj).filePath);
			}
			return false;
		}
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		saveMRU();
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		saveMRU();
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		saveMRU();
	}

	/**
	 * Save {@link MRUSet} to the preferences.
	 */
	private void saveMRU() {
		String mruString = mruSet.toReverseString();
		prefs.put(mruPrefsKey, mruString);
	}

	/**
	 * Provide menu of MRU files.
	 * 
	 * @param title
	 *            menu title
	 * @return MRU menu
	 */
	public JMenu getMenu(String title) {
		return new MRUActionMenu(title, mruSet);
	}

}
