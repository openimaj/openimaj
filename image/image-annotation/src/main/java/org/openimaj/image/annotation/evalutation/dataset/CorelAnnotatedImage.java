package org.openimaj.image.annotation.evalutation.dataset;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.ml.annotation.Annotated;

class ImageWrapper implements Identifiable {
	private String id;
	private File imageFile;
	
	public ImageWrapper(String id, File imageFile) {
		this.id = id;
		this.imageFile = imageFile;
	}

	@Override
	public String getID() {
		return id;
	}
	
	public MBFImage getImage() {
		try {
			return ImageUtilities.readMBF(imageFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

public class CorelAnnotatedImage implements Annotated<ImageWrapper, String>, Identifiable {
	private List<String> annotations;
	private ImageWrapper wrapper;
	
	public CorelAnnotatedImage(String id, File imageFile, File keywordFile) throws IOException {
		this.wrapper = new ImageWrapper(id, imageFile);
		
		annotations = FileUtils.readLines(keywordFile);
	}
	
	@Override
	public ImageWrapper getObject() {
		return wrapper;
	}

	@Override
	public Collection<String> getAnnotations() {
		return annotations;
	}

	@Override
	public String getID() {
		return wrapper.getID();
	}
}
