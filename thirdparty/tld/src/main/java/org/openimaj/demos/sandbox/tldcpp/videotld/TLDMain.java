package org.openimaj.demos.sandbox.tldcpp.videotld;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.openimaj.demos.sandbox.tldcpp.TLD;
import org.openimaj.demos.sandbox.tldcpp.tracker.RectangleSelectionListener;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.Mode;
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
	boolean markerMode = false;
	public TLDMain(Video<MBFImage> imageSource){
		source = imageSource;
		tld = new TLD(imageSource.getWidth(),imageSource.getHeight());
	}
	void doWork() throws FileNotFoundException {
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
		TLDMain tldMain = new TLDMain(new VideoCapture(320,240));
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
	public void toggleMarkerMode() {
		this.markerMode  = !markerMode;
	}
}
