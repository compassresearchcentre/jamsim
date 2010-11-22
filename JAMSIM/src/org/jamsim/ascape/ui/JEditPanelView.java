package org.jamsim.ascape.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.ascape.model.event.ScapeEvent;
import org.ascape.runtime.swing.QuitVetoer;
import org.ascape.runtime.swing.SwingEnvironment;
import org.ascape.runtime.swing.ViewFrameBridge;
import org.ascape.util.swing.PanelViewUtil.PanelViewNoStall;
import org.ascape.view.vis.PanelView;
import org.ascape.view.vis.PersistentComponentView;
import org.gjt.sp.jedit.IPropertyManager;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.buffer.BufferAdapter;
import org.gjt.sp.jedit.buffer.BufferListener;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;
import org.gjt.sp.jedit.textarea.TextArea;
import org.jamsim.io.FileBuffer;
import org.omancode.io.FileUtil;

/**
 * A {@link PanelView} that contains a JEdit {@link TextArea}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class JEditPanelView extends PanelViewNoStall implements
		PersistentComponentView, QuitVetoer {

	private PanelViewListener pvl;
	private final FileBuffer buffer;
	private final TextArea jedit;

	private static final Properties PROPS = new Properties();
	private static final IPropertyManager PROPSMGR = new IPropertyManager() {
		public String getProperty(String name) {
			return PROPS.getProperty(name);
		}
	};

	/**
	 * Default mode for buffer. Used when a mode is not specified.
	 */
	public static final String DEFAULT_MODE = "text";

	static {
		// load properties specified in the files
		// jedit_keys.props, jedit.props, and properties.
		try {
			PROPS.putAll(FileUtil.loadProperties(TextArea.class,
					"/org/gjt/sp/jedit/jedit_keys.props"));
			PROPS.putAll(FileUtil.loadProperties(TextArea.class,
					"/org/gjt/sp/jedit/jedit.props"));
			PROPS.putAll(FileUtil.loadProperties(JEditPanelView.class,
					"JEditPanelView.props"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Create a untitled blank {@link JEditPanelView} using the default mode (
	 * {@link #DEFAULT_MODE}).
	 */
	public JEditPanelView() {
		this("Untitled", "");
	}

	/**
	 * Create a blank {@link JEditPanelView} with specified title and mode.
	 * 
	 * @param title
	 *            {@link PanelView} frame title
	 * @param modeExt
	 *            filename eg: {@code "d:\readme.txt"}, or file extension eg:
	 *            {@code ".txt"}, used to look up the mode. If a mode cannot be
	 *            found, then {@link #DEFAULT_MODE} is used.
	 */
	public JEditPanelView(String title, String modeExt) {
		this(title, new FileBuffer(), modeExt);
	}

	/**
	 * Create {@link JEditPanelView} with specified title and underlying file.
	 * 
	 * @param title
	 *            {@link PanelView} frame title
	 * @param file
	 *            file contents of the JEdit {@link TextArea}, or {@code null}
	 *            to create an empty {@link TextArea}. Mode is determined from
	 *            the file extension.
	 * @throws IOException
	 *             if problem reading file.
	 */
	public JEditPanelView(String title, File file) throws IOException {
		this(title, new FileBuffer(file), file.getCanonicalPath());
	}

	private JEditPanelView(String title, FileBuffer buffer, String modeExt) {
		this.buffer = buffer;

		// create a new empty PanelView
		setName(title);
		setLayout(new BorderLayout());

		// create text area
		jedit = new StandaloneTextArea(PROPSMGR);

		// init buffer, add to text area
		initFileBuffer(buffer, modeExt);
		jedit.setBuffer(buffer);

		// add text area to panel view
		add(jedit, BorderLayout.CENTER);
	}

	/**
	 * Create a frame and display this {@link JEditPanelView}.
	 */
	public void display() {
		SwingEnvironment.DEFAULT_ENVIRONMENT.createFrame(this);
		addInternalFrameListener();
	}

	private void addInternalFrameListener() {
		// get the frame
		Container pvFrameImp = getViewFrame().getFrameImp();

		// The pvFrameImp type will depend on what is selected by
		// the ViewFrameBridge.selectFrameImp method.
		// We are expecting the user to be using the Swing MDI
		// which will return a JInternalFrame
		if (pvFrameImp instanceof JInternalFrame) {

			JInternalFrame pvFrame = (JInternalFrame) pvFrameImp;

			// when the frame closes, fire the PanelViewListener
			pvFrame.addInternalFrameListener(createPanelViewListener());

		} else {
			throw new RuntimeException("Unknown frame type "
					+ pvFrameImp.getClass().getCanonicalName());
		}
	}

	/**
	 * Make this an environmentView. This means it persists (ie: not disposed)
	 * after the scape closes. NB: You also need to make sure we are not removed
	 * as a listener from the scape when it closes (see
	 * {@link #scapeNotification(ScapeEvent)}).
	 * 
	 * @return {@code false}
	 */
	@Override
	public boolean isLifeOfScape() {
		return false;
	}

	@Override
	public boolean canQuit() {

		if (pvl == null) {
			return true;
		}

		// call the PanelViewListener
		int actionOnClose = pvl.panelViewClosing(this);
		return (actionOnClose == JInternalFrame.DISPOSE_ON_CLOSE);
	}

	/**
	 * Is this is a new file that hasn't been saved.
	 * 
	 * @return true if new unsaved file
	 */
	public boolean isNewUnsavedFile() {
		return buffer.getFile() == null;
	}

	/**
	 * Set buffer save file and change panel title.
	 * 
	 * @param file
	 *            file
	 * @throws IOException
	 *             if problem reading file name
	 */
	public void setFile(File file) throws IOException {
		setName(file.getCanonicalPath());
		buffer.setFile(file);
	}

	/**
	 * {@link InternalFrameListener} that fires the closing event on the
	 * {@link PanelViewListener}.
	 * 
	 * @return internal frame listener
	 */
	private InternalFrameListener createPanelViewListener() {
		return new InternalFrameAdapter() {

			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				if (pvl != null) {
					// fire closing event on PanelViewListener
					int actionOnClose =
							pvl.panelViewClosing(JEditPanelView.this);
					JInternalFrame iFrame = e.getInternalFrame();
					iFrame.setDefaultCloseOperation(actionOnClose);
				}
			}

		};
	}

	/**
	 * Looks up the mode for a given filename, or filename extension, adds it to
	 * the buffer and creates a buffer listener to modify the panel view's title
	 * when the buffer's dirty status changes.
	 * 
	 * @param buffer
	 *            buffer
	 * @param modeExt
	 *            filename eg: {@code "d:\readme.txt"}, or file extension eg:
	 *            {@code ".txt"}, used to look up the mode. If a mode cannot be
	 *            found, then {@link #DEFAULT_MODE} is used.
	 * @return the buffer passed in
	 */
	private FileBuffer initFileBuffer(FileBuffer buffer, String modeExt) {

		// get mode for file based on its filename
		Mode mode = ModeProvider.instance.getModeForFile(modeExt, "");

		if (mode == null) {
			mode = ModeProvider.instance.getMode(DEFAULT_MODE);
		}

		buffer.setMode(mode);

		buffer.addBufferListener(createModifiedListener());

		return buffer;
	}

	/**
	 * {@link BufferListener} that updates the modified status of the frame's
	 * title.
	 * 
	 * @return buffer listener
	 */
	private BufferListener createModifiedListener() {
		return new BufferAdapter() {

			/**
			 * Change the modified status of the frame's title. Fired after
			 * insertion, removal, undo, redo operations.
			 */
			@Override
			public void transactionComplete(JEditBuffer buffer) {
				updateFrameTitle();
			}
		};
	}

	/**
	 * Update the frame title with the buffer's dirty/modified status.
	 */
	private void updateFrameTitle() {
		// get the frame
		ViewFrameBridge pvBridge = getViewFrame();

		String modified = buffer.isDirty() ? " * " : "";
		pvBridge.setTitle(name + modified);
	}

	/**
	 * Set the {@link PanelViewListener}.
	 * 
	 * @param pvl
	 *            panel view listener
	 */
	public void setPanelViewListener(PanelViewListener pvl) {
		this.pvl = pvl;
	}

	/**
	 * Get the entire contents of the buffer.
	 * 
	 * @return entire buffer contents
	 */
	public String getBufferContents() {
		return buffer.getText(0, buffer.getLength());
	}

	/**
	 * Get currently highlighted buffer selection.
	 * 
	 * @return current selection of {@code null} if nothing selected.
	 */
	public String getCurrentSelection() {
		return jedit.getSelectedText();
	}

	/**
	 * Return the modified status of the buffer.
	 * 
	 * @return buffer dirty status
	 */
	public boolean isDirty() {
		return buffer.isDirty();
	}

	/**
	 * Save the buffer to its file (if the buffer is dirty).
	 * 
	 * @throws IOException
	 *             if problem saving buffer.
	 */
	public void saveBuffer() throws IOException {
		// save if dirty, or completely empty (allows saving of new empty file)
		if (buffer.isDirty() || buffer.getLength() == 0) {
			buffer.save();
			updateFrameTitle();
		}
	}
}
