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
package org.openimaj.image.indexing.vlad;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.data.identity.Identifiable;
import org.openimaj.image.ImageProvider;
import org.openimaj.image.MBFImage;
import org.openimaj.image.indexing.IncrementalIndexer;
import org.openimaj.image.searching.ImageSearchResult;
import org.openimaj.image.searching.IncrementalMetaIndex;
import org.openimaj.knn.pq.IncrementalFloatADCNearestNeighbours;
import org.openimaj.util.pair.IntFloatPair;

public class VLADIndexer<DATA extends ImageProvider<MBFImage>, METADATA extends Identifiable>
		implements
		IncrementalIndexer<DATA, ImageSearchResult<METADATA>, ImageProvider<MBFImage>>
{
	private static final int DEFAULT_MAX_RESULTS = 5000;

	private VLADIndexerData indexerData;
	private IncrementalFloatADCNearestNeighbours nn;
	private IncrementalMetaIndex<DATA, METADATA> metaStore;

	public VLADIndexer(VLADIndexerData indexerData, IncrementalMetaIndex<DATA, METADATA> metaStore) {
		this.indexerData = indexerData;
		this.nn = indexerData.createIncrementalIndex();
		this.metaStore = metaStore;
	}

	@Override
	public void indexImage(DATA image) {
		final int id = indexerData.index(image.getImage(), nn);
		metaStore.put(id, image);
	}

	@Override
	public List<ImageSearchResult<METADATA>> search(ImageProvider<MBFImage> query) {
		final List<IntFloatPair> res = nn.searchKNN(indexerData.extractPcaVlad(query.getImage()), DEFAULT_MAX_RESULTS);

		final List<ImageSearchResult<METADATA>> results = new ArrayList<ImageSearchResult<METADATA>>(res.size());
		for (int i = 0; i < res.size(); i++) {
			final IntFloatPair r = res.get(i);
			results.add(new ImageSearchResult<METADATA>(metaStore.get(r.first), r.second));
		}

		return results;
	}
}
