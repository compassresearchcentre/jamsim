package org.jamsim.ascape;

import java.io.IOException;

import org.jamsim.io.FileLoader;

/**
 * Factory method interface for creating a {@link ScapeData} object.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * @param <D> scape data type
 */
public interface ScapeDataCreator<D extends ScapeData> {

	/**
	 * Create a {@link ScapeData} object.
	 * 
	 * @param loader
	 *            file loader
	 * @throws IOException
	 *             if problem loading scape data
	 * @return scape data
	 */
	D create(FileLoader loader) throws IOException;

}
