package org.jamsim.example;

import java.io.IOException;

import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.RootScape;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.ascape.ui.cmd.SingleVariableCommand;

/**
 * Initialisation class for the jamsim example simulation.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class JEMScape extends RootScape<JEMData> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5964161962274593351L;

	/**
	 * 100 iterations (1 to 4).
	 */
	public static final int NUM_ITERATIONS = 100;

	private static final int NUM_RUNS = 1;

	private MicroSimScape<JEMData> baseScape;
	private ScapeRInterface scapeR;

	/**
	 * Construct {@link JEMScape}.
	 */
	public JEMScape() {
		super("JEM", NUM_ITERATIONS, NUM_RUNS);
	}

	@Override
	public void createScape() {

		super.createScape();

		try {
			JEMData data = new JEMData(getLoader());

			baseScape = createBaseScape("People", new Person());

			baseScape.loadAgents(data);

			// start R and create scape dataframe
			scapeR = loadR(false);

			loadRStartupFile();

			scapeR.loadCommonRFunctions();

			// setup weights
			setupWeightCalculators(data.getWeightCalculators(scapeR));

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	@Override
	public void createGraphicViews() {

		// add scape R listener that acts on scape events
		addScapeRListener(null, "beginSim()", "beginRun()", "endOfRun()",
				"endOfSim()");

		addBaseFileUpdateRCmd("baseUpdated()");
		baseScape.getScapeNode().addBasefileNode("Basefile (people)",
				"expandPeople()");

		// remove from previous sim (if any)
		removeAllAnalysisMenuCommands();
		addAnalysisMenuCommand(VIEW_OBJECTS);

		super.createGraphicViews();
	}

	private static final SingleVariableCommand VIEW_OBJECTS =
			new SingleVariableCommand("View Objects", null, "with(people, "
					+ SingleVariableCommand.REPLACEMENT + ")");

}
