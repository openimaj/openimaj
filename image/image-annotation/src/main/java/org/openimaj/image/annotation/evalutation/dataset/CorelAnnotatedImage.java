package org.openimaj.image.annotation.evalutation.dataset;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.AnnotatedImage;

public class CorelAnnotatedImage implements AnnotatedImage<MBFImage, String>, Identifiable {
	private String id;
	private File imageFile;
	private List<String> annotations;
	
	public CorelAnnotatedImage(String id, File imageFile, File keywordFile) throws IOException {
		this.id = id;
		this.imageFile = imageFile;
		
		annotations = FileUtils.readLines(keywordFile);
	}
	
	public MBFImage getImage() {
		try {
			return ImageUtilities.readMBF(imageFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<String> getAnnotations() {
		return annotations;
	}

	@Override
	public String getID() {
		return id;
	}
}
