package org.jamsim.io;

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


}
