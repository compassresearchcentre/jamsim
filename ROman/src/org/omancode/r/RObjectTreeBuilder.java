package org.omancode.r;

import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Builds a tree that represents the objects in an R environment.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 */
public class RObjectTreeBuilder {

	private final RInterfaceHL rInterface;
	private final JTree tree = new JTree();
	private final DefaultMutableTreeNode root =
			new DefaultMutableTreeNode("R");
	private final DefaultTreeModel model = new DefaultTreeModel(root);

	/**
	 * Construct a {@link RObjectTreeBuilder} that will supplies a tree that
	 * represents all visible object in the R global environment.
	 * 
	 * @param rInterface
	 *            r interface
	 * @throws RInterfaceException
	 *             if problem getting objects
	 */
	public RObjectTreeBuilder(RInterfaceHL rInterface)
			throws RInterfaceException {
		this.rInterface = rInterface;
		addNodes(root, getObjects());
		tree.setModel(model);
	}

	/**
	 * Get the JTree for display.
	 * 
	 * @return jtree
	 */
	public JTree getTree() {
		return tree;
	}

	/**
	 * Create {@link RObjectNode} from map of strings underneath parent node.
	 * 
	 * @param parent
	 *            parent node
	 * @param nodes
	 *            map of strings to create {@link RObjectNode}s from.
	 */
	public final void addNodes(DefaultMutableTreeNode parent,
			Map<String, String> nodes) {

		for (Map.Entry<String, String> entry : nodes.entrySet()) {
			RObjectNode node =
					new RObjectNode(this, entry.getKey(), entry.getValue());
			model.insertNodeInto(node, parent, parent.getChildCount());
		}

	}

	private Map<String, String> getObjects() throws RInterfaceException {
		String cmd = ".getObjects()";
		Map<String, String> namedRObjects =
				rInterface.evalReturnNamedStrings(cmd);
		return namedRObjects;
	}

	/**
	 * Get the parts that make up an R object, or an empty map if the R object
	 * has no parts.
	 * 
	 * @param rname r object name
	 * @return map of parts
	 */
	public Map<String, String> getParts(String rname) {
		String expr = ".getParts(" + rname + ")";

		try {
			Map<String, String> namedRObjects =
					rInterface.evalReturnNamedStrings(expr);

			return namedRObjects;
		} catch (RInterfaceException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get info for a particular R object. Info returned depends on the R object
	 * class and could be dimensions/length etc.
	 * 
	 * @param rname
	 *            r object name
	 * @return info
	 */
	public String getInfo(String rname) {
		String expr = ".getInfo(" + rname + ")";

		try {
			String info = rInterface.evalReturnString(expr);

			return info;
		} catch (RInterfaceException e) {
			throw new RuntimeException(e);
		}
	}

}
