package org.openimaj.demos.sandbox.tldcpp;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import org.openimaj.demos.sandbox.tldcpp.TLDMain.Command;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.Mode;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class TLDMain {
	public Video<MBFImage> source;
	TLD tld;
//	Gui * gui;
	boolean showOutput;
	File printResults;
	File saveDir;
	double threshold = 0.5;
	boolean showForeground = false;
	boolean showNotConfident;
	boolean selectManually;
	Rectangle initialBB;
	boolean reinit;
	boolean exportModelAfterRun;
	boolean loadModel;
	File modelPath;
	File modelExportFile;
	int seed;
	public PrintStream resultsFile;
	public VideoDisplay<MBFImage> disp;
	public RectangleSelectionListener selector;
	public Command command;
	public TLDMain(Video<MBFImage> imageSource){
		source = imageSource;
		tld = new TLD();
	}
	void doWork() throws FileNotFoundException {
		
		MBFImage img = source.getNextFrame(); //= imAcqGetImg(imAcq);
		FImage grey = img.flatten(); // cvCreateImage( cvGetSize(img), 8, 1 )
		// cvCvtColor( img,grey, CV_BGR2GRAY );

		tld.detectorCascade.imgWidth = grey.width;
		tld.detectorCascade.imgHeight = grey.height;

		
		if(printResults != null) {
			resultsFile = new PrintStream(printResults);
		}
		
		// Start a video processor
		selector = new RectangleSelectionListener(this);
		disp = VideoDisplay.createVideoDisplay(source);
		disp.addVideoListener(new TLDVideoListener(this));
		
		JFrame rootScreen = (JFrame) SwingUtilities.windowForComponent(disp.getScreen());
		rootScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		rootScreen.addKeyListener(new TLDKeyListener(this));
		disp.getScreen().addMouseListener(selector);
		disp.getScreen().addMouseMotionListener(selector);
	}
	public void initiateObjectSelect() {
		command = Command.SELECT;
	}
	public void selectObject(Rectangle r) throws Exception{
		this.tld.selectObject(this.source.getCurrentFrame().flatten(),r);
		this.disp.setMode(Mode.PLAY);
		
	}
	
	public static void main(String[] args) throws IOException {
		TLDMain tldMain = new TLDMain(new VideoCapture(320, 240));
		TLDConfig.tldConfig(tldMain);
		tldMain.doWork();
	}
	public enum Command{
		NONE,CLEAR,LEARNING,ALTERNATING,SELECT;
	}
	public void queueClear() {
		command = Command.CLEAR;
	}
	public void toggleLearning() {
		command = Command.LEARNING;
		
	}
	public void toggleAlternating() {
		command = Command.ALTERNATING;
		
	}
}
