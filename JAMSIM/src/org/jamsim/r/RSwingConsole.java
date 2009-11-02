package org.jamsim.r;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import javax.swing.JComponent;

import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

import bsh.util.JConsole;

public class RSwingConsole extends JComponent implements RMainLoopCallbacks {
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = -8885521123731152442L;

	private static final String newline =
			System.getProperty("line.separator");

	private JConsole console = new JConsole();

	private Reader keyboardReader = console.getIn();

	private BufferedReader keyboard = new BufferedReader(console.getIn());

	public RSwingConsole() {

		// set the layout manager to BorderLayout so that when this
		// component's container is resized, so are the component's
		// within this component
		setLayout(new BorderLayout());

		add(console);

	}

	@Override
	public void setFont(Font font) {
		console.setFont(font);
	}

	/**
	 * RMainLoopCallbacks
	 * 
	 * These functions are called from R native code during execution of the
	 * main R REPL (Read-eval-print loop). They cause R to block while they
	 * execute.
	 */

	public void rWriteConsole(Rengine re, String text, int oType) {
		console.print(text, Color.BLUE);
	}

	public void rBusy(Rengine re, int which) {
		// TODO
		// System.out.println("rBusy("+which+")");
	}

	private void prompt(String prompt) {
		console.print(prompt, Color.RED);
	}

	/**
	 * Called by the main R REPL (Read-eval-print loop) to receive input.
	 * 
	 * R is blocked when this is called. This means that any R Graphic Device
	 * windows (unless they are JavaGD windows) will not update or respond to
	 * events and will appear frozen while this method waits for and/or
	 * processes input.
	 * 
	 */
	public String rReadConsole(Rengine re, String prompt, int addToHistory) {
		prompt(prompt);

		String input = null;
		try {

			/*
			 * //loop whilst there isn't a line //ready to be read so that
			 * synchronized(this) { while (!keyboard.ready()) { //tell the
			 * Rengine it can run //it's event handlers, such as updating
			 * //graphics devices try { wait(100) ; re.rniIdle(); } catch
			 * (InterruptedException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } } }
			 * 
			 * 
			 * while (!keyboard.ready()) { try{ wait(100) ; re.rniIdle(); }
			 * catch( InterruptedException e){} }
			 */

			// read the keyboard line
			// this will not block because we know there
			// is something waiting to be read
			input = keyboard.readLine();

			// JConsole returns ";\n" if you hit enter.
			// Remove the ;
			if (input == null || input.equals(";")) {
				input = "\n";
			} else {
				// add a newline to the input to prevent
				// a + prompt from the R REPL
				input = input + "\n";
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return input;
	}

	public void rShowMessage(Rengine re, String message) {
		rWriteConsole(re, "rShowMessage \"" + message + "\"", 1);
	}

	public String rChooseFile(Rengine re, int newFile) {
		FileDialog fd =
				new FileDialog(new Frame(), newFile == 0 ? "Select a file"
						: "Select a new file", newFile == 0 ? FileDialog.LOAD
						: FileDialog.SAVE);
		fd.show();
		String res = null;
		if (fd.getDirectory() != null) {
			res = fd.getDirectory();
		}
		if (fd.getFile() != null) {
			res = res == null ? fd.getFile() : res + fd.getFile();
		}
		return res;
	}

	public void rFlushConsole(Rengine re) {
	}

	public void rLoadHistory(Rengine re, String filename) {
	}

	public void rSaveHistory(Rengine re, String filename) {
	}
}
