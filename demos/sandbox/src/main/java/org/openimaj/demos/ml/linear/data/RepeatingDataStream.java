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
package org.openimaj.demos.ml.linear.data;

import java.util.ArrayList;
import java.util.Iterator;
import org.openimaj.ml.linear.data.DataGenerator;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.AbstractStream;

public class RepeatingDataStream<I, D> extends AbstractStream<IndependentPair<I,D>> {

	private DataGenerator<I, D> dg;
	private int total;
	private ArrayList<IndependentPair<I, D>> items;
	private Iterator<IndependentPair<I, D>> innerIter;

	public RepeatingDataStream(DataGenerator<I,D> dg,int totalDataItems) {
		this.dg = dg;
		this.total = totalDataItems;
		this.items = new ArrayList<IndependentPair<I,D>>();
		
		for (int i = 0; i < total; i++) {
			items.add(dg.generate());
		}
		refresh();
	}

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public IndependentPair<I, D> next() {
		if(!innerIter.hasNext()) refresh();
		return innerIter.next();
	}

	private void refresh() {
		this.innerIter = this.items.iterator();
	}

}
