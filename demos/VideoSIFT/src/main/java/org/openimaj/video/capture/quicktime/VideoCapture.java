/**
 * Copyright (c) ${year}, The University of Southampton and the individual contributors.
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
package org.openimaj.video.capture.quicktime;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.video.Video;

import quicktime.QTRuntimeException;
import quicktime.QTRuntimeHandler;
import quicktime.QTSession;
import quicktime.qd.PixMap;
import quicktime.qd.QDGraphics;
import quicktime.qd.QDRect;
import quicktime.std.StdQTConstants;
import quicktime.std.StdQTException;
import quicktime.std.sg.SGVideoChannel;
import quicktime.std.sg.SequenceGrabber;
import quicktime.util.RawEncodedImage;

/**
 * Handle video capture from quicktime
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 */
@SuppressWarnings("deprecation")
public class VideoCapture extends Video<MBFImage> {
    private SequenceGrabber grabber;
    private SGVideoChannel channel;
    private RawEncodedImage rowEncodedImage;

    private int width;
    private int height;
    private int videoWidth;

    private int[] pixels;

    private MBFImage mbfimage;
    
    public VideoCapture(int width, int height) throws Exception {
        this.width = width;
        this.height = height;
                
        try {
            QTSession.open();
            QDRect bounds = new QDRect(width, height);
            QDGraphics graphics = new QDGraphics(bounds);
            grabber = new SequenceGrabber();
            grabber.setGWorld(graphics, null);
            channel = new SGVideoChannel(grabber);
            channel.setBounds(bounds);
            channel.setUsage(StdQTConstants.seqGrabPreview);
//            channel.settingsDialog();
            grabber.prepare(true, false);
            grabber.startPreview();
            PixMap pixMap = graphics.getPixMap();
            rowEncodedImage = pixMap.getPixelData();

            videoWidth = width + (rowEncodedImage.getRowBytes() - width * 4) / 4;
            mbfimage = new MBFImage(videoWidth, height, ColourSpace.RGB);
            
            pixels = new int[videoWidth * height];
            
            QTRuntimeException.registerHandler(new QTRuntimeHandler() {
                @Override
				public void exceptionOccurred(
                        QTRuntimeException e, Object eGenerator,
                        String methodNameIfKnown, boolean unrecoverableFlag) {
                    System.out.println("what should i do?");
                }
            });
        } catch (Exception e) {
            QTSession.close();
            throw e;
        }
    }

    public void dispose() {
        try {
            grabber.stop();
            grabber.release();
            grabber.disposeChannel(channel);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            QTSession.close();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getVideoHeight() {
        return height;
    }

    public void getNextPixels(int[] pixels) throws Exception {
        grabber.idle();
        rowEncodedImage.copyToArray(0, pixels, 0, pixels.length);
    }
    
    @Override 
    public double getFPS(){
    	return -1;
    }
	@Override
	public MBFImage getNextFrame() {
		try {
			grabber.idle();
		} catch (StdQTException e) {
			return this.getCurrentFrame();
		}
        rowEncodedImage.copyToArray(0, pixels, 0, pixels.length);
        mbfimage.internalAssign(pixels, videoWidth, height);
        
        return mbfimage;
	}

	@Override
	public MBFImage getCurrentFrame() {
		return mbfimage;
	}
}