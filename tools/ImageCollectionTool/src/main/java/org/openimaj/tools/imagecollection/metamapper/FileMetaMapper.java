package org.openimaj.tools.imagecollection.metamapper;

import java.io.File;

import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;

public class FileMetaMapper implements MetaMapper{
	public FileMetaMapper(){
	}
	@Override
	public void map(String name, ImageCollectionEntry<?> entry) {
		String jsonString = entry.metaAsJson();
		System.out.println(String.format("%s: %s",name,jsonString));
	}
}
