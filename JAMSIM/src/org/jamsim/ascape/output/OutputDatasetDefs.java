package org.jamsim.ascape.output;

import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.ScapeData;
import org.jamsim.ascape.r.ScapeRInterface;

/**
 * Defines a series of output datasets to be attached to a {@link MicroSimScape}
 * .
 * 
 * @param <D>
 *            a scape data class that defines data external to the scape for use
 *            by agents, and for loading agents. 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface OutputDatasetDefs<D extends ScapeData> {

	/**
	 * Attach the output datasets defined by this instance to the scape.
	 * 
	 * @param msscape
	 *            scape to attach output datasets to
	 * @param scapeR
	 *            scape R interface. May be required to produce output datasets.
	 * @return information string to be output to the console
	 */
	String attachOutputDatasets(MicroSimScape<D> msscape,
			ScapeRInterface scapeR);

}
