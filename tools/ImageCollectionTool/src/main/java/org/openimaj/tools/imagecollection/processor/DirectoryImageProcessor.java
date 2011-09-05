package org.openimaj.tools.imagecollection.processor;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.io.FileUtils;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;

public class DirectoryImageProcessor<T extends Image<?, T>> extends ImageCollectionProcessor<T> {

	File directoryFile = new File(".");
	boolean force = true;
	String imageOutputFormat = "%s.png";

	public DirectoryImageProcessor(String output, boolean force,String imageNameFormat) {
		this.directoryFile = new File(output);
		this.force = force;
		this.imageOutputFormat = imageNameFormat;
	}

	@Override
	public void start() throws IOException{
		if(this.directoryFile.isDirectory() ){
			if(this.directoryFile.exists()){
				if(force)
					FileUtils.deleteRecursive(this.directoryFile);
				else
					throw new IOException("Directory already exists");
			}
			
			
			if(!this.directoryFile.mkdirs()){
				throw new IOException("Can't create directory");
			}
		}
		// Directory should exist and be a directory now
	}

	@Override
	public void process(ImageCollectionEntry<T> image) throws IOException{
		if(image.accepted){
			File imageOutput = new File(this.directoryFile,String.format(imageOutputFormat,image.name));
			ImageUtilities.write(image.image, imageOutput);
		}
	}

}
