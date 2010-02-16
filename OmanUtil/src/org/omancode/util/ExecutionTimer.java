package org.omancode.util;

/**
 * Simple execution timer.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 *
 */
public class ExecutionTimer {
	private long startMs = 0;
	private long endMs = 0;

	/**
	 * Start the timer.
	 */
	public void start() {
		startMs = System.currentTimeMillis();
	}

	/**
	 * Stop the timer.
	 */
	public void stop() {
		endMs = System.currentTimeMillis();
	}

	/**
	 * Get the duration of last run.
	 * 
	 * @return duration.
	 */
	public long duration() {
		return (endMs - startMs);
	}

}