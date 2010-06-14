package org.jamsim.ascape.navigator;

import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.MutableTreeNode;

import org.ascape.model.AscapeObject;
import org.ascape.runtime.swing.navigator.DefaultTreeBuilder;
import org.ascape.runtime.swing.navigator.PeformActionOnSelectedNode;
import org.ascape.runtime.swing.navigator.RightClickPopupMenu;
import org.ascape.runtime.swing.navigator.TreeBuilder;
import org.jamsim.ascape.MicroSimScape;

/**
 * {@link TreeBuilder} that creates {@link MicroSimScapeNode} for
 * {@link MicroSimScape} model elements, provides a
 * {@link PeformActionOnSelectedNode} selection listener and *** Same as
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

		// add listener that calls actionPerformed on the selected node
		tsls.add(new PeformActionOnSelectedNode());

		return tsls;
	}

	@Override
	public Set<MouseListener> getMouseListeners() {
		Set<MouseListener> mls =
				new HashSet<MouseListener>();

		mls.addAll(super.getMouseListeners());

		// add listener that calls actionPerformed on the selected node
		mls.add(new RightClickPopupMenu());

		return mls;
	}

}
