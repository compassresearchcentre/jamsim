package org.jamsim.swing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.text.Segment;

import org.apache.commons.lang.StringUtils;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.jamsim.io.FileUtil;

/**
 * A {@link JEditBuffer} that contains the contents of a file.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class FileBuffer extends JEditBuffer {

	private final File file;

	/**
	 * System specific line separator, eg: "\r\n" if running on a Windows box.
	 */
	private static final String SYSTEM_LINESEP =
			System.getProperty("line.separator");

	public FileBuffer(File file) throws IOException {
		this.file = file;
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
		} finally {
			readUnlock();
		}

	}

}