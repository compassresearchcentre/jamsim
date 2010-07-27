package org.jamsim.ascape.ui;

import javax.swing.JPanel;
import javax.swing.JTree;

import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

public class AnalysisPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1697208275538033643L;

	private BuildResult result;
	private final JTree rObjects;

	public AnalysisPanel(JTree rObjects) {
		this.rObjects = rObjects;
		result = SwingJavaBuilder.build(this);
	}
}
