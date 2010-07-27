package org.omancode.swing;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * A {@link DefaultMutableTreeNode} that lazily creates children when they are
 * needed.
 * <p>
 * Creates children (via abstract method {@link #createChildren()}) whenever
 * {@link #getChildCount()} is called. This can happen when the following are
 * called:
 * <ul>
 * <li>{@link DefaultTreeModel#nodeStructureChanged(javax.swing.tree.TreeNode)}
 * <li>{@link DefaultMutableTreeNode#add(javax.swing.tree.MutableTreeNode)}
 * <li> {@link JTree#expandPath(javax.swing.tree.TreePath)}
 * </ul>
 * The overall effect is that children are created as nodes are needed, i.e.:
 * when expanded. In addition, more than what is expanded is loaded,
 * particularly the children of the last unexpanded node at the currently
 * expanded level of the hierarchy.
 * 
 * Adapted from com.jidesoft.plaf.basic.LazyMutableTreeNode.
 */
public abstract class LazyMutableTreeNode extends DefaultMutableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5234855334034342509L;
	
	private transient boolean loaded = false;

    /**
     * Creates a tree node that has no parent and no children, but which
     * allows children.
     */
	public LazyMutableTreeNode() {
		super();
	}

    /**
     * Creates a tree node with no parent, no children, but which allows 
     * children, and initializes it with the specified user object.
     * 
     * @param userObject an Object provided by the user that constitutes
     *                   the node's data
     */
	public LazyMutableTreeNode(Object userObject) {
		super(userObject);
	}

    /**
     * Creates a tree node with no parent, no children, initialized with
     * the specified user object, and that allows children only if
     * specified.
     * 
     * @param userObject an Object provided by the user that constitutes
     *        the node's data
     * @param allowsChildren if true, the node is allowed to have child
     *        nodes -- otherwise, it is always a leaf node
     */
	public LazyMutableTreeNode(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
	}

	@Override
	public int getChildCount() {
		synchronized (this) {
			if (!loaded) {
				loaded = true;
				createChildren();
			}
		}
		return super.getChildCount();
	}

	/**
	 * Remove all children.
	 */
	public void clear() {
		removeAllChildren();
		loaded = false;
	}

	/**
	 * Have children been created?
	 * 
	 * @return {@code true} if children have already been created.
	 */
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * Create children. Only called when children need to be created.
	 */
	protected abstract void createChildren();

}
