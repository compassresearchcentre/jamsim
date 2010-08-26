package org.omancode.r;

import java.io.IOException;
import java.util.Map;

/**
 * An R command constructed from a set of GUI elements.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface RUICommand {


	/**
	 * Generate an R command text string from the UI elements. 
	 * 
	 * @param uiElements UI elements
	 * @return R command text string
	 */
	String generateCmdText(Map<String, Object> uiElements);
	
	/**
	 * Get {@link RUICommand} name.
	 * 
	 * @return name
	 */
	String getName();

	/**
	 * JavaBuilder YAML UI definition that defines the GUI.
	 * 
	 * @return YAML UI definition
	 * @throws IOException
	 *             if problem reading YAML
	 */
	String getYAML() throws IOException;

	/**
	 * Does this command produce a chart?
	 * 
	 * @return {@code true} if this command produce a chart.
	 */
	boolean isChart();

}