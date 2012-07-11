package org.openimaj.demos.sandbox;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.SwingUtilities;

import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.xuggle.XuggleVideoWriter;

/**
 * Record the webcam to a file.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class VideoRecorder extends KeyAdapter implements VideoDisplayListener<MBFImage> {
	private Video<MBFImage> video;
	private VideoDisplay<MBFImage> display;
	private XuggleVideoWriter writer;
	private boolean close = false;

	/**
	 * Default constructor
	 * @throws IOException
	 */
	public VideoRecorder() throws IOException {
		//open webcam
		video = new VideoCapture(320, 240);
		
		//open display
		display = VideoDisplay.createVideoDisplay(video);
		
		//open a writer
		writer = new XuggleVideoWriter("video.flv", video.getWidth(), video.getHeight(), 30);

		//set this class to listen to video display events
		display.addVideoListener(this);
		
		//set this class to listen to keyboard events
		SwingUtilities.getRoot(display.getScreen()).addKeyListener(this);
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		//Do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		//write a frame 
		if (!close) {
			writer.addFrame(frame);
		}
	}
	
	@Override
	public void keyPressed(KeyEvent key) {
		//wait for the escape key to be pressed
		close = key.getKeyCode() == KeyEvent.VK_ESCAPE;
	}
	
	/**
	 * Main method
	 * @param args ignored
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		new VideoRecorder();
	}
}
