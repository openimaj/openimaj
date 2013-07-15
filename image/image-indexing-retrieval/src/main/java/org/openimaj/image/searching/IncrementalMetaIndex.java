package org.openimaj.image.searching;

import org.openimaj.data.identity.Identifiable;

public interface IncrementalMetaIndex<DATA, METADATA extends Identifiable> {
	public void put(int id, DATA data);

	public METADATA get(int id);
}
