package org.openimaj.demos.sandbox.tldcpp.videotld;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class TLDKeyListener implements KeyListener {
	private TLDMain tldMain;

	public TLDKeyListener(TLDMain tldMain) {
		this.tldMain = tldMain;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		handle(e.getKeyChar());
	}

	private void handle(char key) {
		if (key == 'q') {
			System.exit(0);
		}

		if (key == 'b') {

			// ForegroundDetector* fg = tld.detectorCascade.foregroundDetector;
			//
			// if(fg.bgImg.empty()) {
			// fg.bgImg = cvCloneImage(grey);
			// } else {
			// fg.bgImg.release();
			// }
		}

		if (key == 'c') {
			tldMain.queueClear();
		}

		if (key == 'l') {
			tldMain.toggleLearning();
			// tld.learningEnabled = !tld.learningEnabled;
			// System.out.printf("LearningEnabled: %d\n", tld.learningEnabled);
		}

		if (key == 'a') {
			tldMain.toggleAlternating();
			// tld.alternating = !tld.alternating;
			// System.out.printf("alternating: %d\n", tld.alternating);
		}

		if (key == 'e') {
			// tld.writeToFile(modelExportFile);
		}

		if (key == 'i') {
			// tld.readFromFile(modelPath);
		}

		if (key == 'r') {
			tldMain.initiateObjectSelect();
		}
		if (key == 'm') {
			tldMain.toggleMarkerMode();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
