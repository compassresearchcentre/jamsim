package org.jamsim.ascape.ui;

import org.omancode.swing.DoubleCellRenderer;

/**
 * Utility functions related to Ascape Swing GUI components provided by JAMSIM.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class UIUtil {

	private UIUtil() {
		
	}
	
	/**
	 * Create a {@link DoubleCellRenderer}.
	 * 
	 * @return double cell renderer
	 */
	public static DoubleCellRenderer getDoubleCellRenderer() {
		return new DoubleCellRenderer(2);
	}
}
