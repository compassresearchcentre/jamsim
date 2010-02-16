package org.jamsim.ascape.stats;

import java.io.Serializable;

/**
 * Tests a scape member.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 * @param <T>
 *            type of scape member.
 */
public interface StatsPredicate<T> extends Serializable {

	/**
	 * Test name.
	 * 
	 * @return test name.
	 */
	String getName();

	/**
	 * Test scape member.
	 * 
	 * @param pParam
	 *            scape member to test
	 * @return {@code true} if the scape member passes the tests
	 */
	boolean test(T pParam);

}
