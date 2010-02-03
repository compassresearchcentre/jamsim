package org.jamsim.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

public final class FileUtil {

	private FileUtil() {
		// no instantiation
	}

	/**
	 * Add a trailing slash to a string if it does not have one.
	 * 
	 * @param str
	 *            string to test
	 * @return {@code str} with a trailing slash, or {@code null} if {@code str}
	 *         is {@code null}
	 */
	public static String addTrailingSlash(String str) {
		if (str == null) {
			return null;
		}
		return str.substring(str.length() - 1).equals("\\") ? str : str
				+ "\\";
	}

	/**
	 * Return contents of a file as a String. Closes file after reading.
	 * 
	 * @param file
	 *            file to read
	 * @return contents of the file
	 * @throws IOException
	 *             if file not found or problem reading the file
	 */
	public static String readFile(File file) throws IOException {
		FileReader fr = new FileReader(file);
		String contents = IOUtils.toString(fr);

		// release file after loading,
		// instead of waiting for VM exit/garbage collection
		fr.close();

		return contents;

	}
	
	public static void writeFile(File file, String contents) throws IOException {
		FileWriter fw = new FileWriter(file);
		IOUtils.write(contents, fw);
	}

}
