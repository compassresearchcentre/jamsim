package org.omancode.io;

/**
 * Output interface providing {@link #print(String)} and
 * {@link #println(String)} functions.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public interface Output {

	/**
	 * Print.
	 * 
	 * @param message message
	 */
	void print(String message);

	/**
	 * Print with linefeed.
	 * 
	 * @param message message
	 */
	void println(String message);
}
