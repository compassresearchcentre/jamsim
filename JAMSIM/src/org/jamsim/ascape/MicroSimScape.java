package org.jamsim.ascape;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.casper.io.file.util.ExtFileFilter;

import org.ascape.model.Agent;
import org.ascape.model.Scape;
import org.ascape.model.space.CollectionSpace;
import org.ascape.runtime.RuntimeEnvironment;
import org.ascape.runtime.swing.DesktopEnvironment;
import org.ascape.runtime.swing.TreeModifier;
import org.jamsim.io.FileLoader;
import org.jamsim.matrix.IndexedMatrixTableModel;
import org.jamsim.r.RInterfaceHL;
import org.jamsim.swing.DoubleCellRenderer;

import com.bbn.openmap.layer.util.html.TableCellElement;

/**
 * A Scape with micro-simulation input/output functions including base file
 * loading, and output tables and external / global data tables that appear in
 * the Ascape Navigator.
 * 
 * @param <D>
 *            a scape data class that defines data external to the scape for use
 *            by agents, and for loading agents.
 * @author Oliver Mannion
 * @version $Revision$
 */
public class MicroSimScape<D extends ScapeData> extends Scape implements
		TreeModifier {

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

	/**
	 * The base file is kept as an instance variable and exposed as a model
	 * parameter.
	 */
	private File basefile = null;

	private static final String BASEFILE_KEY = "base file";

	/**
	 * Used to help load the base file.
	 */
	private final FileLoader loader;

	/**
	 * Prefs for loading/saving location of base file.
	 */
	private final Preferences prefs;

	private final List<TreeModifier> outputTableProviders =
			new LinkedList<TreeModifier>();

	/**
	 * Global data external to scape. Used by agents of this scape and used to
	 * load these agents.
	 */
	private final D scapeData;

	/**
	 * Return the {@link ScapeData} object. This method allows agents to access
	 * the external/global scape data.
	 * 
	 * @return scape data
	 */
	public D getScapeData() {
		return scapeData;
	}

	/**
	 * Creates a {@link MicroSimScape} and loads its agents from a base file.
	 * The location of the base file is stored in Preferences. If such a
	 * location does not exist, then the user is prompted for the base file
	 * location.
	 * 
	 * @param name
	 *            name of the scape. This will be the name of the dataframe if &
	 *            when created in R.
	 * @param prototypeAgent
	 *            the prototype agent so the navigator knows to show fields that
	 *            have getter/setter methods.
	 * @param loader
	 *            file loader object which provides preferences, an select file
	 *            dialog, and output services
	 * @param scapeData
	 *            a class that specifies data external to the scape and
	 *            available for global access by agents via
	 *            {@link #getScapeData()}. This class also specifies how base
	 *            agents are loaded (from file, database etc.)
	 */
	public MicroSimScape(CollectionSpace space, String name,
			Agent prototypeAgent, FileLoader loader, D scapeData) {
		super(space);
		this.loader = loader;
		this.prefs = loader.getPrefs();
		this.scapeData = scapeData;

		// set prototype agent after setting scapeData
		// in case the agent wants to make use of scapeData
		setPrototypeAgent(prototypeAgent);

		setName(name);

		// load the patient scape with patient agents
		loadBaseScape();

		// tell the patients scape not to auto create, otherwise
		// it will remove the agents we've added to it and
		// replace them with clones with parameter values of 0
		this.setAutoCreate(false);
	}

	/**
	 * Loads this scape with agents from a base file. The location of the base
	 * file is stored in Preferences. If such a location does not exist, then
	 * the user is prompted for the base file location. Calls
	 * {@link #setBasefile(String)} to do the work.
	 */
	private void loadBaseScape() {
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
	 * @throws IOException
	 */
	public final void setBasefile(String bfileName) {

		// bfileName will be null when editing the base file text area in the
		// model parameters, before enter is pressed
		// bfileName will be the empty string "" when there are no saved
		// preferences
		if (bfileName != null) {
			File newBaseFile = new File(bfileName);

			// if the new base file doesn't exist then show a file chooser
			// dialog for the user to select one
			if (!newBaseFile.exists()) {
				newBaseFile =
						loader.showOpenDialog("Select base file to load",
								null, new ExtFileFilter("csv", "CSV files"));
			}

			// if we have a passed in, or selected base file, then load it
			// and set the base file instance variable
			if (newBaseFile != null) {
				try {
					basefile = newBaseFile;

					// remove all existing agents (if any are present
					// from previous loads this session)
					clear();

					print("Loading base file [" + basefile.getPath() + "]. ");
					Collection<?> col =
							scapeData.getBaseScapeAgents(basefile);
					addAll(col);
					println("Done. " + col.size() + " " + getName()
							+ " created.");

					// save the base file to the prefs
					prefs.put(BASEFILE_KEY, basefile.getPath());
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e); // NOPMD
				}
			}
		}
	}

	private void print(String message) {
		loader.print(message);
	}

	private void println(String message) {
		loader.println(message);
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

		// get all the tables from the scape external data
		// and add them to the navigator as a Panel View Node

		TableCellRenderer dblRenderer = new DoubleCellRenderer(10);

		for (Map.Entry<String, TableModel> entry : scapeData.getTableModels()
				.entrySet()) {

			TableModel tmodel = entry.getValue();
			JTable table = new JTable(tmodel); // NOPMD
			table.setName(entry.getKey());
			table.setDefaultRenderer(Double.class, dblRenderer);

			// add PanelViewNode to the tree
			datasetsNode.add(new TreeUtil.PanelViewNode(this, table)); // NOPMD
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
	 * @param tmod
	 *            tree modifying model element
	 */
	public void addOutputTableNode(TreeModifier tmod) {
		outputTableProviders.add(tmod);
	}
}
