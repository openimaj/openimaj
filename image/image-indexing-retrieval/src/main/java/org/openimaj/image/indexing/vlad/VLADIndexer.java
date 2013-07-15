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
