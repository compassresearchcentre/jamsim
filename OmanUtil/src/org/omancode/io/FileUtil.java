package org.omancode.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;


/**
 * Static utility class of general purpose file functions.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
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

	public static void writeFile(File file, String contents)
			throws IOException {
		FileWriter fw = new FileWriter(file);
		IOUtils.write(contents, fw);
	}

	/**
	 * Method that will close an {@link InputStream} ignoring it if it is null
	 * and ignoring exceptions.
	 * 
	 * @param in
	 *            the InputStream to close.
	 */
	public static void closeQuietly(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * Load a file resource into a {@link Properties} object.
	 * 
	 * @param resourceClass
	 *            class the file resource is associated with
	 * @param fileName
	 *            file resource to load. Usually needs to exist in the same
	 *            directory as the class it is associated with.
	 * @return properties
	 * @throws IOException
	 *             if problem reading file
	 */
	public static Properties loadProperties(Class<?> resourceClass,
			String fileName) throws IOException {
		Properties props = new Properties();
		InputStream ins = resourceClass.getResourceAsStream(fileName);

		if (ins == null) {
			throw new IOException("Could not find resource \"" + fileName
					+ "\"");
		}

		try {
			props.load(ins);
		} finally {
			closeQuietly(ins);
		}
		return props;
	}

	/**
	 * Regex of invalid Windows file name characters.
	 */
	private static final String INVALID_FILENAME_CHARS_REGEX =
			"[\\\\/:*?<>\"|]";

	/**
	 * Compiled regex pattern of invalid file name characters.
	 */
	private static final Pattern INVALID_FILENAME_CHARS_PATTERN =
			Pattern.compile(INVALID_FILENAME_CHARS_REGEX);

	/**
	 * Remove invalid Windows filename characters (including directory slashes)
	 * from the string.
	 * 
	 * @param filename
	 *            filename, without the path
	 * @return cleaned filename
	 */
	public static String stripInvalidFileNameChars(String filename) {
		Matcher m = INVALID_FILENAME_CHARS_PATTERN.matcher(filename);

		return m.replaceAll("");
	}
}
