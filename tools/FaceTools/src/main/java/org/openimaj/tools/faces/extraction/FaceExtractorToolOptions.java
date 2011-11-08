/**
 * 
 */
package org.openimaj.tools.faces.extraction;

import org.kohsuke.args4j.Option;

/**
 * 	Command-line option class for the face extractor tool.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 7 Nov 2011
 */
public class FaceExtractorToolOptions
{
	/** The video file to process */
	@Option(name="-v",aliases="--video",usage="Video File",required=true)
	public String videoFile = null;
	
	/** The directory to which all faces will be extracted */
	@Option(name="-o",aliases="--output",usage="Output Directory",required=true)
	public String outputFile = null;
	
	/** Whether to output verbose progress to the stdout */
	@Option(name="--verbose",usage="Verbose Output",required=false)
	public boolean verbose = false;
	
	/** How often to take a face test */
	@Option(name="-s",usage="Face Test Period",required=false)
	public int seconds = 2;
	
	/** Whether to use the centre frame (as opposed to doing face tests) */
	@Option(name="-c",aliases="--centre",usage="Use Centre Frame",required=false)
	public boolean useCentre = false;
	
	/** Whether to write the face image or not */
	@Option(name="-f",aliases="--face",usage="Write Face Images",required=false)
	public boolean writeFaceImage = false;
	
	/** Whether to write the frames in which the faces are found */
	@Option(name="-vf",aliases="--frame",usage="Write Frame Image",required=false)
	public boolean writeFrameImage = false;
	
	/** Whether to write XML information about the found faces */
	@Option(name="-x",aliases="--xml",usage="Write XML info for faces",required=false)
	public boolean writeXML = false;
	
	/** The shot differential threshold to use for the shot boundary detector */
	@Option(name="-t",aliases="--threshold",usage="Set Shot Boundary Threshold",required=false)
	public double threshold = 5000;
	
	/** The minimum face size for the face detector */
	@Option(name="-m",aliases="--minFaceSize",usage="Set the Minimum Face Size",required=false)
	public int faceSize = 40;
}
