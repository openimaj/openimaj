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
package org.openimaj.experiment.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link ListDataset} is a {@link Dataset} backed by an ordered
 * list of items.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> the type of items in the dataset
 */
public class ListDataset<T extends Identifiable> implements Dataset<T> {
	private List<T> data;

	/**
	 * Construct with the an empty {@link ArrayList} as the backing store.
	 */
	public ListDataset() {
		this.data = new ArrayList<T>();
	}
	
	/**
	 * Construct with the given list of items.
	 * @param backingList
	 */
	public ListDataset(List<T> backingList) {
		this.data = backingList;
	}

	@Override
	public T getRandomItem() {
		return data.get((int)(Math.random() * data.size()));
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public T getItem(int i) {
		return data.get(i);
	}

	/**
	 * Add an item to the dataset.
	 * @param item the item to add.
	 */
	public void addItem(T item) {
		data.add(item);
	}
	
	/**
	 * Add the given items to the dataset.
	 * @param items the items to add.
	 */
	public void addItems(Collection<T> items) {
		data.addAll(items);
	}

	/**
	 * @return the list backing this dataset
	 */
	public List<T> getList() {
		return data;
	}

	@Override
	public Iterator<T> iterator() {
		return data.iterator();
	}
}
