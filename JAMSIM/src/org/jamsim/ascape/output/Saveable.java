package org.jamsim.ascape.output;

import java.io.IOException;

/**
 * A Saveable object.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface Saveable {

	/**
	 * Save to a CSV file in the specified directory.
	 * 
	 * @param directory directory.
	 * @throws IOException if there is a problem saving
	 */
	void saveToCSV(String directory) throws IOException;

}
