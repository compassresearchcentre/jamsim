package org.jamsim.example;

import org.jamsim.ascape.RunAscape;

/**
 * Start Ascape with {@link JEMScape}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class RunJEM {

	private RunJEM() {
		// no instantiation
	}

	/**
	 * Start Ascape with {@link JEMScape}.
	 * 
	 * @param args
	 *            not used
	 */
	public static void main(String[] args) {
		RunAscape.start(JEMScape.class);
	}

}