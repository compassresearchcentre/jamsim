package org.jamsim.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.MutableTreeNode;

import org.ascape.model.AscapeObject;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.navigator.RecordedMicroSimTreeBuilder;

/**
 * {@link org.ascape.runtime.swing.navigator.TreeBuilder} that records nodes 
 * created so they can be fetched afterwards.
 * <p>
 * NB: Must be a subclass of the recorded TreeBuilder, rather than a wrapper,
 * because created nodes may use the TreeBuilder they are built by, which would
 * bypass the wrapper.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class JAMSIMTreeBuilder extends RecordedMicroSimTreeBuilder {

	
	private final Map<AscapeObject, MutableTreeNode> cache =
			new HashMap<AscapeObject, MutableTreeNode>();

	@Override
	public MutableTreeNode createTreeNode(AscapeObject modelElement) {
		MutableTreeNode newNode;
		if (modelElement instanceof MicroSimScape<?>) {
			MicroSimScape<?> msScape = (MicroSimScape<?>) modelElement;
			newNode = new TreeRootNode(msScape, this);
		} else {
			newNode = super.createTreeNode(modelElement);
		}

		cache.put(modelElement, newNode);
		return newNode;
	}

	
	/**
	 * Return the last created node for a given model element.
	 * 
	 * @param modelElement
	 *            model element to return node for
	 * @return the node last created for {@code modelElement}.
	 */
	public MutableTreeNode getCreatedTreeNode(AscapeObject modelElement) {
		return cache.get(modelElement);
	}
	
	
}
