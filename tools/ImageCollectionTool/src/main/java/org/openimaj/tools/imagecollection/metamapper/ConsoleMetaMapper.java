package org.openimaj.tools.imagecollection.metamapper;

import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;

public class ConsoleMetaMapper implements MetaMapper{
	@Override
	public void map(String name, ImageCollectionEntry<?> entry) {
		String jsonString = entry.metaAsJson();
		System.out.println(String.format("%s: %s",name,jsonString));
	}
}
