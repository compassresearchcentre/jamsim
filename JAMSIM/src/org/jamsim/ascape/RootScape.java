package org.jamsim.ascape;

import java.io.IOException;

import org.apache.commons.lang.mutable.MutableInt;
import org.ascape.model.Agent;
import org.ascape.model.Scape;
import org.ascape.model.space.ListSpace;
import org.ascape.model.space.SpatialTemporalException;
import org.jamsim.ascape.output.OutputDatasetDefs;
import org.jamsim.ascape.output.ROutput;
import org.jamsim.ascape.output.ROutputMultiRun;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.io.FileLoader;
import org.omancode.io.Output;

/**
 * Root scape that initialises and loads agents into a base microsimulation
 * scape.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 * @param <D>
 */
public class RootScape<D extends ScapeData> extends Scape {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7744671548962486008L;

	private MicroSimScape<D> msscape;

	private transient Output consoleOutput;

	private ScapeRInterface scapeR;

	private final MutableInt numberRuns = new MutableInt(2);

	/**
	 * Set up the microsimulation.
	 * 
	 * @param scapeDataCreator
	 *            scape data creator
	 * @param prototypeAgent
	 *            required by Ascape
	 * @param baseScapeName
	 *            name of the base scape
	 * @param numIterations
	 *            number of iterations per run
	 * @param numRuns
	 *            number of runs
	 */
	public void createScape(ScapeDataCreator<D> scapeDataCreator,
			Agent prototypeAgent, String baseScapeName, int numIterations,
			int numRuns) {
		super.createScape();

		// set up console output
		consoleOutput =
				new ConsoleOutput(this.getRunner().getEnvironment()
						.getConsole());

		// set up file loader
		FileLoader loader = new FileLoader(this.getClass(), consoleOutput);

		// load scape data
		D scapeData;
		try {
			scapeData = scapeDataCreator.create(loader);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		// create scape of agents
		msscape =
				new MicroSimScape<D>(new ListSpace(), baseScapeName,
						prototypeAgent, loader, scapeData);
		add(msscape);

		// load agents
		msscape.loadAgents();

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

		// set number of runs
		numberRuns.setValue(numRuns);

		// for some reason this needs to be added, otherwise the iterate
		// method on each agent doesn't get called
		msscape.addRule(ITERATE_RULE);

	}

	/**
	 * Adds R to the base scape.
	 * <p>
	 * Uses the lowercase version of the base scape name as the dataframe
	 * symbol. When evaluating {@code rRunEndCommand} and commands during the
	 * creation of output datasets in {@link ROutput} and
	 * {@link ROutputMultiRun}, this symbol is searched for and replaced with
	 * the current run's dataframe name.
	 * <p>
	 * Uses "R startup file" as the preferences key to lookup the location of
	 * the R startup file.
	 * 
	 * @param rRunEndCommand
	 *            R command to run at the end of each run, or {@code null}.
	 * @param keepAllRunDFs
	 *            flag to keep the dataframes from each run in R. This means
	 *            creating each new dataframe with a unique name.
	 * @return scape R interface
	 */
	public ScapeRInterface addR(String rRunEndCommand, boolean keepAllRunDFs) {

		try {
			scapeR =
					msscape.addR(msscape.getName().toLowerCase(),
							"R startup file", rRunEndCommand, false);
			return scapeR;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

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

		// add multi run controller
		msscape.addView(new MultipleRunController(numberRuns, true));
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

}
