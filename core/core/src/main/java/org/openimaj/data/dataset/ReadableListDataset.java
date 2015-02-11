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

import java.util.AbstractList;

import org.openimaj.data.identity.Identifiable;
import org.openimaj.data.identity.IdentifiableObject;
import org.openimaj.io.InputStreamObjectReader;
import org.openimaj.io.ObjectReader;

/**
 * Base class for {@link ListDataset}s in which each instance is read with an
 * {@link InputStreamObjectReader}.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <INSTANCE>
 *            the type of instances in the dataset
 * @param <SOURCE>
 *            the type of object that provides the data to create the instance
 */
public abstract class ReadableListDataset<INSTANCE, SOURCE> extends AbstractList<INSTANCE>
		implements
		ListDataset<INSTANCE>
{
	protected ObjectReader<INSTANCE, SOURCE> reader;

	/**
	 * Construct with the given {@link ObjectReader}.
	 *
	 * @param reader
	 *            the {@link InputStreamObjectReader}.
	 */
	public ReadableListDataset(ObjectReader<INSTANCE, SOURCE> reader) {
		this.reader = reader;
	}

	@Override
	public INSTANCE getRandomInstance() {
		return getInstance((int) (Math.random() * size()));
	}

	@Override
	public INSTANCE get(int index) {
		return this.getInstance(index);
	}

	/**
	 * Get an identifier for the instance at the given index. By default this
	 * just returns the index converted to a {@link String}, but sub-classes
	 * should override to to something more sensible if possible.
	 *
	 * @param index
	 *            the index
	 * @return the identifier of the instance at the given index
	 */
	public String getID(int index) {
		return index + "";
	}

	/**
	 * Get the index of the instance with the given ID, or -1 if it can't be
	 * found.
	 *
	 * @param id
	 *            the ID string
	 * @return the index; or -1 if not found.
	 */
	public int indexOfID(String id) {
		for (int i = 0; i < size(); i++) {
			if (getID(i).equals(id))
				return i;
		}
		return -1;
	}

	@Override
	public final int size() {
		return numInstances();
	}

	private class WrappedListDataset extends AbstractList<IdentifiableObject<INSTANCE>>
			implements
			ListDataset<IdentifiableObject<INSTANCE>>
	{
		private final ReadableListDataset<INSTANCE, SOURCE> internal;

		WrappedListDataset(ReadableListDataset<INSTANCE, SOURCE> internal) {
			this.internal = internal;
		}

		@Override
		public IdentifiableObject<INSTANCE> getRandomInstance() {
			final int index = (int) (Math.random() * size());

			return getInstance(index);
		}

		@Override
		public IdentifiableObject<INSTANCE> getInstance(int index) {
			return new IdentifiableObject<INSTANCE>(internal.getID(index), internal.getInstance(index));
		}

		@Override
		public IdentifiableObject<INSTANCE> get(int index) {
			return getInstance(index);
		}

		@Override
		public int size() {
			return internal.size();
		}

		@Override
		public int numInstances() {
			return internal.size();
		}
	}

	/**
	 * Create a view of this dataset in which the instances are wrapped up in
	 * {@link IdentifiableObject}s. The {@link #getID(int)} method is used to
	 * determine the identifier.
	 *
	 * @return a view of this dataset with {@link Identifiable}-wrapped
	 *         instances
	 */
	public ListDataset<IdentifiableObject<INSTANCE>> toIdentifiable() {
		return new WrappedListDataset(this);
	}
}
