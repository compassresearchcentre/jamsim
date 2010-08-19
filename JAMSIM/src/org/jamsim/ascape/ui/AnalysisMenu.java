package org.jamsim.ascape.ui;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.ascape.util.swing.AscapeGUIUtil;

/**
 * Analysis menu. Provides analysis menu items.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 */
public final class AnalysisMenu {

	private JMenu menu; 

	private AnalysisMenu() {
		// do nothing
	}

	/**
	 * SingletonHolder is loaded, and the static initializer executed, on the
	 * first execution of Singleton.getInstance() or the first access to
	 * SingletonHolder.INSTANCE, not before.
	 */
	private static final class SingletonHolder {

		/**
		 * Singleton instance, with static initializer.
		 */
		private static final AnalysisMenu INSTANCE = createSingleton();

		/**
		 * Create singleton instance using static parameters from outer class.
		 * 
		 * @return instance
		 */
		private static AnalysisMenu createSingleton() {
			try {
				return new AnalysisMenu(); // NOPMD
			} catch (Exception e) {
				// a static initializer cannot throw exceptions
				// but it can throw an ExceptionInInitializerError
				throw new ExceptionInInitializerError(e);
			}
		}

		/**
		 * Prevent instantiation.
		 */
		private SingletonHolder() {
		}

		/**
		 * Get singleton instance.
		 * 
		 * @return singleton instance.
		 */
		public static AnalysisMenu getInstance() {
			return SingletonHolder.INSTANCE;
		}

	}

	/**
	 * Return the singleton instance. The first time this is called the instance
	 * will be created using the supplied parameters.
	 * 
	 * @return an {@link AnalysisMenu} singleton instance.
	 */
	public static AnalysisMenu getInstance() {
		return SingletonHolder.getInstance();
	}

	/**
	 * Add the Analysis menu to the menu bar.
	 * 
	 * @param scape
	 *            scape
	 */
	private void addMenu() {
		menu = new JMenu("Analysis");
		menu.setMnemonic(KeyEvent.VK_A);

		AscapeGUIUtil.addMenu(menu);
	}

	/**
	 * Add a menu item to the Analysis menu.
	 * 
	 * @param action action to add as a menu item.
	 */
	public void addMenuItem(Action action) {
		if (menu == null) {
			addMenu();
		}
		
		menu.add(new JMenuItem(action));
	}

	
	/**
	 * Remove all items from the Analysis menu.
	 */
	public void removeAll() {
		if (menu != null) {
			menu.removeAll();
		}
	}

	/**
	 * Get the {@link JMenu} component.
	 * 
	 * @return {@link JMenu}.
	 */
	public JMenu getJMenu() {
		return menu;
	}

}
