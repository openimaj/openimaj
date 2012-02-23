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
import org.openimaj.image.feature.local.interest.HessianIPD;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.typography.hershey.HersheyFont;

import Jama.Matrix;

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