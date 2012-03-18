package org.openimaj.image.annotation.evalutation.dataset;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

public class CorelAnnotatedImage {
	String id;
	File imageFile;
	List<String> annotations;
	
	public CorelAnnotatedImage(String id, File imageFile, File keywordFile) throws IOException {
		this.id = id;
		this.imageFile = imageFile;
		
		annotations = FileUtils.readLines(keywordFile);
	}
	
	public MBFImage getImage() throws IOException {
		return ImageUtilities.readMBF(imageFile);
	}
	
	public String getId() {
		return id;
	}
}
