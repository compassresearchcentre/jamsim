package org.jamsim.ascape.ui;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.ascape.util.swing.AscapeGUIUtil;

/**
 * Analysis menu. Provides analysis menu items. Implemented as a singleton (as
 * per Effective Java recommendation p18: "a single-element enum type is the
 * best way to implement a singleton").
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 */
public enum ToolsMenu {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	private JMenu menu;

	public JMenu getMenu() {
		return menu;
	}

	public void setMenu(JMenu menu) {
		this.menu = menu;
	}

	/**
	 * Add the Analysis menu to the menu bar.
	 * 
	 * @param scape
	 *            scape
	 */
	private void addMenu() {
		menu = new JMenu("Tools");
		menu.setMnemonic(KeyEvent.VK_A);

		AscapeGUIUtil.addMenu(menu);
	}

	/**
	 * Add a menu item to the Analysis menu.
	 * 
	 * @param action
	 *            action to add as a menu item.
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
