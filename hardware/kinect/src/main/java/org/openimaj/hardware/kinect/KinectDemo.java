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
public class KinectDemo extends Video<MBFImage> implements KeyListener {
	MBFImage currentFrame;
	KinectController controller;
	JFrame frame;
	private double tilt = 0;
	private boolean irmode = false;
	private MBFImageRenderer renderer;
	private String accel;
	
	public KinectDemo(int id) {
		controller = new KinectController(id, irmode);
		currentFrame = new MBFImage(640*2, 480, ColourSpace.RGB);
		renderer = currentFrame.createRenderer(RenderHints.ANTI_ALIASED);
		
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
		
		renderer.drawImage(vid, 0, 0);
		
		tmp = controller.depthStream.getNextFrame();
		MBFImage depth = org.openimaj.image.colour.Transforms.Grey_TO_HeatRGB((FImage) tmp);
		
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
		new KinectDemo(1);
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
