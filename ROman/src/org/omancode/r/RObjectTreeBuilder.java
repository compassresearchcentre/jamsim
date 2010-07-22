package org.omancode.r;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;

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
	 * Create {@link RObjectNode}s underneath parent node from array of nodes.
	 * 
	 * @param parent
	 *            parent node
	 * @param nodes
	 *            map of strings to create {@link RObjectNode}s from.
	 */
	public final void addNodes(DefaultMutableTreeNode parent,
			RObjectNode[] nodes) {

		for (RObjectNode node : nodes) {
			model.insertNodeInto(node, parent, parent.getChildCount());
		}

	}

	private RObjectNode[] getObjects() throws RInterfaceException {
		String expr = ".getObjects()";
		return getNodes(expr);
	}

	/**
	 * Get the parts that make up an R object, or an empty array if the R object
	 * has no parts.
	 * 
	 * @param rname
	 *            r object name
	 * @return array of nodes created from parts
	 * @throws RInterfaceException
	 *             if problem getting part information from R
	 */
	public RObjectNode[] getParts(String rname) throws RInterfaceException {
		String expr = ".getParts(" + rname + ")";
		return getNodes(expr);
	}

	/**
	 * Execute an R command that returns a list containing the named {@code chr}
	 * vectors {@code names}, {@code class}, {@code info}.
	 * 
	 * @param expr
	 *            r expression
	 * @return array of nodes created from expr
	 * @throws RInterfaceException
	 *             if problem evaluating expr
	 */
	private RObjectNode[] getNodes(String expr) throws RInterfaceException {
		RList rlist = rInterface.parseEvalTryAsRList(expr);

		if (rlist == null) {
			return new RObjectNode[0];
		}

		String[] names = ((REXPString) rlist.get("names")).asStrings();
		String[] klass = ((REXPString) rlist.get("class")).asStrings();
		String[] info = ((REXPString) rlist.get("info")).asStrings();

		RObjectNode[] nodes = new RObjectNode[names.length];

		for (int i = 0; i < names.length; i++) {
			nodes[i] = new RObjectNode(this, names[i], klass[i], info[i]);
		}

		return nodes;
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
