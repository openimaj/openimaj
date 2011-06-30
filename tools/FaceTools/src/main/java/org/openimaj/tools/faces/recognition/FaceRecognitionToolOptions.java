package org.openimaj.tools.faces.recognition;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;

class FaceRecognitionToolOptions {
	
	@Option(name="-f", aliases="--file", usage="Recogniser file", required=true)
	File recogniserFile;
	
	@Argument()
	List<File> files;
	
	public FaceRecognitionEngine<?> getEngine() throws IOException {
		return FaceRecognitionEngine.load(recogniserFile);
	}
	
}
