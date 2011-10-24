package org.openimaj.hardware.kinect;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
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
public class KinectDemo extends Video<MBFImage> implements KeyListener {
	MBFImage currentFrame;
	KinectController controller;
	JFrame frame;
	private double tilt = 0;
	private boolean irmode = false;
	
	public KinectDemo() {
		controller = new KinectController(0, irmode);
		currentFrame = new MBFImage(640*2, 480, ColourSpace.RGB);
		
		VideoDisplay<MBFImage> videoFrame = VideoDisplay.createVideoDisplay(this);
		SwingUtilities.getRoot(videoFrame.getScreen()).addKeyListener(this);
	}
	
	@Override
	public MBFImage getNextFrame() {
		MBFImage vid;
		Image<?,?> tmp = controller.videoStream.getNextFrame();
		
		if (tmp instanceof MBFImage)
			vid = (MBFImage) tmp;
		else
			vid = new MBFImage((FImage)tmp, (FImage)tmp, (FImage)tmp);
		
		currentFrame.drawImage(vid, 0, 0);
		
		tmp = controller.depthStream.getNextFrame();
		MBFImage depth = org.openimaj.image.colour.Transforms.Grey_TO_HeatRGB((FImage) tmp);
		
		currentFrame.drawImage(depth, 640, 0);

		currentFrame.createRenderer(RenderHints.ANTI_ALIASED).drawText(controller.getAcceleration().toString(), 0, 480, HersheyFont.TIMES_MEDIUM, 16, RGBColour.WHITE);
		
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
		new KinectDemo();
	}

	@Override
    public long getTimeStamp()
    {
	    return (long)(super.currentFrame / getFPS()) * 1000;
    }

	@Override
    public double getFPS()
    {
	    return 30;
    }
}
