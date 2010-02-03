package org.jamsim.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.ascape.runtime.swing.SwingEnvironment;
import org.ascape.runtime.swing.ViewFrameBridge;
import org.ascape.util.swing.PanelViewUtil.PanelViewNoStall;
import org.gjt.sp.jedit.IPropertyManager;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.buffer.BufferAdapter;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.util.IOUtilities;
import org.gjt.sp.util.Log;

public class JEditPanelView extends PanelViewNoStall {

	private PanelViewListener pvl;
	private final File file;
	private final FileBuffer buffer;
	private final TextArea jedit;

	public JEditPanelView(String name, File file) throws IOException {
		this.file = file;

		// create a new empty PanelView
		setName(name);
		setLayout(new BorderLayout());

		// create text area
		jedit = createTextArea();

		// create buffer, add to text area
		buffer = createRFileBuffer();
		jedit.setBuffer(buffer);

		// add text area to panel view
		add(jedit, BorderLayout.CENTER);
	}

	public void addToSwingEnvironment() {
		// add panel view directly instead of via scape listener
		SwingEnvironment.DEFAULT_ENVIRONMENT.createFrame(this);
		build();

		// get the frame
		Container pvFrameImp = getViewFrame().getFrameImp();

		// The pvFrameImp type will depend on what is selected by
		// the ViewFrameBridge.selectFrameImp method.
		// We are expecting the user to be using the Swing MDI
		// which will return a JInternalFrame
		if (pvFrameImp instanceof JInternalFrame) {

			JInternalFrame pvFrame = (JInternalFrame) pvFrameImp;

			// when the frame closes, check if the buffer is dirty
			// and offer to save changes
			pvFrame.addInternalFrameListener(new InternalFrameAdapter() {

				public void internalFrameClosing(InternalFrameEvent e) {
					JInternalFrame iFrame = e.getInternalFrame();

					int actionOnClose = JInternalFrame.DISPOSE_ON_CLOSE;
					if (buffer.isDirty()) {
						int n =
								JOptionPane.showConfirmDialog(iFrame,
										"Save modifications to " + name,
										"Save changes?",
										JOptionPane.YES_NO_CANCEL_OPTION);

						if (n == JOptionPane.CANCEL_OPTION) {
							actionOnClose =
									JInternalFrame.DO_NOTHING_ON_CLOSE;
						} else if (n == JOptionPane.YES_OPTION) {

							try {
								buffer.save();
							} catch (IOException e1) {
								throw new RuntimeException();
							}
						}
					}
					iFrame.setDefaultCloseOperation(actionOnClose);
					if (actionOnClose == JInternalFrame.DISPOSE_ON_CLOSE) {
						fireClosing();
					}
				}

			});

		} else {
			throw new RuntimeException("Unknown frame type "
					+ pvFrameImp.getClass().getCanonicalName());
		}
	}

	private void fireClosing() {
		if (pvl != null) {
			pvl.panelViewClosing();
		}
	}

	
	private FileBuffer createRFileBuffer() throws IOException {

		Mode mode = new Mode("r");
		mode.setProperty("file", "r.xml");
		ModeProvider.instance.addMode(mode);

		FileBuffer buffer = new FileBuffer(file);
		buffer.setMode(mode);

		BufferAdapter listener = new BufferAdapter() {

			/**
			 * Change the modified status of the frame's title. Fired after
			 * insertion, removal, undo, redo operations.
			 */
			@Override
			public void transactionComplete(JEditBuffer buffer) {
				updateFrameTitle();
			}
		};

		buffer.addBufferListener(listener);

		return buffer;
	}

	private void updateFrameTitle() {
		// get the frame
		ViewFrameBridge pvBridge = getViewFrame();

		String modified = buffer.isDirty() ? " * " : "";
		pvBridge.setTitle(name + modified);
	}
	
	public static StandaloneTextArea createTextArea() {
		final Properties props = new Properties();
		props.putAll(loadProperties("/org/gjt/sp/jedit/jedit_keys.props"));
		props.putAll(loadProperties("/org/gjt/sp/jedit/jedit.props"));
		props.putAll(loadProperties("/org/gjt/sp/jedit/properties"));
		StandaloneTextArea textArea =
				new StandaloneTextArea(new IPropertyManager() {
					public String getProperty(String name) {
						return props.getProperty(name);
					}
				});
		textArea.getBuffer().setProperty("folding", "explicit");
		return textArea;
	}

	private static Properties loadProperties(String fileName) {
		Properties props = new Properties();
		InputStream in = TextArea.class.getResourceAsStream(fileName);
		try {
			props.load(in);
		} catch (IOException e) {
			Log.log(Log.ERROR, TextArea.class, e);
		} finally {
			IOUtilities.closeQuietly(in);
		}
		return props;
	} // }}}


	public void setPanelViewListener(PanelViewListener pvl) {
		this.pvl = pvl;
	}
	
	public String getBufferContents() {
		return buffer.getText(0, buffer.getLength());
	}
	
	public String getCurrentSelection() {
		return jedit.getSelectedText();
	}
	
	public void saveBuffer() throws IOException {
		if (buffer.isDirty()) {
			buffer.save();
			updateFrameTitle();
		}
	}
}
