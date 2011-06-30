package org.openimaj.tools.faces.recognition;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;

public class FaceRecognitionInfoTool {
	@Option(name="-f", aliases="--file", usage="Recogniser file", required=true)
	File recogniserFile;
	
	public static void main(String [] args) throws IOException {
		FaceRecognitionInfoTool options = new FaceRecognitionInfoTool();
        CmdLineParser parser = new CmdLineParser( options );

        try
        {
	        parser.parseArgument( args );
        }
        catch( CmdLineException e )
        {
	        System.err.println( e.getMessage() );
	        System.err.println( "java FaceRecognitionInfoTool [options...]");
	        parser.printUsage( System.err );
	        return;
        }

		FaceRecognitionEngine<?> engine = options.getEngine();
		
		System.out.println("Detector:\n" + engine.getDetector());
		System.out.println();
		System.out.println("Recogniser:\n" + engine.getRecogniser());
		
		System.out.println();
		
		List<String> people = engine.getRecogniser().listPeople();
		System.out.println("The recogniser has been trained on " + people.size() + " distinct people:");
		System.out.println(people);
	}

	public FaceRecognitionEngine<?> getEngine() throws IOException {
		return FaceRecognitionEngine.load(recogniserFile);
	}
}
