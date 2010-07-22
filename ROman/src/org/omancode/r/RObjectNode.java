package org.omancode.r;

import javax.swing.tree.MutableTreeNode;

import org.apache.commons.lang.NotImplementedException;
import org.omancode.swing.LazyMutableTreeNode;

/**
 * Tree node that represents an object in R. Provides basic information about
 * the object.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 */
public class RObjectNode extends LazyMutableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -490906438921398129L;

	private final RObjectTreeBuilder rotb;
	private final String name;
	private final String rClass;
	private String rname;
	private String info;

	/**
	 * Construct {@link RObjectNode}.
	 * 
	 * @param rotb
	 *            {@link RObjectTreeBuilder}
	 * @param name
	 *            display name
	 * @param rClass
	 *            R class name of this object, eg: data.frame, list, matrix etc.
	 */
	public RObjectNode(RObjectTreeBuilder rotb, String name, String rClass) {
		super(name);
		this.rotb = rotb;
		this.rClass = rClass;
		this.name = name;
	}

	@Override
	public void setParent(MutableTreeNode newParent) {
		super.setParent(newParent);
		this.rname = buildRName();
		this.info = rotb.getInfo(rname);
	}

	@Override
	protected void createChildren() {
		rotb.addNodes(this, rotb.getParts(getRName()));
	}

	@Override
	public String toString() {
		return name + "\t (" + rClass + ")"
				+ ((info == null) ? "" : " " + info);
	}

	/**
	 * Get the real R name of this object, e.g.: children$accom[[1]].
	 * 
	 * @return r name
	 */
	public String getRName() {
		return rname;
	}

	/**
	 * Build the real R name of this object, e.g.: children$accom[[1]].
	 * 
	 * @return r name
	 */
	private String buildRName() {
		if (!(parent instanceof RObjectNode)) {
			return name;
		}

		RObjectNode parentRON = (RObjectNode) parent;
		String parentKlass = parentRON.getRClass();
		String parentName = parentRON.getRName();

		String result;

		if ("data.frame".equals(parentKlass) || "list".equals(parentKlass)) {
			result = parentName + listPartAsIndex(name);
		} else if ("matrix".equals(parentKlass)
				|| "table".equals(parentKlass)) {
			result = parentName + matrixPartAsIndex(name);
		} else {
			throw new NotImplementedException(
					"Cannot generate R name for parent class " + parentKlass);
		}

		return result;

	}

	public static String listPartAsIndex(String part) {
		if (part.startsWith("[")) {
			return part;
		}

		return "$" + part;
	}

	public static String matrixPartAsIndex(String part) {
		if (part.startsWith("[")) {
			return part;
		}

		return "[,\"" + part + "\"]";
	}

	/**
	 * Get the display name of this R object.
	 * 
	 * @return display name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the class of this R object.
	 * 
	 * @return class
	 */
	public String getRClass() {
		return rClass;
	}

}
