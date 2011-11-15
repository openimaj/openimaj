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
package org.openimaj.demos.sandbox;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.data.RandomData;
import org.openimaj.feature.local.matcher.BasicTwoWayMatcher;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.model.UnivariateGaussianNaiveBayesModel;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

public class LearnGeometricModel implements KeyListener {
	static final int N_INSTANCES = 15;
	static final int N_PERSON = 50;
	
	int nPersons = 10;
	int nSamples = 5;

	DetectedFace [][] faces;
	JFrame frame = DisplayUtilities.createNamedWindow("test", "Match", true);
	
	public LearnGeometricModel() throws IOException {
		loadData();
		buildMatches();
		
		frame.addKeyListener(this);
		displayNext();
	}
	
	private void loadData() throws IOException {
		int [] persons = RandomData.getUniqueRandomInts(10, 1, N_PERSON+1);
		String basedir = "/Volumes/Raid/face_databases/gt_db";
		faces = new DetectedFace[nPersons][nSamples];
		HaarCascadeDetector det = HaarCascadeDetector.BuiltInCascade.frontalface_alt2.load();
		
		for (int p=0; p<persons.length; p++) {
			int [] ids = RandomData.getUniqueRandomInts(N_INSTANCES, 1, N_INSTANCES+1);
			
			int i=0, j=0;
			while (i<nSamples) {
				File imagefile = new File(basedir , String.format("s%02d/%02d.jpg", persons[p], ids[j]));
				FImage img = ImageUtilities.readF(imagefile);
				img = ResizeProcessor.halfSize(img);
				List<DetectedFace> face = det.detectFaces(img);
				
				j++;
				if (face.size() == 1) {					
					faces[p][i] = face.get(0);
					i++;
				}
			}
		}
	}

	class Match {
		boolean correct;
		MBFImage image;
		double distance;
	}
	
	List<Match> matches = new ArrayList<Match>();
	Match current;
	Iterator<Match> iter;
	
	private void buildMatches() {
		Rectangle unit = new Rectangle(0,0,1,1);
		DoGSIFTEngine engine = new DoGSIFTEngine();
		
		for (int p=0; p<nPersons; p++) {
			for (int j=0; j<nSamples; j++) { 
				for (int i=j; i<nSamples; i++) {
					FImage im1 = faces[p][i].getFacePatch();
					FImage im2 = faces[p][j].getFacePatch();
					
					int newwidth = im1.getWidth() + im2.getWidth();
					int newheight = Math.max(im1.getHeight(), im2.getHeight());
					
					FImage outG = im1.newInstance(newwidth, newheight);
					outG.drawImage(im1, 0, 0);
					outG.drawImage(im2, im1.getWidth(), 0);
					MBFImage out = new MBFImage(outG, outG, outG);
					
					Matrix transform1 = TransformUtilities.makeTransform(im1.getBounds(), unit);
					Matrix transform2 = TransformUtilities.makeTransform(im2.getBounds(), unit);
					
					BasicTwoWayMatcher<Keypoint> matcher = new BasicTwoWayMatcher<Keypoint>();
					matcher.setModelFeatures(engine.findFeatures(im1));
					matcher.findMatches(engine.findFeatures(im2));
					
					for (Pair<Keypoint> match : matcher.getMatches()) {
						MBFImage img = out.clone();
						img.drawLine((int)match.firstObject().getX() + im1.getWidth(), 
								(int)match.firstObject().getY(), 
								(int)match.secondObject().getX(), 
								(int)match.secondObject().getY(),
								RGBColour.RED);
						
						Match m = new Match();
						m.image = img;
						m.distance = Line2d.distance(match.firstObject().transform(transform1), match.secondObject().transform(transform2));
						m.correct = (i == j && match.firstObject().getX() == match.secondObject().getX() && match.firstObject().getY() == match.secondObject().getY());
						matches.add(m);
					}
				}
			}
		}
		iter = matches.iterator();
	}

	private void displayNext() {
		if (!iter.hasNext()) {
			finish();
			return;
		}
		current = iter.next();
		
		if (current.correct) displayNext(); //move on if its already been marked
		
		DisplayUtilities.displayName(current.image, "test");
	}

	private void finish() {
		UnivariateGaussianNaiveBayesModel<Boolean> model = new UnivariateGaussianNaiveBayesModel<Boolean>();
		
		List<IndependentPair<Double, Boolean>> data = new ArrayList<IndependentPair<Double, Boolean>>();
		for (Match m : matches) {
			data.add(new IndependentPair<Double, Boolean>(m.distance, m.correct));
		}
		model.estimate(data);
		
		System.out.println("Results");
		System.out.println("Class distributions:");
		System.out.println(model.getClassDistribution());
		System.out.println("Class priors:");
		System.out.println(model.getClassPriors());
		
		System.exit(0);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == 't') {
			current.correct = true;
			System.out.println("match marked as true (distance=" + current.distance + ")");
			displayNext();
		} else if (e.getKeyChar() == 'f') {
			current.correct = false;
			System.out.println("match marked as false (distance=" + current.distance + ")");
			displayNext();
		}
	}


	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		new LearnGeometricModel();
	}
}
