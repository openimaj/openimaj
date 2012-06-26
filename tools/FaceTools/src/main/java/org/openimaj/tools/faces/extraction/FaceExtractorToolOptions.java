/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * 
 */
package org.openimaj.tools.faces.extraction;

import org.kohsuke.args4j.Option;

/**
 * 	Command-line option class for the face extractor tool.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
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
