package org.openimaj.demos.video.videosift;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.features.TruncatedDistanceLTPFeature;
import org.openimaj.image.processing.face.parts.KEDetectedFace;
import org.openimaj.image.processing.face.parts.FKEFaceDetector;
import org.openimaj.image.processing.face.recognition.SimpleKNNRecogniser;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;

public class VideoFaceRecognition implements KeyListener {
	private VideoCapture capture;
	private VideoDisplay<MBFImage> videoFrame;

	private FKEFaceDetector engine;
	private SimpleKNNRecogniser<TruncatedDistanceLTPFeature<KEDetectedFace>, KEDetectedFace> recogniser;

	public VideoFaceRecognition() throws Exception {
		capture = new VideoCapture(320, 240);
		engine = new FKEFaceDetector();
		videoFrame = VideoDisplay.createVideoDisplay(capture);
		SwingUtilities.getRoot(videoFrame.getScreen()).addKeyListener(this);
		recogniser = new SimpleKNNRecogniser<TruncatedDistanceLTPFeature<KEDetectedFace>, KEDetectedFace>(new TruncatedDistanceLTPFeature.Factory<KEDetectedFace>(new AffineAligner()), 1);
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent key) {
		if(key.getKeyCode() == KeyEvent.VK_SPACE) {
			this.videoFrame.togglePause();
		} else if (key.getKeyChar() == 'c') {
			if (!this.videoFrame.isPaused())
				this.videoFrame.togglePause();
			
			String person = JOptionPane.showInputDialog(this.videoFrame.getScreen(), "", "", JOptionPane.QUESTION_MESSAGE);
			FImage image = Transforms.calculateIntensityNTSC(this.videoFrame.getVideo().getCurrentFrame());
			
			List<KEDetectedFace> faces = engine.detectFaces(image);
			if (faces.size() == 1) {
				recogniser.addInstance(person, faces.get(0));
			} else {
				System.out.println("Wrong number of faces found");
			}
			
			this.videoFrame.togglePause();
		} else if (key.getKeyChar() == 'q') {
			if (!this.videoFrame.isPaused())
				this.videoFrame.togglePause();
			
			FImage image = Transforms.calculateIntensityNTSC(this.videoFrame.getVideo().getCurrentFrame());
			
			List<KEDetectedFace> faces = engine.detectFaces(image);
			if (faces.size() == 1) {
				System.out.println("Looks like: " + recogniser.queryBestMatch(faces.get(0)));
			} else {
				System.out.println("Wrong number of faces found");
			}
			
			this.videoFrame.togglePause();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public static void main(String [] args) throws Exception {		
		new VideoFaceRecognition();
	}
}
