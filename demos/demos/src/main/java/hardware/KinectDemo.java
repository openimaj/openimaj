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
package hardware;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.openimaj.demos.Demo;
import org.openimaj.hardware.kinect.KinectController;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;


/**
 * A demo showing off all the features.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
@Demo(
		author = "Jonathon Hare", 
		description = "Kinect integration demo. Shows video and depth. Press t " +
				"to toggle between rgb and ir mode. Pressing w and x moves the device " +
				"up or down. Pressing s levels the device.", 
		keywords = { "kinect", "video" }, 
		title = "Kinect Integration"
	)
public class KinectDemo extends Video<MBFImage> implements KeyListener {
	MBFImage currentFrame;
	KinectController controller;
	JFrame frame;
	private double tilt = 0;
	private boolean irmode = false;
	private MBFImageRenderer renderer;
	private String accel;
	private VideoDisplay<MBFImage> videoFrame;
	
	public KinectDemo(int id) {
		controller = new KinectController(id, irmode);
		currentFrame = new MBFImage(640*2, 480, ColourSpace.RGB);
		renderer = currentFrame.createRenderer(RenderHints.ANTI_ALIASED);
		
		videoFrame = VideoDisplay.createVideoDisplay(this);
		SwingUtilities.getRoot(videoFrame.getScreen()).addKeyListener(this);
		
	}
	
	@Override
	public MBFImage getNextFrame() {
		MBFImage vid;
		Image<?,?> tmp = controller.videoStream.getNextFrame();
		
		if (tmp instanceof MBFImage)
		{
			vid = (MBFImage) tmp;
		}
		else
		{
			vid = new MBFImage((FImage)tmp, (FImage)tmp, (FImage)tmp);
		}
		
		renderer.drawImage(vid, 0, 0);
		
		tmp = controller.depthStream.getNextFrame();
		MBFImage depth = org.openimaj.image.colour.Transforms.Grey_TO_Colour((FImage) tmp);
		
		
		renderer.drawImage(depth, 640, 0);

		if (super.currentFrame % 30 == 0) accel = controller.getAcceleration().toString();
		renderer.drawText(accel, 0, 480, HersheyFont.TIMES_MEDIUM, 16, RGBColour.WHITE);
		
		super.currentFrame++;
		
		return currentFrame;
	}

	@Override
	public MBFImage getCurrentFrame() {
		return currentFrame;
	}

	@Override
	public int getWidth() {
		return currentFrame.getWidth();
	}

	@Override
	public int getHeight() {
		return currentFrame.getHeight();
	}

	@Override
	public boolean hasNextFrame() {
		return true;
	}

	@Override
	public long countFrames() {
		return -1;
	}

	@Override
	public void reset() {
		//do nothing
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == 'w') {
			controller.setTilt(tilt+=1);
		} else if (e.getKeyChar() == 'x') {
			controller.setTilt(tilt-=1);
		} else if (e.getKeyChar() == 's') {
			controller.setTilt(tilt=0);
		} else if (e.getKeyChar() == 't') {
			controller.setIRMode(irmode=!irmode );
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}
	
	public static void main(String[] args) {
		new KinectDemo(0);
	}

	@Override
    public long getTimeStamp()
    {
		return (long)(super.currentFrame * 1000 / getFPS());
    }

	@Override
    public double getFPS()
    {
	    return 30;
    }

	public VideoDisplay<MBFImage> getDisplay() {
		return videoFrame;
	}
}
