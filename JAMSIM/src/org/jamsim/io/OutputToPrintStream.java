package org.jamsim.io;

import java.io.PrintStream;

public class OutputToPrintStream implements Output {

	private final PrintStream stream;

	public OutputToPrintStream() {
		this(System.out);
	}

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
