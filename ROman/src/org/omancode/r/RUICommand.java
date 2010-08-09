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
	 * Get {@link RUICommand} name.
	 * 
	 * @return name
	 */
	String getName();

	/**
	 * Get name of R variable the command is being applied to.
	 * 
	 * @return variable name
	 */
	String getVariableName();

	/**
	 * Sets the UI elements. Must be called before {@link #getVariableName()}
	 * and {@link #getRCommand()}.
	 * 
	 * @param uiElements
	 *            UI elements
	 */
	void setUIElements(Map<String, Object> uiElements);

	/**
	 * Command to execute in R. Generated from uiElements supplied by
	 * {@link #setUIElements(Map)}.
	 * 
	 * @return r command
	 */
	String getRCommand();

	/**
	 * JavaBuilder YAML UI definition that defines the GUI.
	 * 
	 * @return YAML UI definition
	 * @throws IOException
	 *             if problem reading YAML
	 */
	String getYAML() throws IOException;

}