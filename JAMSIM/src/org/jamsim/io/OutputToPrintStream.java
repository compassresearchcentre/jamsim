package org.jamsim.io;

import java.io.PrintStream;

/**
 * Implementation of the {@link Output} interface that prints to a
 * {@link PrintStream}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class OutputToPrintStream implements Output {

	private final PrintStream stream;

	/**
	 * Construct printing to {@link System#out}.
	 */
	public OutputToPrintStream() {
		this(System.out);
	}

	/**
	 * Construct printing to supplied print stream.
	 * 
	 * @param stream print stream
	 */
	public OutputToPrintStream(PrintStream stream) {
		this.stream = stream;
	}

	@Override
	public void print(String message) {
		stream.print(message);
	}

	@Override
	public void println(String message) {
		stream.println(message);
	}

}
