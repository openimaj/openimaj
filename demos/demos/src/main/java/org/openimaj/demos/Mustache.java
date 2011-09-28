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
package org.openimaj.demos;

import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.keypoints.KEDetectedFace;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

import Jama.Matrix;

public class Mustache {
	MBFImage mustache;

	/**
	 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
	 *	@version $Author$, $Revision$, $Date$
	 *	@created 26 Sep 2011
	 */
	public static class VideoMustache
	{
		private Mustache m = new Mustache();
		
		public VideoMustache() throws IOException
		{
			VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(
					new VideoCapture( 320, 240 ) );
//					new XuggleVideo(new File( "src/test/resources/rttr1.mpg") ) );
			vd.addVideoListener( new VideoDisplayListener<MBFImage>()
			{
				@Override
				public void beforeUpdate( MBFImage frame )
				{
					frame.internalAssign( m.addMustaches( frame ) );
				}
				
				@Override
				public void afterUpdate( VideoDisplay<MBFImage> display )
				{
				}
			});
			vd.run();
		}
	}

	public Mustache() {
		try {
			mustache = ImageUtilities.readMBFAlpha(Mustache.class.getResourceAsStream("mustache.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Mustache(MBFImage mustache) {
		this.mustache = mustache;
	}
	
	public MBFImage addMustaches(MBFImage image) {
		MBFImage cimg;
		
		if (image.getWidth() > image.getHeight() && image.getWidth() > 640) {
			cimg = image.process(new ResizeProcessor(640, 480));
		} else 	if (image.getHeight() > image.getWidth() && image.getHeight() > 640) {
			cimg = image.process(new ResizeProcessor(480, 640));
		} else {
			cimg = image.clone();
		}
		
		FImage img = Transforms.calculateIntensityNTSC(cimg);
		
		List<KEDetectedFace> faces = new FKEFaceDetector(new HaarCascadeDetector(40)).detectFaces(img);
		MBFImageRenderer renderer = cimg.createRenderer();
		
		for(KEDetectedFace face : faces) {
			Matrix tf = AffineAligner.estimateAffineTransform(face);
			Shape bounds = face.getBounds();
			
			MBFImage m = mustache.transform(tf.times(TransformUtilities.scaleMatrix(1f/4f, 1f/4f)));
			
			renderer.drawImage(m, (int)bounds.minX(), (int)bounds.minY());
		}
		
		return cimg;
	}
	
	public static void main(String[] args) throws IOException 
	{
		if( args.length > 0 && args[0].equals( "-v" ) )
		{
			new Mustache.VideoMustache();
		}
		else
		{
	//		File image = new File("/Users/jon/Desktop/IMG_5590.jpg");
	//		File image = new File("/Users/jon/Pictures/Pictures/2003/09/29/DCP_1051.jpg");
			MBFImage cimg = ImageUtilities.readMBF(Mustache.class.getResourceAsStream("/org/openimaj/image/data/sinaface.jpg"));
	
			cimg = new Mustache().addMustaches(cimg);
			
			DisplayUtilities.display(cimg);
		}
	}
}
