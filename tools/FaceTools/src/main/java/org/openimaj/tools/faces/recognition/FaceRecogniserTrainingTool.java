package org.openimaj.tools.faces.recognition;

import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.tools.faces.recognition.FaceRecogniserTrainingToolOptions.RecognitionStrategy;

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
	        
	        System.err.println();
	        System.err.println("Strategy information:");
	        for (RecognitionStrategy s : RecognitionStrategy.values()) {
	        	System.err.println(s + ":");
	        	System.err.println(s.description());
	        	System.err.println();
	        }
	        return;
        }

        FaceRecognitionEngine<?> engine = options.getEngine();
        
        if (options.identifier == null) {
        	engine.trainBatch(options.files);
        } else {
        	engine.trainSingle(options.identifier, options.files);
        }
        
        engine.save(options.recogniserFile);
	}
	
}
