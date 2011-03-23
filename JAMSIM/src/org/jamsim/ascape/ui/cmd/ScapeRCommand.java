package org.jamsim.ascape.ui.cmd;

import java.util.Map;

import org.jamsim.ascape.output.ROutput;
import org.jamsim.ascape.r.ScapeRInterface;
import org.omancode.r.ui.RUICommand;

/**
 * An {@link RUICommand} that also provides an {@link ROutput}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface ScapeRCommand extends RUICommand {

	/**
	 * Generate an {@link ROutput} from the UI elements.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param uiElements
	 *            UI elements
	 * @return routput
	 */
	ROutput generateROutput(ScapeRInterface scapeR,
			Map<String, Object> uiElements);

}
