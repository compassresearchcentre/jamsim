package org.omancode.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.AbstractListModel;

/**
 * A bounded set ordered from the most recently added to the first added.
 * Implements {@link AbstractListModel} so it can be used as the data model for
 * Swing components (eg: menus).
 * 
 * @author Oliver Mannion
 * @version $Revision$
 * 
 * @param <E>
 *            element type
 */
public class MRUSet<E> extends AbstractListModel implements Set<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6629088027065317007L;

	/**
	 * Used in the reverse string representation. See {@link #toReverseString()}
	 * .
	 */
	public static final String SEPARATOR = "|";

	/**
	 * A double ended list.
	 */
	private final LinkedList<E> backing = new LinkedList<E>();

	/**
	 * Maximum number of items in the {@link MRUSet}.
	 */
	private final int capacity;

	/**
	 * Create {@link MRUSet} with specified capacity.
	 * 
	 * @param capacity
	 *            maximum number of items in the {@link MRUSet}.
	 */
	public MRUSet(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * Add the specified item to the head of the {@link MRUSet}. If the item
	 * already exists it will be repositioned at the head of the set. If
	 * capacity has been reached, items at the end of the {@link MRUSet} will be
	 * removed.
	 * 
	 * @param item
	 *            item
	 * @return true
	 */
	public boolean add(E item) {

		// if exists remove first (we'll add back
		// to the head next)
		if (backing.contains(item)) {
			backing.remove(item);
		}

		// If full, remove items first
		while (backing.size() >= capacity) {
			backing.removeLast();
		}

		// add item to list head
		backing.addFirst(item);

		fireAllContentsChanged();

		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		for (E item : c) {
			add(item);
		}

		return true;
	}

	@Override
	public void clear() {
		backing.clear();
		fireAllContentsChanged();
	}

	@Override
	public boolean contains(Object o) {
		return backing.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return backing.containsAll(c);
	}

	/**
	 * Retrieves, but does not remove, the head (first element) of this set.
	 * 
	 * @return the head of this list
	 * @throws NoSuchElementException
	 *             - if this list is empty
	 */
	public E element() {
		return backing.element();
	}

	private void fireAllContentsChanged() {
		fireContentsChanged(this, 0, getSize() - 1);
	}

	@Override
	public Object getElementAt(int index) {
		return backing.get(index);
	}

	@Override
	public int getSize() {
		return backing.size();
	}

	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return backing.iterator();
	}

	@Override
	public boolean remove(Object o) {
		boolean result = backing.remove(o);
		fireAllContentsChanged();
		return result;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = backing.removeAll(c);
		fireAllContentsChanged();
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean result = backing.retainAll(c);
		fireAllContentsChanged();
		return result;
	}

	@Override
	public int size() {
		return backing.size();
	}

	@Override
	public Object[] toArray() {
		return backing.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return backing.toArray(a);
	}

	/**
	 * String representation of calling toString on every element in reverse
	 * order. Elements are separated by {@link #SEPARATOR}.
	 * 
	 * @return reverse string representation
	 */
	public String toReverseString() {
		StringBuffer sb = new StringBuffer(512);

		for (int i = getSize() - 1; i >= 0; i--) {
			sb.append(getElementAt(i).toString());
			if (i > 0) {
				sb.append(" ");
				sb.append(SEPARATOR);
				sb.append(" ");
			}
		}

		return sb.toString();

	}

	@Override
	public String toString() {
		return backing.toString();
	}
}
