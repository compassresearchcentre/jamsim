package org.jamsim.ascape;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.tree.TreePath;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.io.beans.CBuildFromCollection;
import net.casper.io.file.CBuildFromFile;

import org.apache.commons.lang.ArrayUtils;
import org.ascape.model.Agent;
import org.ascape.model.Scape;
import org.ascape.model.space.CollectionSpace;
import org.ascape.runtime.swing.DesktopEnvironment;
import org.ascape.runtime.swing.SwingRunner;
import org.ascape.runtime.swing.navigator.PanelViewExisting;
import org.ascape.runtime.swing.navigator.PanelViewNode;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.util.swing.AscapeGUIUtil;
import org.ascape.view.vis.ChartView;
import org.jamsim.ascape.navigator.EndOfSimNodeProvider;
import org.jamsim.ascape.navigator.MicroSimScapeNode;
import org.jamsim.ascape.navigator.OutputDatasetNodeProvider;
import org.jamsim.ascape.navigator.OutputNode;
import org.jamsim.ascape.navigator.RecordedMicroSimTreeBuilder;
import org.jamsim.ascape.navigator.SubFolderNode;
import org.jamsim.ascape.output.ChartProvider;
import org.jamsim.ascape.output.OutputDatasetProvider;
import org.jamsim.ascape.output.REXPDatasetProvider;
import org.jamsim.ascape.output.ROutput;
import org.jamsim.ascape.output.ROutput1DMultiRun;
import org.jamsim.ascape.r.PanelViewDatasetProvider;
import org.jamsim.ascape.r.RFileInterface;
import org.jamsim.ascape.r.RLoader;
import org.jamsim.ascape.r.ScapeRCommand;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.ascape.ui.AnalysisMenu;
import org.jamsim.ascape.ui.ParameterSetAction;
import org.jamsim.ascape.ui.ScapeRCommandAction;
import org.jamsim.ascape.weights.WeightCalculator;
import org.jamsim.io.FileLoader;
import org.jamsim.io.ParameterSet;
import org.omancode.r.RInterfaceException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPReference;

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
		Observer {

	/**
	 * Serialization.
	 */
	private static final long serialVersionUID = 5534365905529673862L;

	private static final RecordedMicroSimTreeBuilder TREE_BUILDER =
			new RecordedMicroSimTreeBuilder();

	private MicroSimScapeNode scapeNode;

	private static final String OUTPUTDIR_KEY = "output directory";

	private File outputDirectory;

	private static final String BASEFILE_KEY = "base file";

	private boolean outputToFile = false;

	/**
	 * The base file is kept as an instance variable and exposed as a model
	 * parameter.
	 */
	private File basefile = null;

	/**
	 * Used to help load files.
	 */
	private final FileLoader loader;

	/**
	 * Prefs for loading/saving location of base file.
	 */
	private final Preferences prefs;

	/**
	 * Get whether {@link OutputDataset}s for this scape should write their
	 * results to a file.
	 * 
	 * @return {@code true} if this scape's {@link OutputDataset}s should write
	 *         to a file.
	 */
	public boolean isResultsToFile() {
		return outputToFile;
	}

	/**
	 * Set whether {@link OutputDataset}s for this scape should write their
	 * results to a file.
	 * 
	 * @param outputToFile
	 *            write to file, or not
	 */
	public void setResultsToFile(boolean outputToFile) {
		this.outputToFile = outputToFile;
	}

	/**
	 * Global data external to scape. Used by agents of this scape and used to
	 * load these agents.
	 */
	private D scapeData;

	/**
	 * Return the {@link ScapeData} object. This method allows agents to access
	 * the external/global scape data.
	 * 
	 * @return scape data
	 */
	public D getScapeData() {
		return scapeData;
	}

	private ScapeRInterface scapeR;

	/**
	 * Provide R interface.
	 * 
	 * @return r interface
	 */
	public ScapeRInterface getScapeRInterface() {
		return scapeR;
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
	 */
	public MicroSimScape(CollectionSpace space, String name,
			Agent prototypeAgent, FileLoader loader) {
		super(space);
		this.loader = loader;
		this.prefs = loader.getPrefs();

		// set prototype agent after setting scapeData
		// in case the agent wants to make use of scapeData
		setPrototypeAgent(prototypeAgent);

		setName(name);

		// tell the scape not to auto create, otherwise
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
		AscapeGUIUtil.setNavigatorTreeBuilder(TREE_BUILDER);
	}

	@Override
	public void createGraphicViews() {
		if (wcalc != null) {
			// Add navigator node
			addParameterSetNode(wcalc);

			// Add weightings button to additional toolbar
			addWeightingsButton(wcalc);
		}

		// setup analysis menu
		List<ScapeRCommand> commands =
				scapeData.getAnalysisMenuCommands(scapeR);
		if (commands != null) {
			AnalysisMenu amenu = AnalysisMenu.INSTANCE;

			// clear from previous sim (if any)
			amenu.removeAll();

			for (ScapeRCommand cmd : commands) {
				amenu.addMenuItem(new ScapeRCommandAction(scapeR, cmd));
			}
		}

	}

	/**
	 * Get the output tables node. Must be called after the Navigator tree has
	 * been created. This happens after {@link #createScape()} but before
	 * {@link #createGraphicViews()} is called.
	 * 
	 * @return output tables node
	 */
	private SubFolderNode getOutputTablesNode() {
		initScapeNode();
		return scapeNode.getOutputTablesNode();
	}

	private void initScapeNode() {
		if (scapeNode == null) {
			scapeNode =
					(MicroSimScapeNode) TREE_BUILDER.getCreatedTreeNode(this);

			if (scapeNode == null) {
				throw new IllegalStateException("Navigator tree node for "
						+ getClass().getSimpleName() + " \"" + name
						+ "\" not yet created");
			}

		}
	}

	/**
	 * Add a {@link OutputDatasetProvider} which provides nodes at the end of
	 * runs/simulation under the output tables node.
	 * 
	 * @param provider
	 *            provider
	 */
	public void addOutputDataset(OutputDatasetProvider provider) {
		addOutputDataset(provider, null);
	}

	/**
	 * Add a {@link OutputDatasetProvider} which provides nodes at the end of
	 * runs/simulation under a particular node group under the output tables
	 * node.
	 * 
	 * @param provider
	 *            provider
	 * @param nodeGroupName
	 *            node group name or {@code null} if this dataset will appear
	 *            under outputTablesNode
	 */
	public void addOutputDataset(OutputDatasetProvider provider,
			String nodeGroupName) {

		OutputDatasetNodeProvider datasetNode =
				new OutputDatasetNodeProvider(this, provider);

		addView(new OutputNode(getOutputTablesNode(), nodeGroupName,
				datasetNode));

	}

	/**
	 * Setup a scape listener that adds provider as a node under "Output tables"
	 * at the end of all runs.
	 * 
	 * @param provider
	 *            panelview provider
	 */
	public void addEndOfSimOutputNode(PanelViewProvider provider) {
		addView(new OutputNode(getOutputTablesNode(),
				new EndOfSimNodeProvider(new PanelViewNode(provider))));
	}

	/**
	 * Add a dataset node under "User Tables". The node is automatically opened
	 * after it is added.
	 * 
	 * Takes a provider instead of a dataset directly, so the provider can serve
	 * up whatever it wants each time the node is opened.
	 * 
	 * Takes a {@link OutputDatasetNodeProvider} instead of a
	 * {@link PanelViewProvider} because a dataset is the most likely type of
	 * node to be produced and we can do the wrapping in a PanelView for the
	 * caller.
	 * 
	 * 
	 * @param provider
	 *            provider
	 * @param subFolderName
	 *            name of sub folder to add node under, or {@code null} to add
	 *            directly under "User Tables".
	 */
	public void addUserNode(OutputDatasetProvider provider,
			String subFolderName) {
		initScapeNode();
		PanelViewNode newNode =
				scapeNode.addUserNode(new PanelViewDatasetProvider(provider),
						subFolderName);

		try {
			AscapeGUIUtil.getNavigator().setSelectionPath(
					new TreePath(newNode.getPath()));
		} catch (RuntimeException e) {
			scapeNode.removeNodeFromParent(newNode);
			throw e;
		}
	}

	/**
	 * Add an Output table immediately, ie: not via the Scape Listener
	 * {@link OutputDatasetNodeProvider}.
	 * 
	 * @param provider
	 *            provider
	 * @param subFolderName
	 *            name of sub folder to add node under, or {@code null} to add
	 *            directly under "Output Tables".
	 */
	public void addOutputNode(OutputDatasetProvider provider,
			String subFolderName) {
		initScapeNode();
		scapeNode.addOutputNode(new PanelViewDatasetProvider(provider),
				subFolderName);
	}

	/**
	 * Convenience method that can be called from R without having to cast
	 * provider.
	 * 
	 * @param provider
	 *            provider
	 * @param subFolderName
	 *            name of sub folder to add node under, or {@code null} to add
	 *            directly under "Output Tables".
	 */
	public void addOutputNode(REXPDatasetProvider provider,
			String subFolderName) {
		addOutputNode((OutputDatasetProvider) provider, subFolderName);
	}

	/**
	 * Add a panel view node under "Graphs".
	 * 
	 * @param provider
	 *            provider of the panel view to create node for
	 */
	public void addGraphNode(PanelViewProvider provider) {
		addGraphNode(provider, null);
	}

	/**
	 * Add a panel view node under a subfolder of "Graphs".
	 * 
	 * @param provider
	 *            provider of the panel view to create node for
	 * @param subFolderName
	 *            of navigator subfolder under "Graphs" to create node, or
	 *            {@code null} to create node directly under "Graphs"
	 * @return the newly created node
	 */
	public PanelViewNode addGraphNode(PanelViewProvider provider,
			String subFolderName) {
		initScapeNode();
		return scapeNode.addGraphNode(provider, subFolderName);
	}

	private final Map<String, String> dataFrameNodeMap =
			new HashMap<String, String>();

	private WeightCalculator wcalc;

	/**
	 * Add a data frame node to the navigator. Exits silently without creating a
	 * duplicate if a node of the same name already exists.
	 * 
	 * Must be called after Navigator has been created, eg: in
	 * createGraphicViews or later.
	 * 
	 * @param name
	 *            dataframe name in R
	 */
	public void addDataFrameNode(String name) {

		if (!dataFrameNodeMap.containsKey(name)) {
			initScapeNode();
			scapeNode.addDataFrameNode(name);
			dataFrameNodeMap.put(name, null);
		}

	}

	/**
	 * Add node which displays the contents of the basefile when clicked on.
	 * 
	 * @param name
	 *            basefile node name
	 * @param rcmd
	 *            R command which returns a dataframe, ie: the basefile
	 */
	public void addBasefileNode(String name, String rcmd) {
		initScapeNode();
		scapeNode.addBasefileNode(name, rcmd);
	}

	/**
	 * Add a parameter set node under the "Parameter sets" folder. Creates
	 * "Parameter sets" node if it doesn't exist.
	 * 
	 * @param pset
	 *            parameter set
	 */
	public final void addParameterSetNode(ParameterSet pset) {
		initScapeNode();
		scapeNode.addParameterSetNode(pset);
	}

	/**
	 * Set weights on all agents and set-up observers to register changes when
	 * the {@link WeightCalculator} changes.
	 * 
	 * @param wcalc
	 *            weight calculator
	 */
	public void setWeightCalculator(WeightCalculator wcalc) {
		this.wcalc = wcalc;

		// Set scape observer that will refresh dataframe in R
		// via call to #update
		wcalc.addObserver(this);

		// Set weights & observers on all agents
		for (Object agent : this) {
			MicroSimCell<?> cell = (MicroSimCell<?>) agent;
			cell.setWeight(wcalc);
			wcalc.addObserver(cell);
		}

		// Update dataframe with (potentially changed) weights
		try {
			scapeR.assignScapeDataFrame(0);
		} catch (RInterfaceException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Called after weights have changed (and after all children have been
	 * reweighted). Here we update the scape dataframe in R.
	 * 
	 */
	@Override
	public void update(Observable o, Object arg) {
		try {
			scapeR.assignScapeDataFrame(0);
		} catch (RInterfaceException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Add a "Weightings" button to the additional tool bar.
	 * 
	 * @param weightings
	 *            weightings parameter set
	 */
	private void addWeightingsButton(ParameterSet weightings) {
		// create action
		ParameterSetAction weightingsAction =
				new ParameterSetAction(weightings, "Weightings", "Weightings");
		weightingsAction.putValue(Action.SMALL_ICON, DesktopEnvironment
				.getIcon("Scales"));

		// add button to toolbar
		AscapeGUIUtil.addAdditionalBarButton(weightingsAction);
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
	 * Loads this scape with agents from a base file. The location of the base
	 * file is stored in Preferences. If such a location does not exist, then
	 * the user is prompted for the base file location.
	 * 
	 * The method must be called after construction and after the scape has been
	 * added to its parent, so that the scape variable is available.
	 * 
	 * @param scapeData
	 *            provides method to load agents. Also used by agents during
	 *            their initialisation to obtain globals.
	 * @throws IOException
	 *             if problem loading the agents
	 */
	public void loadAgents(D scapeData) throws IOException {
		if (scape == null) {
			throw new IllegalStateException(name
					+ " has not been added to a scape yet");
		}
		this.scapeData = scapeData;
		MicroSimCell.setData(scapeData);

		loadBasefile(prefs.get(BASEFILE_KEY, ""));
	}

	/**
	 * Change the base file to the specified file after agents have already been
	 * loaded. If the base file doesn't exist, then a file chooser dialog is
	 * displayed to allow the user to select. If specified or selected, any
	 * existing agents are removed and the base file is loaded.
	 * 
	 * This method is used when setting the base file variable from the model
	 * parameters.
	 * 
	 * @param bfileName
	 *            base file to load
	 */
	public void setBasefile(String bfileName) {

		// bfileName will be null when editing the base file text area in the
		// model parameters, before enter is pressed
		if (bfileName != null) {
			try {
				loadBasefile(bfileName);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Sets the base file to the specified file and loads it. If the base file
	 * doesn't exist, then a file chooser dialog is displayed to allow the user
	 * to select.
	 * 
	 * @param bfileName
	 *            base file to load
	 * @throws IOException
	 *             if problem loading the base file
	 */
	private void loadBasefile(String bfileName) throws IOException {
		File newBaseFile = new File(bfileName);

		// if the new base file doesn't exist then show a file chooser
		// dialog for the user to select one
		if (!newBaseFile.exists()) {
			newBaseFile =
					loader.showOpenDialog("Select base file to load", null,
							CBuildFromFile.FileTypeFactories.getFilter());
		}

		// if we have a passed in, or selected base file, then load it
		// and set the base file instance variable
		if (newBaseFile != null) {

			basefile = newBaseFile;

			// remove all existing agents (if any are present
			// from previous loads this session)
			clear();

			println("Loading base file [" + basefile.getPath() + "]...... ");

			Collection<? extends Agent> col = scapeData.loadAgents(basefile);
			addAll(col);

			for (Agent a : col) {
				a.setScape(this);
			}

			println("Done. " + size() + " " + getName() + " created.");

			// save the base file to the prefs
			prefs.put(BASEFILE_KEY, basefile.getPath());
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

	/**
	 * Return the agents of this scape as a caper dataset.
	 * 
	 * @return casper dataset of this scape's agents.
	 * 
	 * @throws CDataGridException
	 *             if problem creating dataset
	 */
	public CDataCacheContainer getDataSetOfAgents() throws CDataGridException {
		CBuildFromCollection builder =
				new CBuildFromCollection(name, this, getPrototypeAgent()
						.getClass().getSuperclass(), null);

		return new CDataCacheContainer(builder);
	}

	/**
	 * Convenience println method.
	 * 
	 * @param message
	 */
	private void println(String message) {
		loader.println(message);
	}

	/**
	 * Add R to this scape, create a dataframe in R from the scape, and create
	 * data dictionary named "dict" in R.
	 * 
	 * Agents and ScapeData must have already been created via a call to
	 * {@link #loadAgents(ScapeData)} prior to calling this.
	 * 
	 * @param dataFrameSymbol
	 *            replacement symbol. When evaluating {@code rRunEndCommand} and
	 *            commands during the creation of output datasets in
	 *            {@link ROutput} and {@link ROutput1DMultiRun}, this symbol is
	 *            searched for and replaced with the current run's dataframe
	 *            name.
	 * @param keepAllRunDFs
	 *            flag to keep the dataframes from each run in R. This means
	 *            creating each new dataframe with a unique name.
	 * @return scape R interface
	 * @throws IOException
	 *             if problem looking up {@code startUpFilePrefsKey} or
	 *             initialising R
	 */
	public ScapeRInterface loadR(String dataFrameSymbol, boolean keepAllRunDFs)
			throws IOException {

		// load R

		RLoader rLoader;
		try {
			rLoader = RLoader.INSTANCE;
		} catch (ExceptionInInitializerError e) {

			// re-throw exception that occurred in the initializer
			// as an exception our caller can deal with
			Throwable eInInit = e.getCause();
			throw new RInterfaceException(eInInit.getMessage(), eInInit); // NOPMD
		}

		// create R scape interface
		scapeR =
				new ScapeRInterface(rLoader, this, dataFrameSymbol,
						keepAllRunDFs);

		rLoader.ascapeStart();

		// create initial dataframe from scape
		// NB: at this point, agent.initialize() will NOT have been called
		// on the agents. This will effect any getters that have values
		// that are dependent on initialisation code.
		scapeR.assignScapeDataFrame(0);

		DataDictionary dict = scapeData.getDataDictionary();
		if (dict != null) {
			scapeR.setDictionary(dict);
		}

		// create R menu with R file editing functions
		RFileInterface.getInstance(this, scapeR, loader);

		return scapeR;
	}

	/**
	 * Add a chart to the scape. Adds the chart as a listener and also creates a
	 * node in the navigator for the chart.
	 * 
	 * @param source
	 *            a chart provider
	 */
	public void addChart(ChartProvider source) {
		int chartType = source.getChartViewType();

		ChartView chart = new ChartView(chartType);
		chart.setPersistAfterScapeCloses(true);

		// add listener but don't create frame
		scape.addView(chart, false);

		// setup the chart AFTER adding it to the scape
		for (String seriesName : source.getChartSeries()) {
			chart.addSeries(seriesName);
		}

		addGraphNode(new PanelViewExisting(chart, source.getName()));
	}
}
