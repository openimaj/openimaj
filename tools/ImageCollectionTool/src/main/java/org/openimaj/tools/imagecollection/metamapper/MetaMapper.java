package org.openimaj.tools.imagecollection.metamapper;

import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;

public abstract class MetaMapper {
	
	public void start() throws Exception{}
	public abstract void mapItem(String name, ImageCollectionEntry<?> entry);
	public void end() throws Exception{}
}
