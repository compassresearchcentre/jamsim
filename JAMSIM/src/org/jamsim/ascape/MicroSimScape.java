package org.jamsim.ascape;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.io.beans.CBuildFromCollection;
import net.casper.io.file.CBuildFromFile;
import net.casper.io.file.util.ExtFileFilter;
import net.sf.jga.swing.spreadsheet.Cell;

import org.ascape.model.Agent;
import org.ascape.model.Scape;
import org.ascape.model.space.CollectionSpace;
import org.ascape.runtime.swing.SwingRunner;
import org.ascape.runtime.swing.navigator.PanelViewNodes;
import org.ascape.util.swing.AscapeGUIUtil;
import org.jamsim.io.FileLoader;
import org.jamsim.r.RInterfaceHL;

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
public class MicroSimScape<D extends ScapeData> extends Scape {

	/**
	 * Serialization.
	 */
	private static final long serialVersionUID = 5534365905529673862L;

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

	private static final RecordedMicroSimTreeBuilder TREE_BUILDER =
			new RecordedMicroSimTreeBuilder();

	private PanelViewNodes outputTablesNode;

	private static final String OUTPUTDIR_KEY = "output directory";

	private File outputDirectory;

	private static final String BASEFILE_KEY = "base file";

	/**
	 * The base file is kept as an instance variable and exposed as a model
	 * parameter.
	 */
	private File basefile = null;

	/**
	 * Used to help load the base file.
	 */
	private final FileLoader loader;

	/**
	 * Prefs for loading/saving location of base file.
	 */
	private final Preferences prefs;

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
	 * @param space
	 *            scape space
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
		// loadAgents();

		// tell the patients scape not to auto create, otherwise
		// it will remove the agents we've added to it and
		// replace them with clones with parameter values of 0
		// this.setAutoCreate(false);

		loadOutputDirectory();

		println("Output directory: " + getOutputDirectory());
	}

	/**
	 * Set the Navigator Tree Builder here. At this point the SwingEnvironment
	 * exists. We can't do this in {@link #createGraphicViews()} because at that
	 * point the navigator has already been built (see the order of scape
	 * creation etc. in
	 * {@link SwingRunner#openImplementation(String[], boolean)}.
	 * 
	 * This method also overrides the scape implementation which removes all
	 * agents and replaces them with clones with parameter values of 0.
	 */
	@Override
	public void createScape() {
		AscapeGUIUtil.setNavigatorTreeBuilder(this, TREE_BUILDER);
	}

	/**
	 * Get the output tables node. Must be called after the Navigator tree has
	 * been created. This happens after {@link #createScape()} but before
	 * {@link #createGraphicViews()} is called.
	 * 
	 * @return output tables node
	 */
	public PanelViewNodes getOutputTablesNode() {

		if (outputTablesNode == null) {
			MicroSimScapeNode scapeNode =
					(MicroSimScapeNode) TREE_BUILDER.getCreatedTreeNode(this);

			if (scapeNode == null) {
				throw new IllegalStateException("Navigator tree node for "
						+ getClass().getSimpleName() + " \"" + name
						+ "\" not yet created");
			}

			outputTablesNode = scapeNode.getOutputTablesNode();
		}

		return outputTablesNode;
	}

	/**
	 * Loads this scape with agents from a base file. The location of the base
	 * file is stored in Preferences. If such a location does not exist, then
	 * the user is prompted for the base file location. Calls
	 * {@link #setBasefile(String)} to do the work.
	 * 
	 * <p>
	 * The method must be called after construction and after the scape has been
	 * added to its parent, so that the scape variable is available.
	 */
	public void loadAgents() {
		if (scape == null) {
			throw new RuntimeException(name
					+ " has not been added to a scape yet");
		}
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
								null, CBuildFromFile.FileTypeFactories
										.getFilter());
			}

			// if we have a passed in, or selected base file, then load it
			// and set the base file instance variable
			if (newBaseFile != null) {

				try {
					basefile = newBaseFile;

					// remove all existing agents (if any are present
					// from previous loads this session)
					clear();

					println("Loading base file [" + basefile.getPath()
							+ "]...... ");

					// AscapeGUIUtil.flushConsoleLog(this);
					// Thread.yield();

					Collection<?> col =
							scapeData.getBaseScapeAgents(basefile);
					addAll(col);

					println("Done. " + size() + " " + getName() + " created.");

					// save the base file to the prefs
					prefs.put(BASEFILE_KEY, basefile.getPath());

				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e); // NOPMD }
				}
			}
		}
	}

	private void loadOutputDirectory() {
		setOutputDirectory(prefs.get(OUTPUTDIR_KEY, ""));
	}

	/**
	 * Get the scape's output directory. Used for result output to files.
	 * 
	 * @return output directory
	 */
	public final String getOutputDirectory() {

		if (outputDirectory == null) {
			throw new IllegalStateException(
					"An output directory has not been specified.");
		}

		return outputDirectory.getPath();
	}

	/**
	 * Set the output directory for the scape.
	 * 
	 * @param strOutputDir
	 *            output directory
	 */
	public final void setOutputDirectory(String strOutputDir) {
		// strOutputDir will be null when editing the base file text area in the
		// model parameters, before enter is pressed
		// strOutputDir will be the empty string "" when there are no saved
		// preferences
		if (strOutputDir != null) {
			File fOutputDir = new File(strOutputDir);

			// if the directory doesn't exist then show a file chooser
			// dialog for the user to select one
			if (!fOutputDir.exists()) {
				fOutputDir =
						loader.showOpenDialogForDirectories(
								"Select output directory", null);
			}

			if (fOutputDir != null) {
				this.outputDirectory = fOutputDir;

				// save the base file to the prefs
				prefs.put(OUTPUTDIR_KEY, fOutputDir.getPath());
			}
		}
	}

	public CDataCacheContainer getDataSetOfAgents() throws CDataGridException {
		CBuildFromCollection builder =
				new CBuildFromCollection(name, this,
						getPrototypeAgent().getClass().getSuperclass(), null);

		return new CDataCacheContainer(builder);
	}

	private final void print(String message) {
		loader.print(message);
	}

	private final void println(String message) {
		loader.println(message);
	}
}
