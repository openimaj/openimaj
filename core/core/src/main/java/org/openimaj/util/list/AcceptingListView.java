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
package org.openimaj.util.list;

import java.util.AbstractList;
import java.util.List;

/**
 * A read-only view on a list with a set of indices from the
 * underlying list that must be accepted; all other
 * indices are discarded. The view is
 * continuous, and presents itself without any gaps.
 * The size of the view is equal to the length of 
 * the number of skipped elements. 
 * <p>
 * The {@link AcceptingListView} is the exact opposite
 * of the {@link SkippingListView}. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> The type of element in the list
 */
public class AcceptingListView<T> extends AbstractList<T> {
	private List<T> list;
	private int[] indices;

	/**
	 * Construct with the underlying list and a set of
	 * indices for the view. 
	 * 
	 * @param list the backing list
	 * @param indices the indices
	 */
	public AcceptingListView(List<T> list, int... indices) {
		this.list = list;

		if (indices == null)
			indices = new int[0];
		
		this.indices = indices;
	}
	
	@Override
	public T get(int index) {
		return list.get(indices[index]);
	}

	@Override
	public int size() {
		return indices.length;
	}
}
