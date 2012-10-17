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
import java.util.Arrays;
import java.util.List;

/**
 * A read-only view on a list with a set of indices from the
 * underlying list that must be skipped. The view is
 * continuous, and presents itself without any gaps.
 * The size  of the view is equal to the length of 
 * the underlying list minus the number of skipped elements. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> The type of element in the list
 */
public class SkippingListView<T> extends AbstractList<T> {
	private List<T> list;
	private int[] skips;
	private int size;

	/**
	 * Construct with the underlying list and a set of
	 * indices to skip. 
	 * 
	 * @param list the backing list
	 * @param skips the indices to skip
	 */
	public SkippingListView(List<T> list, int... skips) {
		if (skips == null)
			skips = new int[0];
		
		this.list = list;
		this.size = list.size() - skips.length;
		
		this.skips = skips.clone();
		Arrays.sort(this.skips);
		
		for (int i=0; i<skips.length; i++)
			this.skips[i] -= i;
	}
	
	@Override
	public T get(int index) {
		int shift = Arrays.binarySearch(skips, index);
				
		if (shift < 0) {
			shift = -1 * (shift + 1);
		} else {
			while ((shift < skips.length - 1) && (skips[shift] == skips[shift + 1])) {
				shift++;
			}
			
			shift++;
			
		}
		
		return list.get(index + shift);
	}

	@Override
	public int size() {
		return size;
	}
}
