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
package org.openimaj.demos.video;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.openimaj.demos.Demo;
import org.openimaj.hardware.kinect.KinectController;
import org.openimaj.hardware.kinect.KinectException;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;


/**
 * 	Kinect integration demo. Shows video and depth. Press t to toggle between 
 * 	rgb and ir mode. Pressing w and x moves the device up or down. 
 * 	Pressing s levels the device.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Demo(
		author = "Jonathon Hare", 
		description = "Kinect integration demo. Shows video and depth. Press t " +
		"to toggle between rgb and ir mode. Pressing w and x moves the device " +
		"up or down. Pressing s levels the device.", 
		keywords = { "kinect", "video" }, 
		title = "Kinect Integration",
		screenshot = "/org/openimaj/demos/screens/hardware/kinect.png",
		icon = "/org/openimaj/demos/icons/hardware/kinect.png"
)
public class KinectDepthSnapshot extends Video<MBFImage> implements KeyListener {
	private static final int MAX_HELD_FRAMES = 10;
	MBFImage currentFrame;
	KinectController controller;
	JFrame frame;
	private double tilt = 0;
	private boolean irmode = false;
	private VideoDisplay<MBFImage> videoFrame;
	private Queue<IndependentPair<FImage, MBFImage>> heldDepthFrames;
	private IndependentPair<FImage, MBFImage> currentDepthFrame;
	private int screenWidth;
	private int screenHeight;
	protected ResizeProcessor fullScreenResizeProcessor;

	/**
	 * 	Default constructor
	 *  @param id of kinect controller
	 *  @throws KinectException
	 */
	public KinectDepthSnapshot(int id) throws KinectException {
		controller = new KinectController(id, irmode,true);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = ge.getScreenDevices();
		JFrame frame=new JFrame("Full Screen JFrame");
		//Set default close operation for JFrame  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		//Set JFrame size to full screen size follow current screen size  
		screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
		screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
		fullScreenResizeProcessor = new ResizeProcessor(screenWidth, screenHeight);
		frame.setBounds(0,0,screenWidth,screenHeight);  
		videoFrame = VideoDisplay.createVideoDisplay(this, new DisplayUtilities.ImageComponent(true));
		JFrame wholeWindow = new JFrame();
		wholeWindow.setUndecorated(true);
		wholeWindow.setAlwaysOnTop(true);
		wholeWindow.getContentPane().add(videoFrame.getScreen());
		devices[0].setFullScreenWindow(wholeWindow);
		((JFrame)SwingUtilities.getRoot(videoFrame.getScreen())).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SwingUtilities.getRoot(videoFrame.getScreen()).addKeyListener(this);

	}
	
	@Override
	public MBFImage getNextFrame() {
		FImage tmp = controller.depthStream.getNextFrame();
		MBFImage frame = (MBFImage) controller.videoStream.getNextFrame();//Transforms.Grey_To_Colour((FImage) tmp);

//		depth.bands.get(0).shiftRightInplace(50);
//		depth.bands.get(1).shiftRightInplace(50);
//		depth.bands.get(2).shiftRightInplace(50);
		
//		MBFImage depth = ((FImage) controller.videoStream.getNextFrame()).toRGB();
		
//		// Clean Frames
//		if (currentFrame == null || super.currentFrame % 600 == 0)
//		{
//			currentFrame = frame.clone();
//		}
//
//		if(oldDepth == null || super.currentFrame % 600 == 0)
//		{
//			oldDepth = tmp.clone();
//		}
		
		// Update held depths
		if(super.currentFrame % 30 == 0){
			if (super.currentFrame % (60 * 2 * MAX_HELD_FRAMES) == 0)
			{
				removeAllFrames();
			}

			addDepthFrame(tmp,frame);
			currentFrame = this.currentDepthFrame.secondObject();
		}
		else{
			FImage oldDepth = this.currentDepthFrame.firstObject().clone();
			currentFrame = this.currentDepthFrame.secondObject().clone();
			for (int y = 0; y < tmp.height; y++) {
				for (int x = 0; x < tmp.width; x++) {
					if (oldDepth.pixels[y][x] == 0 || (tmp.pixels[y][x] != 0 && oldDepth.pixels[y][x] > tmp.pixels[y][x])) {
//						oldDepth.pixels[y][x] = tmp.pixels[y][x];
						currentFrame.setPixel(x, y, frame.getPixel(x, y));
					}
				}
			}
		}

		super.currentFrame++;
//		return currentFrame.process(fullScreenResizeProcessor);
		return currentFrame;
	}

	private void removeAllFrames() {
		if (this.heldDepthFrames!=null)this.heldDepthFrames.clear();
	}

	private void addDepthFrame(FImage tmp, MBFImage frame) {
		if(this.heldDepthFrames == null){
			this.heldDepthFrames = new LinkedList<IndependentPair<FImage,MBFImage>>();
		}
		if(this.heldDepthFrames.size() == MAX_HELD_FRAMES){
			this.heldDepthFrames.poll();
		}
		
		
		this.heldDepthFrames.add(IndependentPair.pair(tmp.clone(), frame.clone()));
		System.out.println("Added frame, new frame size:" + this.heldDepthFrames.size());
		this.currentDepthFrame = constructDepthFrame();
	}
	
	private IndependentPair<FImage, MBFImage> constructDepthFrame() {
		FImage compiledDepth = null;
		MBFImage compiledRGB = null;
		for (IndependentPair<FImage, MBFImage> heldFrame : this.heldDepthFrames) {
			
			FImage heldDepth = heldFrame.firstObject();
			MBFImage heldRGB = heldFrame.secondObject();
			if(compiledDepth == null){
				
				compiledDepth = heldDepth.clone();
				compiledRGB = heldRGB .clone();
				continue;
			}
			
			for (int y = 0; y < heldDepth.height; y++) {
				for (int x = 0; x < heldDepth.width; x++) {
					if (compiledDepth.pixels[y][x] == 0 || (heldDepth.pixels[y][x] != 0 && compiledDepth.pixels[y][x] > heldDepth.pixels[y][x])) {
						compiledDepth.pixels[y][x] = heldDepth.pixels[y][x];
						compiledRGB.setPixel(x, y, heldRGB.getPixel(x, y));
					}
				}
			}
		}
		return IndependentPair.pair(compiledDepth, compiledRGB);
	}

	@Override
	public MBFImage getCurrentFrame() {
		return currentFrame;
	}

	@Override
	public int getWidth() {
		return this.screenWidth;
	}

	@Override
	public int getHeight() {
		return this.screenHeight;
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

	/**
	 * 	Default main 
	 *  @param args Command-line arguments
	 *  @throws KinectException
	 */
	public static void main(String[] args) throws KinectException {
		new KinectDepthSnapshot(0);
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

	/**
	 * 	Get the display showing the kinect video
	 *  @return The video display
	 */
	public VideoDisplay<MBFImage> getDisplay() {
		return videoFrame;
	}
}
