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
package org.openimaj.demos.sandbox.image.puzzles;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import org.openimaj.demos.video.utils.PolygonDrawingListener;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;

public class ImageSelectionFrame implements KeyListener{

	private boolean polygonComplete = false;
	private PolygonDrawingListener draw;
	private MBFImage img;
	private JFrame frame;

	public ImageSelectionFrame(MBFImage img) {
		frame = DisplayUtilities.displaySimple(img);
		this.img = img.clone();
		draw = new PolygonDrawingListener();
		frame.getContentPane().addMouseListener(draw);
		frame.addKeyListener(this);
	}

	public void waitForPolygonSelection() throws InterruptedException {
		while(!polygonComplete){
			MBFImage toDraw = img.clone();
			this.draw.drawPoints(toDraw);
			DisplayUtilities.display(toDraw, frame);
			Thread.sleep(10);
		}
		frame.dispose();
	}

	public SelectedImage getSelectedImage() {
		return new SelectedImage(img,draw.getPolygon());
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			this.polygonComplete  = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}