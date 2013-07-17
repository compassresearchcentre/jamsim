package org.jamsim.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.ascape.runtime.swing.navigator.TreeBuilder;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.navigator.MicroSimScapeNode;
import org.jamsim.ascape.navigator.SaveablePanelViewGraphNode;
import org.jamsim.ascape.navigator.SaveablePanelViewNode;
import org.jamsim.ascape.navigator.SubFolderNode;
import org.jamsim.ascape.output.REXPDatasetProvider;
import org.jamsim.ascape.r.PanelViewDatasetProvider;
import org.jamsim.ascape.r.PanelViewJGraphicsDevice;
import org.rosuda.javaGD.JavaGD;

public class TreeRootNode extends MicroSimScapeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -77656913756499118L;

	private final MicroSimScape<?> scape;
	private final DefaultTreeModel treeModel;
	
	private final Map<String, SubFolderNode> tableNodeMap =
			new HashMap<String, SubFolderNode>();

	
	public TreeRootNode(MicroSimScape<?> scape, TreeBuilder treeBuilder) {
		super(scape, treeBuilder);
		this.scape = scape;
		this.treeModel = treeBuilder.getTreeModel();
	}

	/**
	 * Add a saveable node for a {@link REXPDatasetProvider} under the folder
	 * and subfolder specified. Creates the folder and/or subfolder if it
	 * doesn't already exist.
	 * 
	 * Convenience method that can be called from R.
	 * 
	 * @param provider
	 *            provider
	 * @param parentName
	 *            name of parent to create node under. Created if it doesn't
	 *            already exist.
	 * @param path
	 *            a path to a sub folder node, eg: "Base/Means" which represents
	 *            the folder Means under the folder Base, or just "Base" which
	 *            will add to the folder Base, or {@code null} to add directly
	 *            under parentName.
	 */
	public void addSaveableNode(REXPDatasetProvider provider,
			String parentName, String path) {
		PanelViewDatasetProvider pvprovider = new PanelViewDatasetProvider(
				provider);

		SaveablePanelViewNode newNode = new SaveablePanelViewNode(pvprovider,
				pvprovider);

		addNodeToOnDemandParent(newNode, parentName, path);
	}

	/**
	 * Add a node to a folder and optionally a subfolder, creating that folder
	 * if it doesn't already exist.
	 * 
	 * @param newNode
	 *            new node to add
	 * @param parentName
	 *            name of parent to create node under. Created if it doesn't
	 *            already exist.
	 * @param path
	 *            a path to a sub folder node, eg: "Base/Means" which represents
	 *            the folder Means under the folder Base, or just "Base" which
	 *            will add to the folder Base, or {@code null} to add directly
	 *            under folderName.
	 */
	public final void addNodeToOnDemandParent(MutableTreeNode newNode,
			String parentName, String path) {

		SubFolderNode folderNode = tableNodeMap.get(parentName);

		if (folderNode == null) { // create on demand
			folderNode = new SubFolderNode(parentName, scape, treeModel);
			treeModel.insertNodeInto(folderNode, this, this.getChildCount());
			tableNodeMap.put(parentName, folderNode);
		}

		folderNode.addChildNode(newNode, path);

	}

	/**
	 * Add a lazy node for a {@link JavaGD} graphics device.
	 * 
	 * @param rPlotCmd
	 *            R command that will plot the graph to be displayed
	 * @param name
	 *            Name of the node
	 * @param path
	 *            a path to a sub folder node, eg: "Base/Means" which represents
	 *            the folder Means under the folder Base, or just "Base" which
	 *            will add to the folder Base, or {@code null} to add directly
	 *            under the root node.
	 */
	public void addLazyJGDNode(String rPlotCmd, String name, String path) {

		PanelViewJGraphicsDevice provider = new PanelViewJGraphicsDevice(
				scape.getScapeRInterface(), rPlotCmd, name, scape);

		SaveablePanelViewGraphNode lazyJGDNode = new SaveablePanelViewGraphNode(
				provider);

		String parent = path.substring(0, path.indexOf("/"));
		String subpath = path.substring(path.indexOf("/") + 1, path.length());

		addNodeToOnDemandFolder(lazyJGDNode, parent, subpath);
	}
}
