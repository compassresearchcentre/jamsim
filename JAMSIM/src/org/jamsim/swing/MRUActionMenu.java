package org.jamsim.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.jamsim.util.MRUSet;

/**
 * A {@link JMenu} based on an {@link MRUSet} that refreshes itself when the
 * {@link MRUSet} changes. When a {@link JMenuItem} is selected, it is moved to
 * the head of the {@link MRUSet}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class MRUActionMenu extends JMenu implements ListDataListener,
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -245602531506077759L;

	private final MRUSet<Action> set;

	/**
	 * Create menu from {@link MRUSet}.
	 * 
	 * @param title
	 *            menu title
	 * @param set
	 *            menu items
	 */
	public MRUActionMenu(String title, MRUSet<Action> set) {
		super(title);
		this.set = set;
		rebuildMenu();
		set.addListDataListener(this);
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		rebuildMenu();
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		rebuildMenu();
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		rebuildMenu();
	}

	private void rebuildMenu() {
		removeAll();

		for (Action action : set) {
			JMenuItem menuItem = new JMenuItem(action);
			menuItem.addActionListener(this);
			add(menuItem);
		}

		setEnabled(set.getSize() > 0);

		revalidate();
	}

	/**
	 * Called when one of our {@link JMenuItem}s is selected. Re-adds the action
	 * to the set so it is moved to the top of the list.
	 * 
	 * @param e
	 *            action event that provides {@link JMenuItem} source of event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem menuItem = (JMenuItem) e.getSource();
		Action action = menuItem.getAction();

		// re-add the action so it is moved to the top
		// of the list
		set.add(action);

	}
}
