package org.jamsim.ascape;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.casper.data.model.CDataCacheContainer;
import net.casper.ext.swing.CDatasetTableModel;
import net.casper.io.file.CBuildFromFile;
import net.casper.io.file.CDataFileDef;

import org.apache.commons.lang.NotImplementedException;
import org.ascape.model.Agent;
import org.ascape.model.Scape;
import org.ascape.model.space.CollectionSpace;
import org.ascape.runtime.RuntimeEnvironment;
import org.ascape.runtime.swing.TreeModifier;
import org.ascape.view.nonvis.ConsoleOutView;
import org.jamsim.io.DatasetFileLoader;
import org.jamsim.io.FileUtil;
import org.jamsim.io.Output;
import org.jamsim.io.PrefsOrPromptFileLoader;
import org.jamsim.r.RInterfaceHL;

/**
 * A Scape with micro-simulation input/output functions including base file
 * loading, output tables, and dataset loading.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class MicroSimScape extends Scape implements TreeModifier, Output {

	/**
	 * Integer missing value constant.
	 */
	public static final int MISSING_VALUE_INTEGER =
			RInterfaceHL.MISSING_VALUE_INTEGER;

	/**
	 * Double missing value constant.
	 */
	public static final double MISSING_VALUE_DOUBLE =
			RInterfaceHL.MISSING_VALUE_DOUBLE;

	/**
	 * Serialization.
	 */
	private static final long serialVersionUID = 5534365905529673862L;

	protected Preferences prefs =
			Preferences.userNodeForPackage(this.getClass());

	protected final static String BASEFILE_KEY = "base file";

	protected FileUtil fileutil = new FileUtil(prefs);

	private final PrefsOrPromptFileLoader dsfLoader =
			new PrefsOrPromptFileLoader(this.getClass(), this);

	private ConsoleOutView console;

	private List<TreeModifier> outputTableProviders =
			new LinkedList<TreeModifier>();

	private File basefile;

	/**
	 * The scape loaded from the base file.
	 */
	private Scape baseScape;

	public FileUtil getFileutil() {
		return fileutil;
	}

	public DatasetFileLoader getDsfLoader() {
		return dsfLoader;
	}

	/**
	 * An alphabetically sorted list of all datasets loaded by this scape
	 * instance.
	 * 
	 */
	protected SortedSet<CDataCacheContainer> datasets =
			Collections
					.synchronizedSortedSet(new TreeSet<CDataCacheContainer>(
							new CContainerNameComparator()));

	/**
	 * Comparator to sort CDataCacheContainers alphabetically by their name. NB:
	 * This is not consistent with {@link CDataCacheContainer#equals(Object)}.
	 * For the purposes of returning a sorted set it doesn't appear to matter,
	 * but see {@link Comparator} for more details on the implications of this.
	 * 
	 * @author Oliver Mannion
	 * 
	 */
	public static class CContainerNameComparator implements
			Comparator<CDataCacheContainer>, Serializable {

		/**
		 * Serialization ID.
		 */
		private static final long serialVersionUID = -8030395771003560892L;

		@Override
		public int compare(CDataCacheContainer o1, CDataCacheContainer o2) {
			return o1.getCacheName().compareTo(o2.getCacheName());
		}
	}

	public MicroSimScape() {
		super();
	}

	public MicroSimScape(CollectionSpace space) {
		super(space);
	}

	public MicroSimScape(String name, Agent prototypeAgent) {
		super(name, prototypeAgent);
	}

	public MicroSimScape(CollectionSpace space, String name,
			Agent prototypeAgent) {
		super(space, name, prototypeAgent);
	}

	@Override
	public void createScape() {
		super.createScape();

		RuntimeEnvironment runtime = this.getRunner().getEnvironment();

		// set up console output and output for the DatasetLoader
		console = runtime.getConsole();
	}

	public void print(String message) {
		if (console == null) {
			throw new RuntimeException("console == null. "
					+ "createScape() must be called before print.");

		}
		console.print(message);

	}

	public void println(String message) {
		if (console == null) {
			throw new RuntimeException("console == null. "
					+ "createScape() must be called before print.");
		}
		console.println(message);
	}

	/**
	 * Sets the base scape and loads it from the base file. The location of the
	 * base file is stored in Preferences. Calls {@link #setBasefile(String)} to
	 * do the work.
	 * 
	 * @param scape
	 *            the scape to load from the base file.
	 */
	public void loadBaseScape(Scape scape) {
		baseScape = scape;
		setBasefile(prefs.get(BASEFILE_KEY, ""));
	}

	/**
	 * Expose base file as model parameter to Ascape.
	 * 
	 * @return base file
	 */
	public String getBasefile() {
		if (basefile == null) {
			return null;
		}
		return basefile.getPath();
	}

	/**
	 * Sets the base file to the specified file. The base file can be specified
	 * from the model parameters. If the base file doesn't exist, then a file
	 * chooser dialog is displayed to allow the user to select. If specified or
	 * selected, any existing agents are removed and the base file is loaded. If
	 * a base scape has not been set by a call to {@link #loadBaseScape(Scape)}
	 * then this method will exit silently.
	 * 
	 * @param bfileName
	 *            base file to load
	 */
	public void setBasefile(String bfileName) {

		if (baseScape == null) {
			// no base scape specified so exit
			// silently
			return;
		}

		// bf will be null when editing the base file text area in the
		// model parameters, before enter is pressed bf will be the
		// empty string "" when there are no saved preferences
		if (bfileName != null) {
			File newBaseFile = new File(bfileName);

			// if the new base file doesn't exist then show a file chooser
			// dialog for the user to select one
			if (!newBaseFile.exists()) {
				newBaseFile =
						fileutil.showOpenDialog("Select base file to load",
								null, new FileUtil.CSVFileFilter());
			}

			// if we have a passed in, or selected base file, then load it
			// and set the base file instance variable
			if (newBaseFile != null) {
				try {
					basefile = newBaseFile;

					// remove all existing agents (if any are present
					// from previous loads this session)
					baseScape.clear();

					print("Loading base file [" + basefile.getPath() + "]. ");
					Collection<?> col = getBaseScapeAgents(basefile);
					baseScape.addAll(col);
					println("Done. " + col.size() + " " + baseScape.getName()
							+ " created.");

					// save the base file to the prefs
					prefs.put(BASEFILE_KEY, basefile.getPath());

				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e); // NOPMD
				}
			}
		}
	}

	/**
	 * Return a collection of agents from the base file. Subclasses of
	 * MicroSimScape need to override this.
	 * 
	 * @param basefile
	 *            base file to load agents from
	 * @return collection of agents
	 * @throws IOException
	 *             if problem loading from base file
	 */
	public Collection<?> getBaseScapeAgents(File basefile) throws IOException {
		// subclasses that call loadBaseScape need to override this
		throw new NotImplementedException("Subclasses that call "
				+ "loadBaseScape need to override "
				+ "MicroSimScape.getBaseScapeAgents");
	}

	/**
	 * Supplies a File to a {@link CDataFileDef} and loads the dataset. The file
	 * to load is specified by looking up the file location in the preferences.
	 * If the file is not set in the preferences, or does not exist, a dialog
	 * box is shown allowing the user to select the file location.
	 * 
	 * @param cdef
	 *            {@link CDataFileDef} object describing the dataset to load.
	 * @throws IOException
	 *             if there is a problem loading the file.
	 * @return a casper container
	 */
	public CDataCacheContainer loadDataset(CDataFileDef cdef)
			throws IOException {

		String datasetName = cdef.getName();

		// lookup the file from the prefs, or if it doesn't exist
		// prompt the user
		File file =
				fileutil.getFileFromPrefsOrPrompt(datasetName,
						"Select file containing dataset \"" + datasetName
								+ "\"", CBuildFromFile.FileTypeFactories
								.getFilter(), false);

		// load the dataset
		print("Loading dataset \"" + datasetName + "\" from ["
				+ file.getPath() + "]. ");
		CDataCacheContainer cdcc = cdef.loadDataset(file);
		println("Done. ");
		datasets.add(cdcc);

		// save location of file selected in prefs
		prefs.put(datasetName, file.getPath());

		return cdcc;
	}

	/**
	 * Modify the Ascape Navigator tree to include nodes for datasets and output
	 * tables.
	 * 
	 * Called by {@code org.ascape.runtime.swing.Navigator.ScapeNode}
	 * constructor during the construction of the Navigator tree. This happens
	 * after {@link #createScape()}.
	 * 
	 * @param tree
	 *            the Navigator tree
	 * @param parentNode
	 *            this scape's node in the Navigator tree
	 */
	@Override
	public void modifyTree(JTree tree, DefaultMutableTreeNode parentNode) {

		tree
				.addTreeSelectionListener(new TreeUtil.TSLPeformActionOnSelectedNode());

		// Get the tree model. We will add nodes via the tree model instead
		// of directly to the tree so that all the appropriate events are fired
		DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

		// create Dataset node
		DefaultMutableTreeNode datasetsNode =
				new DefaultMutableTreeNode("Datasets");

		// get all the datasets from the dataset loader,
		// add them to a JTable and then create a PanelViewNode
		// and add that to the tree
		for (CDataCacheContainer cdcc : getDatasets()) {
			try {

				// wrap dataset in a table
				TableModel tm = new CDatasetTableModel(cdcc); // NOPMD
				JTable table = new JTable(tm); // NOPMD
				table.setName(cdcc.getCacheName());

				// add PanelViewNode to the tree
				datasetsNode.add(new TreeUtil.PanelViewNode(this, table)); // NOPMD

			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e); // NOPMD
			}
		}

		// add datasetsNode via the Tree Model
		treeModel.insertNodeInto(datasetsNode, parentNode, parentNode
				.getChildCount());

		// create the Output Tables node
		DefaultMutableTreeNode outputTablesNode =
				new DefaultMutableTreeNode("Output Tables");
		treeModel.insertNodeInto(outputTablesNode, parentNode, parentNode
				.getChildCount());

		// initialise the output table providers
		for (TreeModifier tm : outputTableProviders) {
			tm.modifyTree(tree, outputTablesNode);
		}
	}

	/**
	 * Adds a tree modifier model element under the Output Tables node. The tree
	 * modifier is actually responsible for adding itself as a node at the time
	 * that the output is generated (eg: when the scape stops).
	 * 
	 * @param tm
	 *            tree modifying model element
	 */
	public void addOutputTableNode(TreeModifier tm) {
		outputTableProviders.add(tm);
	}

	/**
	 * Get a alphabetically sorted list of all datasets loaded by this instance.
	 * 
	 * @return Sorted set of CDataCacheContainers.
	 */
	public SortedSet<CDataCacheContainer> getDatasets() {
		return datasets;
	}

}
