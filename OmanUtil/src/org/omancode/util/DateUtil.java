package org.omancode.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class of static methods that work with dates.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class DateUtil {

	/**
	 * Unique sortable date formatter.
	 */
	private static final SimpleDateFormat UNIQUE_SORTABLE =
			new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());

	private DateUtil() {
		// no instantiation
	}

	public static String nowToSortableUniqueDateString() {
		return toUniqueSortableString(new Date());
	}

	/**
	 * Converts date to a unique date time string in sortable order. i.e:
	 * yyyyMMdd-HHmmss.
	 * 
	 * @param date
	 *            date to convert
	 * @return unique sortable string representation of {@code date}.
	 */
	public static String toUniqueSortableString(Date date) {
		return UNIQUE_SORTABLE.format(date);
	}

}
