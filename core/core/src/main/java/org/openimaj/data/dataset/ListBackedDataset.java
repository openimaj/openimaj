/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.data.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A {@link ListBackedDataset} is a {@link Dataset} backed by an ordered list of
 * items. For convenience, the dataset is itself presented as as list.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the type of items in the dataset
 */
public class ListBackedDataset<T> implements ListDataset<T> {
	protected List<T> data;

	/**
	 * Construct with the an empty {@link ArrayList} as the backing store.
	 */
	public ListBackedDataset() {
		this.data = new ArrayList<T>();
	}

	/**
	 * Construct with the given list of items.
	 * 
	 * @param backingList
	 *            the backing list
	 */
	public ListBackedDataset(List<T> backingList) {
		this.data = backingList;
	}

	/**
	 * Construct by consuming the contents of the given iterator into the
	 * backing list.
	 * <p>
	 * Obviously this method could cause problems if the number of items in the
	 * iterator is very large, as memory could be exhausted. Care should be
	 * taken.
	 * 
	 * @param iterator
	 *            the data to read
	 */
	public ListBackedDataset(Iterable<T> iterator) {
		super();

		for (final T item : iterator) {
			data.add(item);
		}
	}

	@Override
	public T getRandomInstance() {
		return data.get((int) (Math.random() * data.size()));
	}

	@Override
	public final int size() {
		return data.size();
	}

	@Override
	public int numInstances() {
		return data.size();
	}

	@Override
	public T getInstance(int i) {
		return data.get(i);
	}

	/**
	 * Get the underlying list backing this dataset
	 * 
	 * @return the list backing this dataset
	 */
	public List<T> getList() {
		return data;
	}

	@Override
	public Iterator<T> iterator() {
		return data.iterator();
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return data.contains(o);
	}

	@Override
	public Object[] toArray() {
		return data.toArray();
	}

	@Override
	public <V> V[] toArray(V[] a) {
		return data.toArray(a);
	}

	@Override
	public boolean add(T e) {
		return data.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return data.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return data.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return data.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		return data.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return data.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return data.retainAll(c);
	}

	@Override
	public void clear() {
		data.clear();
	}

	@Override
	public T get(int index) {
		return data.get(index);
	}

	@Override
	public T set(int index, T element) {
		return data.set(index, element);
	}

	@Override
	public void add(int index, T element) {
		data.add(index, element);
	}

	@Override
	public T remove(int index) {
		return data.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return data.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return data.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return data.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return data.listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return data.subList(fromIndex, toIndex);
	}

	@Override
	public String toString() {
		return data.toString();
	}
}
