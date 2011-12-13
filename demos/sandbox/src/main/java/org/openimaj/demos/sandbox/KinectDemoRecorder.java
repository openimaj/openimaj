package org.openimaj.demos.sandbox;

import hardware.KinectDemo;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.SwingUtilities;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.xuggle.XuggleVideoWriter;

public class KinectDemoRecorder implements KeyListener, VideoDisplayListener<MBFImage> {
	
	private KinectDemo demo;
	private XuggleVideoWriter writer;
	private boolean close = false;

	public KinectDemoRecorder(){
		demo = new KinectDemo(0);
		writer = new XuggleVideoWriter("kinect.mpg",demo.getCurrentFrame().getWidth(),demo.getCurrentFrame().getHeight(),22);
		demo.getDisplay().addVideoListener(this);
		SwingUtilities.getRoot(demo.getDisplay().getScreen()).addKeyListener(this);
	}


	@Override
	public void keyPressed(KeyEvent arg0) {
		close = arg0.getKeyCode() == KeyEvent.VK_ESCAPE;
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		new KinectDemoRecorder();
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		if(!close)writer.addFrame(frame);
	}
}
