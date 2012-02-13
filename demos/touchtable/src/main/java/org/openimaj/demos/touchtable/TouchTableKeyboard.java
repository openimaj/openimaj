package org.openimaj.demos.touchtable;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import org.openimaj.demos.touchtable.TouchTableScreen.Mode;
import org.openimaj.io.IOUtils;

public class TouchTableKeyboard implements KeyListener {
	protected TouchTableDemo demo;
	protected TouchTableScreen touchTable;

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
		else if (e.getKeyChar() == 't'){
			if (this.touchTable.mode instanceof Mode.DRAWING_TRACKED)
				this.touchTable.mode = new Mode.DRAWING(this.touchTable);
			else 
				this.touchTable.mode = new Mode.DRAWING_TRACKED(this.touchTable);
		}
		else if (e.getKeyChar() == 'm'){
			this.touchTable.mode = new Mode.SERVER(this.touchTable);
		}
		else if(e.getKeyChar() == 'p'){
			this.touchTable.mode = new Mode.PONG(this.touchTable);
		}
	}

}
