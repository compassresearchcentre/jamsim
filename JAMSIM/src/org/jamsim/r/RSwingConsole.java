package org.jamsim.r;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.IOException;

import javax.swing.JComponent;

import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

import bsh.util.JConsole;

/**
 * Swing GUI Console that interacts with the main R REPL (Read-eval-print loop).
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 */
public class RSwingConsole extends JComponent implements RMainLoopCallbacks {
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = -8885521123731152442L;

	/*
	 * private static final String NEWLINE =
	 * System.getProperty("line.separator");
	 */
	private final JConsole console = new JConsole();

	private final BufferedReader keyboard =
			new BufferedReader(console.getIn());

	private boolean promptVisible = false;

	private String rPrompt;
	
	/**
	 * Create {@link RSwingConsole} and display the prompt.
	 */
	public RSwingConsole() {
		this(true);
	}

	/**
	 * Create {@link RSwingConsole}.
	 * 
	 * @param startWithPrompt
	 *            show the prompt once R is loaded. If you want to load extra
	 *            packages and libraries after R is loaded you will want this to
	 *            be {@code false} and then call
	 * 
	 */
	public RSwingConsole(boolean startWithPrompt) {
		promptVisible = startWithPrompt;

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
	 * RMainLoopCallbacks ------------------
	 * 
	 * These functions are called from R native code during execution of the
	 * main R REPL (Read-eval-print loop). They cause R to block while they
	 * execute.
	 */

	@Override
	public void rWriteConsole(Rengine re, String text, int oType) {
		console.print(text, Color.BLUE);
	}

	@Override
	public void rBusy(Rengine re, int which) {
		// TODO
		// System.out.println("rBusy("+which+")");
	}

	/**
	 * Set the visibility of the console prompt.
	 * 
	 * @param promptVisible
	 *            if {@code true} the console prompt will show when R is ready
	 *            for input (i.e: when R cals
	 *            {@link #rReadConsole(Rengine, String, int)}).
	 */
	public void setPromptVisible(boolean promptVisible) {
		this.promptVisible = promptVisible;
	}

	private void prompt(String prompt) {
		console.print(prompt, Color.RED);
	}
	
	public void printPrompt() {
		prompt(rPrompt);
	}

	/**
	 * Called by the main R REPL (Read-eval-print loop) to receive input.
	 * 
	 * R is blocked when this is called. This means that any R Graphic Device
	 * windows (unless they are JavaGD windows) will not update or respond to
	 * events and will appear frozen while this method waits for and/or
	 * processes input.
	 * 
	 *@param re
	 *            calling engine
	 *@param prompt
	 *            prompt to be displayed at the console prior to user's input
	 *@param addToHistory
	 *            flag telling the handler whether the input should be
	 *            considered for adding to history (!=0) or not (0)
	 *@return user's input to be passed to R for evaluation
	 */
	@Override
	public String rReadConsole(Rengine re, String prompt, int addToHistory) {
		rPrompt = prompt;
		if (promptVisible) {
			prompt(prompt);
		}

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

	@Override
	public void rShowMessage(Rengine re, String message) {
		rWriteConsole(re, "rShowMessage \"" + message + "\"", 1);
	}

	@Override
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

	@Override
	public void rFlushConsole(Rengine re) {
		// not implemented
	}

	@Override
	public void rLoadHistory(Rengine re, String filename) {
		// not implemented
	}

	@Override
	public void rSaveHistory(Rengine re, String filename) {
		// not implemented
	}
}
