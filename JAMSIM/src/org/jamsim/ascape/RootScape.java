package org.jamsim.ascape;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.prefs.Preferences;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ascape.model.Agent;
import org.ascape.model.Scape;
import org.ascape.model.space.ListSpace;
import org.ascape.model.space.SpatialTemporalException;
import org.ascape.util.swing.AscapeGUIUtil;
import org.jamsim.ascape.output.OutputDatasetDefs;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.ascape.r.ScapeRListener;
import org.jamsim.ascape.ui.AnalysisMenu;
import org.jamsim.ascape.ui.ScenarioBuilder;
import org.jamsim.ascape.ui.TableBuilder;
import org.jamsim.ascape.ui.TableBuilderConfig;
import org.jamsim.ascape.ui.cmd.ScapeRCommand;
import org.jamsim.ascape.ui.cmd.ScapeRCommandAction;
import org.jamsim.ascape.weights.WeightCalculator;
import org.jamsim.io.FileLoader;
import org.jamsim.shared.InvalidDataException;
import org.omancode.r.RFaceException;
import org.omancode.util.io.Output;

/**
 * Root scape that initialises and loads agents into a base microsimulation
 * scape.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 * @param <D>
 *            a scape data class that defines data external to the scape for use
 *            by agents, and for loading agents.
 */
public class RootScape<D extends ScapeData> extends Scape {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7744671548962486008L;

	private MicroSimScape<D> msscape;

	private transient Output consoleOutput;

	private FileLoader loader;

	private ScapeRInterface scapeR;

	private final MutableInt numberRuns = new MutableInt(2);

	private final int numIterations;

	/**
	 * Initialise the log here even though we don't use it (commons-beanutils
	 * does) so that the classloader picks up the log4j.properties in the JAMSIM
	 * package.
	 */
	private static Log log = LogFactory.getLog(RootScape.class);

	/**
	 * Create a {@link RootScape}.
	 * 
	 * @param name
	 *            This name appears in the navigator and Ascape title bar.
	 * @param numIterations
	 *            number of iterations per run
	 * @param numRuns
	 *            number of runs
	 */
	public RootScape(String name, int numIterations, int numRuns) {
		super();
		this.numIterations = numIterations;
		setName(name);
		this.numberRuns.setValue(numRuns);
	}

	@Override
	public void createScape() {
		super.createScape();

		// don't restart automatically when reached end of iterations
		setAutoRestart(false);

		// don't start model when ascape starts
		setStartOnOpen(false);

		// specify iterations
		try {
			setStopPeriod(numIterations);
		} catch (SpatialTemporalException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		// set up console output
		consoleOutput = new ConsoleOutput(this.getRunner().getEnvironment()
				.getConsole());

		// set up file loader
		loader = new FileLoader(this.getClass(), consoleOutput);

	}

	/**
	 * Return the microsimulation base scape. i.e: the scape holding the
	 * microsimulation agents.
	 * 
	 * @return microsimulation base scape
	 */
	public MicroSimScape<D> getBaseScape() {
		return msscape;
	}

	/**
	 * Get the console output.
	 * 
	 * @return console output
	 */
	public Output getConsoleOutput() {
		if (consoleOutput == null) {
			throw new IllegalStateException(
					"consoleOutput has not been initialized!");
		}
		return consoleOutput;
	}

	/**
	 * Set up the microsimulation base scape.
	 * 
	 * @param prototypeAgent
	 *            required by Ascape
	 * @param baseScapeName
	 *            name of the base scape
	 * @return microsimulation base scape
	 */
	public MicroSimScape<D> createBaseScape(String baseScapeName,
			Agent prototypeAgent) {

		// create scape of agents
		msscape = new MicroSimScape<D>(new ListSpace(), baseScapeName,
				prototypeAgent, loader);
		add(msscape);

		// for some reason this needs to be added, otherwise the iterate
		// method on each agent doesn't get called
		msscape.addRule(ITERATE_RULE);

		return msscape;
	}

	/**
	 * Setup a panel view containing a set of weight calculators, and set the
	 * weight calculator to that specified in the preferences.
	 * 
	 * @param wcalcsvarmaps
	 *            map of weight calculators
	 */
	public void setupWeightCalculators(
			Map<String, Map<String, WeightCalculator>> wcalcsvarmaps,
			Map subgroupsToOptions) {
		if (wcalcsvarmaps != null) {
			ScenarioBuilder wcalcPanel = new ScenarioBuilder(wcalcsvarmaps,
					subgroupsToOptions, msscape);

			msscape.setWeightCalculatorPanelView(wcalcPanel);

			for (Map<String, WeightCalculator> wcalcsyearsmap : wcalcsvarmaps
					.values()) {
				for (WeightCalculator wcalc : wcalcsyearsmap.values()) {
					wcalc.addObserver((Observer) this);
				}
			}
		}
		WeightCalculator currentCalc = selectWeightCalculatorFromPrefs(
				wcalcsvarmaps, loader.getPrefs());
		try {
			msscape.setWeightCalculator(currentCalc);
		} catch (InvalidDataException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void setupTableBuilder(TableBuilderConfig tableBuilderConfig) {

		TableBuilder tableBuilderPanel = new TableBuilder(tableBuilderConfig, msscape);

		msscape.setCreateTableOptionsPanelView(tableBuilderPanel);
	}

	/**
	 * Select weight calculator specified in the preferences from the supplied
	 * map of weight calculators.
	 * 
	 * @param wcalcsvarmaps
	 *            map of weight calculators
	 * @param prefs
	 *            preferences
	 * @return current weight calculator
	 */
	public static WeightCalculator selectWeightCalculatorFromPrefs(
			Map<String, Map<String, WeightCalculator>> wcalcsvarmaps,
			Preferences prefs) {

		if (wcalcsvarmaps == null || wcalcsvarmaps.isEmpty()) {
			throw new IllegalStateException("No weight calculators defined.");
		}

		String wcalcName = prefs.get(WeightCalculator.WCALC_KEY, "");

		WeightCalculator wcalc;

		if (wcalcName.equals("") ||  wcalcsvarmaps.get(wcalcName) == null) {
			wcalc = wcalcsvarmaps.values().iterator().next().values()
					.toArray(new WeightCalculator[0])[0];
		}

		else {
			wcalc = wcalcsvarmaps.get(wcalcName).values()
					.toArray(new WeightCalculator[0])[0];
		}

		return (wcalc == null) ? wcalcsvarmaps.values().toArray(
				new WeightCalculator[wcalcsvarmaps.size()])[0] : wcalc;
	}

	/**
	 * Starts R on the base scape and creates a dataframe from the scape.
	 * <p>
	 * Uses the lowercase version of the base scape name as the dataframe
	 * symbol. When evaluating {@code rRunEndCommand} and commands during the
	 * creation of output datasets in {@link org.jamsim.ascape.output.ROutput}
	 * and {@link org.jamsim.ascape.output.ROutput1DMultiRun}, this symbol is
	 * searched for and replaced with the current run's dataframe name.
	 * 
	 * @param keepAllRunDFs
	 *            flag to keep the dataframes from each run in R. This means
	 *            creating each new dataframe with a unique name.
	 * @return scape R interface
	 * @throws IOException
	 *             if problem starting R
	 */
	public ScapeRInterface loadR(boolean keepAllRunDFs, boolean showRMenu) throws IOException {
		scapeR = msscape.loadR(msscape.getName().toLowerCase(), false, showRMenu);
		return scapeR;
	}

	/**
	 * Run commands in the R startup file.
	 * 
	 * Uses "R startup file" as the preferences key to lookup the location of
	 * the R startup file.
	 * 
	 * @throws IOException
	 *             if problem loading file
	 */
	public void loadRStartupFile() throws IOException {

		// get startup file
		File startUpFile = loader.getFile("R startup file");

		// change working directory of R and Java to same directory as file
		// so any source() commands when executed from command line
		// or elsewhere will be operating from the startup file's directory
		scapeR.setWd(startUpFile.getParent());

		scapeR.loadRFile(startUpFile);
	}

	/**
	 * Add scape R listener.
	 * 
	 * @param rIterationEndCommand
	 *            R command to run at the end of each iteration, or {@code null}
	 *            .
	 * @param rSimBeginCommand
	 *            R command to run at the beginning of the simulation (ie: at
	 *            the beginning of the first run only), or {@code null}.
	 * @param rRunBeginCommand
	 *            R command to run at the beginning of each run, or {@code null}
	 *            .
	 * @param rRunEndCommand
	 *            R command to run at the end of each run, or {@code null}.
	 * @param rSimEndCommand
	 *            R command to run at the end of the simulation (ie: end of all
	 *            runs), or {@code null}.
	 */
	public void addScapeRListener(String rIterationEndCommand,
			String rSimBeginCommand, String rRunBeginCommand,
			String rRunEndCommand, String rSimEndCommand) {
		try {
			msscape.addView(new ScapeRListener(scapeR, rIterationEndCommand,
					rSimBeginCommand, rRunBeginCommand, rRunEndCommand,
					rSimEndCommand));
		} catch (RFaceException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Define the R command that is called by
	 * {@link ScapeRInterface#baseFileUpdated()} and call it.
	 * 
	 * @param cmd
	 *            base file update command, or {@code null} if none
	 */
	public void addBaseFileUpdateRCmd(String cmd) {
		scapeR.setBaseFileUpdateCmd(cmd);
		scapeR.baseFileUpdated();
	}

	/**
	 * Add a series of output datasets to the base scape.
	 * 
	 * @param outputs
	 *            output dataset definitions
	 */
	public void addOutputDatasets(OutputDatasetDefs<D> outputs) {
		consoleOutput.println(outputs.attachOutputDatasets(msscape, scapeR));
	}

	@Override
	public void createGraphicViews() {
		super.createGraphicViews();

		// *** QUICK FIX
		// remove otherwise weightings button will
		// appear twice after Reload Model.
		AscapeGUIUtil.getAdditionalBar().removeAll();

		// add multi run controller. this must be added AFTER any output
		// datasets/nodes
		msscape.addView(new MultipleRunController(numberRuns));

		if (scapeR != null) {
			// display prompt after all setup done
			// scapeR.printPrompt();

			// add a dataframe information node
		//	msscape.getScapeNode()
		//			.addDataFrameNode(scapeR.getScapeDFRunName(0));

		}
	}

	/**
	 * Add a {@link ScapeRCommand} to the Analysis menu.
	 * 
	 * @param command
	 *            command
	 */
	public void addAnalysisMenuCommand(ScapeRCommand command) {
		AnalysisMenu.INSTANCE.addMenuItem(new ScapeRCommandAction(scapeR,
				command));
	}

	/**
	 * Remove all commands from the Analysis menu.
	 */
	public void removeAllAnalysisMenuCommands() {
		AnalysisMenu.INSTANCE.removeAll();
	}

	/**
	 * Number of simulation runs.
	 * 
	 * @return simulation runs.
	 */
	public int getNumberRuns() {
		return numberRuns.intValue();
	}

	/**
	 * Set number of simulation runs.
	 * 
	 * @param numberRuns
	 *            simulation runs.
	 */
	public void setNumberRuns(int numberRuns) {
		this.numberRuns.setValue(numberRuns);
	}

	/**
	 * Whether output datasets should write their outputs to a file.
	 * 
	 * @return write to file
	 */
	public boolean isWriteResultsToFile() {
		return msscape.isResultsToFile();
	}

	/**
	 * Set whether output datasets should write their outputs to a file.
	 * 
	 * @param write
	 *            write to file
	 */
	public void setWriteResultsToFile(boolean write) {
		msscape.setResultsToFile(write);
	}

	/**
	 * Get {@link FileLoader}.
	 * 
	 * @return file loader
	 */
	public FileLoader getLoader() {
		return loader;
	}

}