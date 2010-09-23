package org.jamsim.ascape;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.mutable.MutableInt;
import org.ascape.model.Agent;
import org.ascape.model.Scape;
import org.ascape.model.space.ListSpace;
import org.ascape.model.space.SpatialTemporalException;
import org.jamsim.ascape.output.OutputDatasetDefs;
import org.jamsim.ascape.output.ROutput;
import org.jamsim.ascape.output.ROutput1DMultiRun;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.ascape.r.ScapeRListener;
import org.jamsim.io.FileLoader;
import org.omancode.io.Output;
import org.omancode.r.RInterfaceException;

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
		setNumberRuns(numRuns);
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
		consoleOutput =
				new ConsoleOutput(this.getRunner().getEnvironment()
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
		msscape =
				new MicroSimScape<D>(new ListSpace(), baseScapeName,
						prototypeAgent, loader);
		add(msscape);

		// for some reason this needs to be added, otherwise the iterate
		// method on each agent doesn't get called
		msscape.addRule(ITERATE_RULE);

		return msscape;
	}

	/**
	 * Starts R on the base scape and creates a dataframe from the scape.
	 * <p>
	 * Uses the lowercase version of the base scape name as the dataframe
	 * symbol. When evaluating {@code rRunEndCommand} and commands during the
	 * creation of output datasets in {@link ROutput} and
	 * {@link ROutput1DMultiRun}, this symbol is searched for and replaced with
	 * the current run's dataframe name.
	 * 
	 * @param keepAllRunDFs
	 *            flag to keep the dataframes from each run in R. This means
	 *            creating each new dataframe with a unique name.
	 * @return scape R interface
	 * @throws IOException
	 *             if problem starting R
	 */
	public ScapeRInterface loadR(boolean keepAllRunDFs) throws IOException {
		scapeR = msscape.loadR(msscape.getName().toLowerCase(), false);
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

		scapeR.loadRFile(startUpFile);
	}

	/**
	 * Add scape R listener.
	 * 
	 * @param rIterationEndCommand
	 *            R command to run at the end of each iteration, or {@code null}
	 *            .
	 * @param rRunBeginCommand
	 *            R command to run at the beginning of each run, or {@code null}
	 *            .
	 * @param rRunEndCommand
	 *            R command to run at the end of each run, or {@code null}.
	 * @param rSimBeginCommand
	 *            R command to run at the beginning of the simulation (ie:
	 *            during construction of the {@link ScapeRListener}), or {@code
	 *            null}.
	 * @param rSimEndCommand
	 *            R command to run at the end of the simulation (ie: end of all
	 *            runs), or {@code null}.
	 */
	public void addScapeRListener(String rIterationEndCommand,
			String rRunBeginCommand, String rRunEndCommand,
			String rSimBeginCommand, String rSimEndCommand) {
		try {
			msscape.addView(new ScapeRListener(scapeR, rIterationEndCommand,
					rRunBeginCommand, rRunEndCommand, rSimBeginCommand,
					rSimEndCommand));
		} catch (RInterfaceException e) {
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
	public void addOutputDatasets(OutputDatasetDefs outputs) {
		consoleOutput.println(outputs.attachOutputDatasets(msscape, scapeR));
	}

	@Override
	public void createGraphicViews() {
		super.createGraphicViews();

		// *** FIX AscapeGUIUtil.getAdditionalBar().removeAll();

		// add multi run controller. this must be added AFTER any output
		// datasets/nodes
		msscape.addView(new MultipleRunController(numberRuns));

		if (scapeR != null) {
			// display prompt after all setup done
			scapeR.printPrompt();

			// add a dataframe information node
			msscape.addDataFrameNode(scapeR.getScapeDFRunName(0));

		}

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
	public final void setNumberRuns(int numberRuns) {
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
