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
package org.openimaj.demos.sitestuff;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.demos.sandbox.image.gif.GifSequenceWriter;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

public class VideoProcessingSiteDemo {
	public static void main(String[] args) throws VideoCaptureException {
		VideoCapture cap = new VideoCapture(640, 480);
		final List<Image<?,?>> frames = new ArrayList<Image<?,?>>();
		VideoDisplay.createOffscreenVideoDisplay(cap).addVideoListener(new VideoDisplayListener<MBFImage>() {
			MBFImage last;
			int nWritten = 0;
			@Override
			public void beforeUpdate(MBFImage frame) {
				if(frame==null) return;
				MBFImage combined = new MBFImage(frame.getWidth()*2,frame.getHeight(),ColourSpace.RGB);
				combined.drawImage(frame, 0, 0);
				if(last != null){
					combined.drawImage(
						frame.subtract(last).abs(), frame.getWidth(),0
					);
				}
				last = frame.clone();
				combined.processInplace(new ResizeProcessor(400, 300));
				DisplayUtilities.displayName(combined, "combined");
				frames.add(combined);
				if(frames.size()%60 == 0){
					try {
						File gifOut = new File("/Users/ss/Desktop/videoProc/"+ "out_" + nWritten + ".gif");
						GifSequenceWriter.writeGif(frames,200, true, gifOut);
						frames.clear();
						System.out.println("GIF written: " + gifOut);
						nWritten++;
					} catch (Exception e) {
					}
				}
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {

			}
		});
	}
}
