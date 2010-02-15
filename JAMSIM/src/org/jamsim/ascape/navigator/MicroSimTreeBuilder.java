package org.jamsim.ascape.navigator;

import java.util.HashSet;
import java.util.Set;

import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.MutableTreeNode;

import org.ascape.model.AscapeObject;
import org.ascape.runtime.swing.navigator.DefaultTreeBuilder;
import org.ascape.runtime.swing.navigator.PeformActionOnSelectedNode;
import org.ascape.runtime.swing.navigator.TreeBuilder;
import org.jamsim.ascape.MicroSimScape;

/**
 * {@link TreeBuilder} that creates {@link MicroSimScapeNode} for
 * {@link MicroSimScape} model elements, and the same as
 * {@link DefaultTreeBuilder} for everything else.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class MicroSimTreeBuilder extends DefaultTreeBuilder {

	@Override
	public MutableTreeNode createTreeNode(AscapeObject modelElement) {
		if (modelElement instanceof MicroSimScape<?>) {
			MicroSimScape<?> msScape = (MicroSimScape<?>) modelElement;
			return new MicroSimScapeNode(msScape, this);
		} else {
			return super.createTreeNode(modelElement);
		}
	}

	@Override
	public Set<TreeSelectionListener> getTreeSelectionListeners() {
		Set<TreeSelectionListener> tsls =
				new HashSet<TreeSelectionListener>();

		tsls.addAll(super.getTreeSelectionListeners());

		// add listener to open table node when clicked on
		tsls.add(new PeformActionOnSelectedNode());

		return tsls;
	}

}
