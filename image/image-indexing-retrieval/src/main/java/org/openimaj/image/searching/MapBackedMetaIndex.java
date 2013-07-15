package org.openimaj.image.searching;

import gnu.trove.map.hash.TIntObjectHashMap;

import org.openimaj.data.identity.Identifiable;
import org.openimaj.feature.FeatureExtractor;

public class MapBackedMetaIndex<DATA, METADATA extends Identifiable>
		implements
		IncrementalMetaIndex<DATA, METADATA>
{
	TIntObjectHashMap<METADATA> index = new TIntObjectHashMap<METADATA>();
	FeatureExtractor<METADATA, DATA> extractor;

	public MapBackedMetaIndex(FeatureExtractor<METADATA, DATA> extractor) {
		this.extractor = extractor;
	}

	@Override
	public void put(int id, DATA data) {
		index.put(id, extractor.extractFeature(data));
	}

	@Override
	public METADATA get(int id) {
		return index.get(id);
	}
}
