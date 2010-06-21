package org.jamsim.ascape.output;

import java.io.IOException;

/**
 * Generic exception that signals a problem with the creation of output data.
 * 
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class OutputException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2260127450897805233L;

	/**
	 * Throw exception with message / reason.
	 * 
	 * @param message
	 *            The detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method)
	 */
	public OutputException(String message) {
		super(message);
	}

	/**
	 * Throw exception with previous (chained) exception.
	 * 
	 * <p>
	 * Note that the detail message associated with {@code cause} is <i>not</i>
	 * automatically incorporated into this exception's detail message.
	 * 
	 * @param message
	 *            The detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method)
	 * 
	 * @param cause
	 *            The cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A null value is permitted, and
	 *            indicates that the cause is nonexistent or unknown.)
	 */
	public OutputException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs an exception with the specified cause and a detail message of
	 * {@code (cause==null ? null : cause.toString())} (which typically contains
	 * the class and detail message of {@code cause}). This constructor is
	 * useful for IO exceptions that are little more than wrappers for other
	 * throwables.
	 * 
	 * @param cause
	 *            The cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A null value is permitted, and
	 *            indicates that the cause is nonexistent or unknown.)
	 * 
	 * @since 1.6
	 */
	public OutputException(Throwable cause) {
		super(cause);
	}

}