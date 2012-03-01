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
package org.openimaj.ml.neuralnet;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.JFrame;

import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.typography.hershey.HersheyFont;

class HandWritingInputDisplay implements KeyListener{

	private double[][] imageValues;
	private int currentImageIndex;
	private FImage currentImage;
	private ResizeProcessor rp;
	private int[] numberValues;

	public HandWritingInputDisplay(double[][] xVals, int[] yVals) {
		this.imageValues = xVals;
		this.numberValues = yVals;
		this.currentImageIndex = 0;
		
		rp = new ResizeProcessor(200, 200);
		JFrame frame = DisplayUtilities.displayName(this.getCurrentImage(), "numbers");
		frame.addKeyListener(this);
	}

	public HandWritingInputDisplay(MLDataSet training) {
		this.imageValues = new double[(int) training.getRecordCount()][];
		this.numberValues = new int[(int) training.getRecordCount()];
		
		int index = 0;
		for (MLDataPair mlDataPair : training) {
			this.imageValues[index] = mlDataPair.getInputArray();
			int yIndex = 0;
			while(mlDataPair.getIdealArray()[yIndex]!=1)yIndex++;
			this.numberValues[index] = (yIndex + 1) % 10;
			index++;
		}
		
		this.currentImageIndex = 0;
		rp = new ResizeProcessor(200, 200);
		JFrame frame = DisplayUtilities.displayName(this.getCurrentImage(), "numbers");
		frame.addKeyListener(this);
	}

	private MBFImage getCurrentImage() {
		if(imageValues.length<1)return null;
		double[] imageDoubles = imageValues[this.currentImageIndex];
		int wh = (int) Math.sqrt(imageDoubles.length);
		int i = 0;
		if(this.currentImage == null)
			this.currentImage = new FImage(wh,wh);
		for (int x = 0; x < wh; x++) {
			for (int y = 0; y < wh; y++) {
				this.currentImage.pixels[y][x] = (float) imageDoubles[i++];
			}
		}
		MBFImage toDraw = this.currentImage.normalise().process(rp).toRGB();
		toDraw.drawText("Guess: " + this.numberValues[this.currentImageIndex],10, 30, HersheyFont.ASTROLOGY	, 20, RGBColour.RED);
		return toDraw;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if(e.getKeyChar() == 'e'){
			this.currentImageIndex = new Random().nextInt(this.imageValues.length);
		}
		else if(e.getKeyChar() == 'q'){
			this.currentImageIndex = this.currentImageIndex > 0 ? this.currentImageIndex - 1: 0;
		}
		else if(e.getKeyChar() == 'w'){
			this.currentImageIndex = this.currentImageIndex < this.imageValues.length - 1 ? this.currentImageIndex + 1 : this.imageValues.length - 1;
		}
		DisplayUtilities.displayName(this.getCurrentImage(), "numbers");
	}

	@Override
	public void keyPressed(KeyEvent e) {
//		keyTyped(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}