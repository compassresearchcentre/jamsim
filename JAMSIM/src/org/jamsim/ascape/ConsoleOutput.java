package org.jamsim.ascape;

import org.ascape.view.nonvis.ConsoleOutView;
import org.omancode.io.Output;

/**
 * Adapt the Ascape {@link ConsoleOutView} to the {@link Output} interface.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class ConsoleOutput implements Output {

	/**
	 * Ascape console used to provide printing services.
	 */
	private final ConsoleOutView console;

	/**
	 * Construct an output interface to an Ascape console.
	 * 
	 * @param console ascape console
	 */
	public ConsoleOutput(ConsoleOutView console) {
		this.console = console;
	}

	/**
	 * Print a message to the Ascape console.
	 * 
	 * @param message
	 *            string to print
	 */
	@Override
	public final void print(String message) {
		console.print(message);
	}

	/**
	 * Print a message to the Ascape console with a line feed.
	 * 
	 * @param message
	 *            string to print
	 */
	@Override
	public final void println(String message) {
		console.println(message);
	}

}
