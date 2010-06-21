package org.jamsim.ascape.output;

import java.io.IOException;

import net.casper.data.model.CDataCacheContainer;
import net.casper.ext.CasperUtil;

import org.omancode.io.FileUtil;

/**
 * A {@link Saveable} {@link CDataCacheContainer}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class SaveableDataset implements Saveable {

	private final CDataCacheContainer container;
	private final String fileName;
	private final boolean cleanDatedFilename;

	/**
	 * Construct {@link SaveableDataset} with cleaned and dated filename.
	 * 
	 * @param fileName
	 *            file name, without path. Cleaned and dated. See
	 *            {@link #cleanDatedCSVName(String)}
	 * @param container
	 *            dataset
	 */
	public SaveableDataset(String fileName, CDataCacheContainer container) {
		this(fileName, container, true);
	}

	/**
	 * Construct {@link SaveableDataset} with cleaned and dated filename.
	 * 
	 * @param fileName
	 *            file name, without path. Cleaned and dated. See
	 *            {@link #cleanDatedCSVName(String)}
	 * @param container
	 *            dataset
	 * @param cleanDatedFilename
	 *            clean and date the filename at the time of saving.
	 */
	public SaveableDataset(String fileName, CDataCacheContainer container,
			boolean cleanDatedFilename) {
		this.fileName = fileName;
		this.container = container;
		this.cleanDatedFilename = cleanDatedFilename;
	}

	@Override
	public void saveToCSV(String directory) throws IOException {
		// TODO Auto-generated method stub
		String cleanedName =
				cleanDatedFilename ? cleanDatedCSVName(fileName) : fileName;

		String fullFileName =
				FileUtil.addTrailingSlash(directory) +  cleanedName;

		// make any non-existent directories in the file path
		FileUtil.mkdirs(fullFileName);
		
		CasperUtil.writeToCSV(fullFileName, container);
	}

	/**
	 * Takes a file name, removes any illegal characters and prefixes a date and
	 * appends ".csv".
	 * 
	 * @param fileName
	 *            file name
	 * @return date + filename (cleaned) + ".csv"
	 */
	public static String cleanDatedCSVName(String fileName) {
		
		return FileUtil.addExtension(FileUtil.cleanDatedName(fileName), ".csv");
	}

}
