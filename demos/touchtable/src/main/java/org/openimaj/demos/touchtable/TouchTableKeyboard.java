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
		else if(e.getKeyChar() == 'c'){
			this.touchTable.clear();
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
