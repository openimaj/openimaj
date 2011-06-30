package org.openimaj.tools.faces.recognition;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.FaceMatchResult;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.util.pair.IndependentPair;

public class FaceRecognitionTool {
	
	public static void main(String [] args) throws IOException, ClassNotFoundException {
		FaceRecognitionToolOptions options = new FaceRecognitionToolOptions();
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

        @SuppressWarnings("unchecked")
		FaceRecognitionEngine<DetectedFace> engine = (FaceRecognitionEngine<DetectedFace>) options.getEngine();
        
        for (File f : options.files) {
        	
        	System.out.println(f);
			
        	List<IndependentPair<DetectedFace, FaceMatchResult>> res = engine.queryBestMatch(f);
        	for (int i=0; i<res.size(); i++) {
				System.out.println("Face "+i+": " + res.get(i).firstObject() + " -> " + res.get(i).secondObject());
			}
			
			System.out.println();
        }
	}
	
}
