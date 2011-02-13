package org.jamsim.ascape;

import org.ascape.model.Scape;

/**
 * Start Ascape with the supplied scape.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class RunAscape {

	private RunAscape() {
		// no instantiation
	}

	/**
	 * Stub to call org.ascape.runtime.swing.SwingRunner.main with a scape.
	 * <p>
	 * If the system property {@code sysInfo} is set via the -D command line
	 * option, eg: {@code -D:sysInfo=on} then output basic system environment
	 * info, such as java.library.path, path, classpath, and R_HOME before
	 * starting scape.
	 * </p>
	 * 
	 * @param scapeClass
	 *            the scape to start
	 */
	public static void start(Class<? extends Scape> scapeClass) {

		if (System.getProperty("sysInfo") != null) {

			// output diagnosis information
			System.out.format("%s=%s%n", "java.runtime.version",
					System.getProperty("java.runtime.version"));
			System.out.format("%s=%s%n", "java.library.path",
					System.getProperty("java.library.path"));
			System.out.format("%s=%s%n", "Path", System.getenv().get("Path"));
			System.out.format("%s=%s%n", "R_HOME",
					System.getenv().get("R_HOME"));
			System.out.format("%s=%s%n", "java.class.path",
					System.getProperty("java.class.path"));
			
		}

		String[] ascapeArgs = { scapeClass.getName() };

		org.ascape.runtime.swing.SwingRunner.main(ascapeArgs);
	}

}