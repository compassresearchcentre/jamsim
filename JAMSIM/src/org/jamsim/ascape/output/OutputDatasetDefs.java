package org.jamsim.ascape.output;

import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.r.ScapeRInterface;

/**
 * Defines a series of output datasets to be attached to a {@link MicroSimScape}
 * .
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface OutputDatasetDefs {

	/**
	 * Attach the output datasets defined by this instance to the scape.
	 * 
	 * @param msscape
	 *            scape to attach output datasets to
	 * @param scapeR
	 *            scape R interface. May be required to produce output datasets.
	 * @return information string to be output to the console
	 */
	String attachOutputDatasets(MicroSimScape<?> msscape,
			ScapeRInterface scapeR);

}
