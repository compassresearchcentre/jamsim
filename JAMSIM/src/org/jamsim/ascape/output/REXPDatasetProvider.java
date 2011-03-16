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
	private final CDataCacheContainer container;

	/**
	 * Master {@link REXPDatasetProvider} constructor.
	 * 
	 * @param name
	 *            dataset name
	 * @param rexp
	 *            rexp
	 * @throws RFaceException
	 *             if cannot created {@link CDataCacheContainer} from
	 *             {@link REXP}
	 */
	public REXPDatasetProvider(String name, REXP rexp) throws RFaceException {
		this.name = name;
		
		// since rexp is invariable, we create container
		// now and don't store rexp
		try {
			this.container =
					new CDataCacheContainer(new CBuildFromREXP(rexp, name));
		} catch (CDataGridException e) {
			throw new RFaceException(e.getMessage(), e);
		}
	}

	/**
	 * Construct {@link REXPDatasetProvider} from a {@link REXPReference},
	 * resolving the reference in the process.
	 * 
	 * @param name
	 *            dataset name
	 * @param rexpRef
	 *            rexpRef
	 * @throws RFaceException
	 *             if cannot created {@link CDataCacheContainer} from
	 *             {@link REXPReference}
	 */
	public REXPDatasetProvider(String name, REXPReference rexpRef)
			throws RFaceException {
		this(name, rexpRef.resolve());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CDataCacheContainer getOutputDataset(int run)
			throws CDataGridException {
		return container;
	}

}
