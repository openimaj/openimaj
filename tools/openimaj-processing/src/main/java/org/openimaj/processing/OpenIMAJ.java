package org.openimaj.processing;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PShape;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class OpenIMAJ implements PConstants{

	private static final int DEFAULT_WIDTH = 640;
	private static final int DEFAULT_HEIGHT = 480;
	PApplet parent;
	private MBFImage oiImage;
	private HaarCascadeDetector faceDetector;
	private VideoCapture capture;

	/**
	 * @param parent
	 */
	public OpenIMAJ(PApplet parent) {
		this();
		this.parent = parent;

		parent.registerMethod("dispose", this);
		parent.registerMethod("pre", this);
	}

	/**
	 *
	 */
	public OpenIMAJ() {
		faceDetector = new HaarCascadeDetector(80);
	}

	/**
	 * Initialise face detection with minimum face size
	 * @param min
	 */
	public void initFace(int min){
		faceDetector = new HaarCascadeDetector(min);
	}

	/**
	 * Start a video capture, default size, default device
	 */
	public void startCapture(){
		try {
			this.capture = new VideoCapture(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		} catch (VideoCaptureException e) {
		}
	}

	/**
	 * Initialise video capture on the default device
	 *
	 * @param width
	 * @param height
	 */
	public void startCapture(int width, int height){
		try {
			this.capture = new VideoCapture(width, height);
		} catch (VideoCaptureException e) {
		}
	}
	/**
	 * Initialise video capture
	 *
	 * @param width
	 * @param height
	 * @param device
	 */
	public void startCapture(int width, int height, int device){
		try {
			this.capture = new VideoCapture(width, height,VideoCapture.getVideoDevices().get(device));
		} catch (VideoCaptureException e) {
		}
	}

	/**
	 * Given an initialised video capture, capture a {@link PImage}
	 * @return capture
	 */
	public PImage capturePImage(){
		MBFImage frame = this.capture.getNextFrame();
		return asPImage(frame);
	}
	/**
	 * Given an initialised video capture, capture a {@link PImage}
	 * @param setToCurrentFrame whether the current openimaj frame (for analysis) should be set from capture
	 * @return capture
	 */
	public PImage capturePImage(boolean setToCurrentFrame){

		MBFImage frame = this.capture.getNextFrame();
		if(setToCurrentFrame){
			this.oiImage = frame.clone();
		}
		return asPImage(frame);
	}

	/**
	 * Capture an {@link MBFImage}
	 * @return
	 */
	public MBFImage capture(){
		MBFImage frame = this.capture.getNextFrame();
		return frame;
	}
	public MBFImage capture(boolean setToCurrentFrame){

		MBFImage frame = this.capture.getNextFrame();
		if(setToCurrentFrame){
			this.oiImage = frame.clone();
		}
		return frame;
	}

	public PImage asPImage(MBFImage frame) {
		PImage img = this.parent.createImage(frame.getWidth(), frame.getHeight(), RGB);
		img.pixels = frame.toPackedARGBPixels();
		return img;
	}

	/**
	 *
	 */
	public void pre(){
	}
	/**
	 * Updates the OpenIMAJ held {@link MBFImage} instance from the whole parent {@link PApplet}
	 */
	public void updateImage() {
		this.parent.loadPixels();
		updateImage(this.parent.pixels,this.parent.width,this.parent.height);
	}

	/**
	 * @param capture
	 */
	public void updateImage(PImage capture){
		updateImage(capture.pixels,capture.width, capture.height);
	}

	/**
	 * @param capture
	 */
	public void updateImage(MBFImage capture){
		this.oiImage = capture;
	}

	/**
	 * Updates the OpenIMAJ held {@link MBFImage} instance
	 * @param pixels the pixels to use as the MBFImage
	 * @param width the width of the image
	 * @param height the height of the image
	 */
	public void updateImage(int[] pixels,int width, int height) {
		this.oiImage = new MBFImage(pixels,width, height);
	}
	/**
	 *
	 */
	public void dispose() {
		this.oiImage = null;
	}

	public void resize(int width, int height){
		if(this.oiImage == null) return;
		this.oiImage.processInplace(new ResizeProcessor(width, height));
	}

	/**
	 * Detect faces using {@link HaarCascadeDetector}, return an {@link ArrayList} of
	 * {@link PShape} instances. Note the {@link PShape} instances have no fill and
	 * a colour: 255,0,0
	 * @return detected faces
	 */
	public ArrayList<PShape> faces(){
		ArrayList<PShape> faces = new ArrayList<PShape>();
		List<DetectedFace> detected = faceDetector.detectFaces(oiImage.flatten());
		for (DetectedFace detectedFace : detected) {
			Rectangle bounds = detectedFace.getBounds();
			PShape detectedPShape = this.parent.createShape(RECT,bounds.x,bounds.y,bounds.width,bounds.height);

			detectedPShape.setFill(false);
			detectedPShape.setStroke(this.parent.color(255f, 0, 0));
			faces.add(detectedPShape);
		}
		return faces;
	}
}
