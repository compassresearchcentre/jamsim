package org.omancode.swing;

import java.awt.Insets;

import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * Static utility class of general purpose file functions.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class SwingUtil {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private SwingUtil() {
	}
	
	/**
	 * Resize a {@link JSplitPane} to its maximum or minimum. Silently exits
	 * without resize if the split pane is not visible. Adapted from {@code
	 * OneTouchActionHandler.actionPerformed} in {@link BasicSplitPaneDivider}.
	 * 
	 * @param toMinimum
	 *            {@code true} indicates the resize should go the minimum (top
	 *            or left) vs {@code false} which indicates the resize should go
	 *            to the maximum.
	 * @param splitPane
	 *            the split pane to adjust
	 */
	public static void resizeSplit(boolean toMinimum, JSplitPane splitPane) {
		BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) splitPane.getUI();
		BasicSplitPaneDivider divider = splitPaneUI.getDivider();
		Insets insets = splitPane.getInsets();
		int lastLoc = splitPane.getLastDividerLocation();
		int currentLoc = splitPaneUI.getDividerLocation(splitPane);
		int newLoc;

		if (!splitPane.isVisible()) {
			// if split pane is not visible then
			// splitPane.getHeight() will be -1
			// and we can't get the max/min value to resize to
			// so return
			return;
		}

		// We use the location from the UI directly, as the location the
		// JSplitPane itself maintains is not necessarily correct.
		if (toMinimum) {
			if (splitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
				if (currentLoc >= (splitPane.getHeight() - insets.bottom - divider
						.getHeight())) {
					int maxLoc = splitPane.getMaximumDividerLocation();
					newLoc = Math.min(lastLoc, maxLoc);
					// splitPaneUI.setKeepHidden(false);
				} else {
					newLoc = insets.top;
					// splitPaneUI.setKeepHidden(true);
				}
			} else {
				if (currentLoc >= (splitPane.getWidth() - insets.right - divider
						.getWidth())) {
					int maxLoc = splitPane.getMaximumDividerLocation();
					newLoc = Math.min(lastLoc, maxLoc);
					// splitPaneUI.setKeepHidden(false);
				} else {
					newLoc = insets.left;
					// splitPaneUI.setKeepHidden(true);
				}
			}
		} else {
			if (splitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
				if (currentLoc == insets.top) {
					int maxLoc = splitPane.getMaximumDividerLocation();
					newLoc = Math.min(lastLoc, maxLoc);
					// splitPaneUI.setKeepHidden(false);
				} else {
					newLoc =
							splitPane.getHeight() - divider.getHeight()
									- insets.top;
					// splitPaneUI.setKeepHidden(true);
				}
			} else {
				if (currentLoc == insets.left) {
					int maxLoc = splitPane.getMaximumDividerLocation();
					newLoc = Math.min(lastLoc, maxLoc);
					// splitPaneUI.setKeepHidden(false);
				} else {
					newLoc =
							splitPane.getWidth() - divider.getWidth()
									- insets.left;
					// splitPaneUI.setKeepHidden(true);
				}
			}
		}
		if (currentLoc != newLoc) {
			splitPane.setDividerLocation(newLoc);
			// We do this in case the dividers notion of the location
			// differs from the real location.
			splitPane.setLastDividerLocation(currentLoc);
		}
	}

}
