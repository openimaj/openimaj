package org.openimaj.tools.imagecollection.metamapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;
import org.openimaj.tools.imagecollection.utils.MetaMapUtils;

public class FileMetaMapper extends MetaMapper{
	private File metaOutputFile;
	private PrintWriter metaWriter;
	public FileMetaMapper(File metaOutputFile) throws FileNotFoundException{
		this.metaOutputFile = metaOutputFile;
	}
	
	public void start() throws FileNotFoundException{
		this.metaWriter = new PrintWriter(new FileOutputStream(metaOutputFile),true);
	}
	
	@Override
	public void mapItem(String name, ImageCollectionEntry<?> entry) {
		String jsonString = MetaMapUtils.metaAsJson(entry.meta);
		metaWriter.println(String.format("%s: %s",name,jsonString));
	}
	
	public void end() throws FileNotFoundException{
		this.metaWriter.flush();
		this.metaWriter.close();
	}
}
