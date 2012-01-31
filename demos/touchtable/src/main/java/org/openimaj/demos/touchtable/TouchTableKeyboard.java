package org.openimaj.demos.touchtable;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import org.openimaj.io.IOUtils;

public class TouchTableKeyboard implements KeyListener {

	private TouchTableDemo demo;
	private TouchTableScreen touchTable;

	public TouchTableKeyboard(TouchTableDemo touchTableDemo, TouchTableScreen touchTableScreen) {
		this.demo = touchTableDemo;
		this.touchTable = touchTableScreen;
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if(e.getKeyChar() == 's'){
			System.out.println("Writing config!");
			try {
				IOUtils.writeASCII(new File("camera.conf"), this.touchTable.cameraConfig);
				System.out.println("Camera config written");
			} catch (Exception e1) {
				System.out.println("Failed to write camera.conf: " + e1.getMessage());
			}
		}
		else if (e.getKeyChar() == 'l'){
			System.out.println("Loading config!");
			try {
				TriangleCameraConfig newCC = IOUtils.read(new File("camera.conf"), new TriangleCameraConfig());
				this.touchTable.setCameraConfig(newCC);
				System.out.println("Read camera config");
			} catch (Exception e1) {
				System.out.println("Failed to read camera config");
			}
		}
	}

}
