package org.openimaj.tools.vis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointVisualizer;
import org.openimaj.image.feature.local.keypoints.quantised.QuantisedKeypoint;

public class QuantisedSIFTVisualiser {
	@Option(name="--input-image", aliases="-ii", required=true, usage="input image file")
	File imageFile;
	
	@Option(name="--input-features", aliases="-if", required=true, usage="input quantised features file")
	File quantisedFeatureFile;

	@Option(name="--output", aliases="-o", required=true, usage="output image file")
	File outputImageFile;
	
	@Option(name="--colour", aliases="-c", required=false, usage="colour to draw boxes in")
	String colour = "BLUE";
	Float[] colourValue = null; 
	
	@Argument(required=false, usage="required term identifiers")
	List<Integer> requiredIds;

	public void process() throws IOException {
		LocalFeatureList<QuantisedKeypoint> qkeys = MemoryLocalFeatureList.read(quantisedFeatureFile, QuantisedKeypoint.class);
		List<Keypoint> keys = new ArrayList<Keypoint>();
		
		MBFImage image = ImageUtilities.readMBF(imageFile);
		
		
		if (requiredIds == null || requiredIds.size() == 0) {
			for (QuantisedKeypoint kpt : qkeys) {
				keys.add(makeKeypoint(kpt));
			}
		} else {
			for (QuantisedKeypoint kpt : qkeys) {
				if (requiredIds.contains(kpt.id)) {
					keys.add(makeKeypoint(kpt));
				}
			}
		}
		
		KeypointVisualizer<Float[], MBFImage> viz = new KeypointVisualizer<Float[], MBFImage>(image, keys);
		MBFImage outimg = viz.drawPatches(colourValue, null);
		
		ImageUtilities.write(outimg, outputImageFile);
	}
	
	protected Keypoint makeKeypoint(QuantisedKeypoint kpt) {
		Keypoint key = new Keypoint();
		
		key.y = kpt.location.y;
		key.x = kpt.location.x;
		key.scale = kpt.location.scale;
		key.ori = kpt.location.orientation;
		
		return key; 
	}
	
	public static QuantisedSIFTVisualiser load(String [] args) {
		QuantisedSIFTVisualiser options = new QuantisedSIFTVisualiser();
        CmdLineParser parser = new CmdLineParser( options );

        try {
	        parser.parseArgument( args );
	        
	        options.colourValue = RGBColour.fromString(options.colour);
        } catch( CmdLineException e ) {
	        System.err.println( e.getMessage() );
	        System.err.println( "java " + QuantisedSIFTVisualiser.class.getName() + " options...");
	        parser.printUsage( System.err );
	        System.exit(1);
        }

        return options;
	}
	
	public static void main(String [] args) throws IOException {
		QuantisedSIFTVisualiser extr = QuantisedSIFTVisualiser.load(args);
		extr.process();
	}
}
