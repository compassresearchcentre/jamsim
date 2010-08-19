package org.jamsim.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.text.Segment;

import org.apache.commons.lang.StringUtils;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.omancode.io.FileUtil;

/**
 * A {@link JEditBuffer} that contains the contents of a file. Extends
 * {@link JEditBuffer} to load and save a file.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class FileBuffer extends JEditBuffer {

	private File file;

	/**
	 * System specific line separator, eg: "\r\n" if running on a Windows box.
	 */
	private static final String SYSTEM_LINESEP =
			System.getProperty("line.separator");

	/**
	 * Create a {@link FileBuffer} without specifying a file.
	 */
	public FileBuffer() {
		// empty constructor
	}

	/**
	 * Create {@link #FileBuffer(File)} and load the contents of file into it.
	 * File is also used later when saving.
	 * 
	 * @param file
	 *            file, or {@code null} if no file to be specified yet.
	 * @throws IOException
	 *             if problem reading contents of file.
	 */
	public FileBuffer(File file) throws IOException {
		this.file = file;
		if (file != null) {
			loadFileIntoBuffer(file);
		}
	}

	
	/**
	 * Get the file used for saving.
	 * 
	 * @return file 
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Set the file used for saving.
	 * 
	 * @param file
	 *            file
	 */
	public void setFile(File file) {
		this.file = file;
	}

	private void loadFileIntoBuffer(File file) throws IOException {
		String contents = FileUtil.readFile(file);

		/**
		 * The buffer line manager expects lines with "\n" at the end. Replace
		 * any occurrences of the system line-sep with "\n". This code won't
		 * work with files using line-seps different to the system line-sep.
		 */
		String replaceWindowsCRLF =
				StringUtils.replace(contents, SYSTEM_LINESEP, "\n");

		insertWithoutRecordingUndo(replaceWindowsCRLF);
	}

	private void insertWithoutRecordingUndo(String contents) {
		this.insert(0, contents);
		resetUndoState();
	}

	private void resetUndoState() {
		this.setDirty(false);

		// super.undoMgr = new UndoManager(this);
		undoMgr.clear();
		undoMgr.resetClearDirty();
	}

	/**
	 * Save contents of the buffer to the file.
	 * 
	 * @throws IOException
	 * 
	 * @throws IOException
	 *             if problem saving
	 */
	public void save() throws IOException {
		
		if (file == null) {
			throw new IOException("File to save has not been specified.");
		}
		
		FileWriter writer = new FileWriter(file);

		readLock();

		Segment lineSegment = new Segment();
		String newline = getStringProperty(JEditBuffer.LINESEP);
		if (newline == null) {
			// don't know this file's line separator
			// so use the system line separator
			newline = SYSTEM_LINESEP;
		}

		int bufferLineCount = getLineCount();

		try {

			int i = 0;

			// write out each line (ie: each block of text
			// between a "\n" character) followed by the
			// system line separator
			while (i < bufferLineCount) {
				getLineText(i, lineSegment);
				writer.write(lineSegment.array, lineSegment.offset,
						lineSegment.count);
				if (i < bufferLineCount - 1) {
					writer.write(newline);
				}
				i++;
			}
			writer.flush();
			resetUndoState();
		} finally {
			readUnlock();
			writer.close();
		}

	}

}