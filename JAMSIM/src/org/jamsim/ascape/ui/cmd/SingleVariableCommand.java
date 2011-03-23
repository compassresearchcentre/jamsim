package org.jamsim.ascape.ui.cmd;

import java.io.IOException;
import java.util.Map;

import org.jamsim.ascape.output.ROutput;
import org.jamsim.ascape.r.ScapeRInterface;
import org.omancode.util.io.FileUtil;

/**
 * An R command that takes the name of a single variable and displays a table.
 * Provides a {@link org.omancode.r.ui.RObjectTreeBuilder} for the user to
 * select the variable.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class SingleVariableCommand implements ScapeRCommand {

	private final String name;
	private final String[] variableTypes;
	private final String cmdText;

	/**
	 * YAML resource file specifying the GUI elements.
	 */
	public static final String YAML_FILE = "SingleVariableCommand.yml";

	/**
	 * Replacement string. Before executed on the supplied command, the commmand
	 * text is searched for this value and it is replaced with the single
	 * variable selected in the displayed
	 * {@link org.omancode.r.ui.RObjectTreeBuilder}.
	 */
	public static final String REPLACEMENT = "#VARNAME#";

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            name of command
	 * @param variableTypes
	 *            The types of variable class this command works on. eg:
	 *            "list,data.frame,integer" separated by comma (NB do not
	 *            include a space after the comma). Use {@code null} to work on
	 *            all objects. The {@link org.omancode.r.ui.RObjectTreeBuilder}
	 *            will only display objects of these types.
	 * @param cmdText
	 *            the command executed in R. Before executed
	 *            {@link #REPLACEMENT} is replaced with the single variable
	 *            selected in the displayed
	 *            {@link org.omancode.r.ui.RObjectTreeBuilder}
	 */
	public SingleVariableCommand(String name, String variableTypes,
			String cmdText) {
		this.name = name;
		this.variableTypes =
				(variableTypes == null) ? null : variableTypes.split(","); // NOPMD
		this.cmdText = cmdText;
	}

	@Override
	public String[] getVariableTypes() {
		return variableTypes;
	}

	@Override
	public ROutput generateROutput(ScapeRInterface scapeR,
			Map<String, Object> uiElements) {
		return generateROutput(scapeR,
				ScapeRCommandPanel.getSelectedNodeName(uiElements));
	}

	@Override
	public String generateCmdText(Map<String, Object> uiElements) {
		return generateCmdText(ScapeRCommandPanel
				.getSelectedNodeName(uiElements));
	}

	/**
	 * Generate {@link ROutput} from single variable name.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param varName
	 *            variable name
	 * @return routput
	 */
	public ROutput generateROutput(ScapeRInterface scapeR, String varName) {
		return new ROutput(scapeR.getDictionary().getDescription(varName),
				scapeR, generateCmdText(varName));
	}

	/**
	 * Generate R command text from single variable name.
	 * 
	 * @param varName
	 *            variable name
	 * @return R command text
	 */
	public String generateCmdText(String varName) {
		return cmdText.replace(REPLACEMENT, varName);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getYAML() throws IOException {
		return FileUtil.readResource(getClass(), YAML_FILE);
	}

	@Override
	public boolean isChart() {
		return false;
	}

}
