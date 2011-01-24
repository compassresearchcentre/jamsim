package org.jamsim.ascape.r;

import java.util.Map;

import org.jamsim.ascape.output.ROutput;
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
	 * @param uiElements UI elements
	 * @return routput
	 */
	ROutput generateROutput(Map<String, Object> uiElements);

}
