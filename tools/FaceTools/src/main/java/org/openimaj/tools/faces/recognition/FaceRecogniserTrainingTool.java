package org.openimaj.tools.faces.recognition;

import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;

public class FaceRecogniserTrainingTool<T extends DetectedFace> {
	
	public static void main(String [] args) throws IOException {
		FaceRecogniserTrainingToolOptions options = new FaceRecogniserTrainingToolOptions();
        CmdLineParser parser = new CmdLineParser( options );

        try
        {
	        parser.parseArgument( args );
        }
        catch( CmdLineException e )
        {
	        System.err.println( e.getMessage() );
	        System.err.println( "java FaceRecogniserTrainingTool [options...] IMAGE-FILES-OR-DIRECTORIES");
	        parser.printUsage( System.err );
        }

        FaceRecognitionEngine<?> engine = options.getEngine();
        
        if (options.identifier == null) {
        	engine.trainBatch(options.files);
        } else {
        	engine.trainSingle(options.identifier, options.files);
        }
	}
	
}
