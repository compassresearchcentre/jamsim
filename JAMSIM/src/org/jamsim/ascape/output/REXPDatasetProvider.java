package org.jamsim.ascape.output;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;

import org.omancode.r.RFaceException;
import org.omancode.r.types.CBuildFromREXP;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPReference;

/**
 * Provides a pre-existing {@link REXP} as a dataset.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class REXPDatasetProvider implements OutputDatasetProvider {

	private final String name;
	private final REXP rexp;

	/**
	 * Master {@link REXPDatasetProvider} constructor.
	 * 
	 * @param name dataset name
	 * @param rexp rexp
	 */
	public REXPDatasetProvider(String name, REXP rexp) {
		this.name = name;
		this.rexp = rexp;
	}

	/**
	 * Construct {@link REXPDatasetProvider} from a {@link REXPReference},
	 * resolving the reference in the process.
	 * 
	 * @param name dataset name
	 * @param rexpRef rexpRef
	 */
	public REXPDatasetProvider(String name, REXPReference rexpRef) {
		this(name, rexpRef.resolve());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CDataCacheContainer getOutputDataset(int run)
			throws CDataGridException {

		try {
			return new CDataCacheContainer(new CBuildFromREXP(rexp, name));
		} catch (RFaceException e) {
			throw new CDataGridException(e.getMessage(), e);
		}
	}

}
