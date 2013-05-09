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
package org.openimaj.feature.local.matcher;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.Image;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;


/**
 * Drawing utility useful for drawing two images and the matches between their feature points
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class MatchingUtilities {
	/**
	 * Draw matches between two images in the given colour.
	 * Places the images side-by-side and draws a line
	 * for each match.
	 *
	 * @param <T> Pixel type
	 * @param <I> Image type
	 * @param im1 first image
	 * @param im2 second image
	 * @param matches matched features between images
	 * @param col the colour to draw in
	 * @return image drawn on
	 */
	public static <T, I extends Image<T,I>> I drawMatches(I im1, I im2, List<? extends Pair<? extends Point2d>> matches, T col) {
		int newwidth = im1.getWidth() + im2.getWidth();
		int newheight = Math.max(im1.getHeight(), im2.getHeight());

		I out = im1.newInstance(newwidth, newheight);
		ImageRenderer<T, I> renderer = out.createRenderer();
		renderer.drawImage(im1, 0, 0);
		renderer.drawImage(im2, im1.getWidth(), 0);

		if (matches!=null) {
			for (Pair<? extends Point2d> p : matches) {
				renderer.drawLine(	(int)p.firstObject().getX() + im1.getWidth(),
								(int)p.firstObject().getY(),
								(int)p.secondObject().getX(),
								(int)p.secondObject().getY(),
								1,col);
			}
		}

		return out;
	}

	/**
	 * Draw two sets of matches between two images in the given colours.
	 * Places the images side-by-side and draws a line
	 * for each match.
	 *
	 * @param <T> Pixel type
	 * @param <I> Image type
	 * @param im1 first image
	 * @param im2 second image
	 * @param matches first set of matched features between images
	 * @param col the colour to draw first set of matches in
	 * @param matches2 second set of matched features between images
	 * @param col2 the colour to draw second set of matches in
	 * @return image drawn on
	 */
	public static <T, I extends Image<T,I>> I drawMatches(I im1, I im2, List<? extends Pair<? extends Point2d>> matches, T col, List<? extends Pair<? extends Point2d>> matches2, T col2) {
		int newwidth = im1.getWidth() + im2.getWidth();
		int newheight = Math.max(im1.getHeight(), im2.getHeight());

		I out = im1.newInstance(newwidth, newheight);
		ImageRenderer<T, I> renderer = out.createRenderer();
		renderer.drawImage(im1, 0, 0);
		renderer.drawImage(im2, im1.getWidth(), 0);

		if (matches!=null) {
			for (Pair<? extends Point2d> p : matches) {
				renderer.drawLine(	(int)p.firstObject().getX() + im1.getWidth(),
								(int)p.firstObject().getY(),
								(int)p.secondObject().getX(),
								(int)p.secondObject().getY(),
								col);
			}
		}

		if (matches2!=null) {
			for (Pair<? extends Point2d> p : matches2) {
				renderer.drawLine(	(int)p.firstObject().getX() + im1.getWidth(),
								(int)p.firstObject().getY(),
								(int)p.secondObject().getX(),
								(int)p.secondObject().getY(),
								col2);
			}
		}

		return out;
	}

	/**
	 * Draw matches between two images in the given colour. The
	 * lines representing the matches are drawn on a copy of the
	 * input image. The positions of the matches should represent
	 * valid points in the input image.
	 *
	 * @param <T> Pixel type
	 * @param <I> Image type
	 * @param image first image
	 * @param matches matched features between images
	 * @param col the colour to draw in
	 * @return image drawn on
	 */
	public static <T, I extends Image<T,I>> I drawMatches(I image, List<IndependentPair<Point2d, Point2d>> matches, T col) {
		I out = image.clone();
		ImageRenderer<T, I> renderer = out.createRenderer();

		if (matches!=null) {
			for (IndependentPair<? extends Point2d, ? extends Point2d> p  : matches) {
				renderer.drawLine(	(int)p.firstObject().getX(),
								(int)p.firstObject().getY(),
								(int)p.secondObject().getX(),
								(int)p.secondObject().getY(),
								col);
			}
		}

		return out;
	}

	static class MouseOverFeatureListener<T, I extends Image<T,I>> implements MouseMotionListener, KeyListener{
		private JFrame frame;
		private List<Pair<Keypoint>> matches;
		private T colour;
		private I im1;
		private I im2;
		private boolean allMode;

		public MouseOverFeatureListener(I im1,I im2, JFrame frame,List<Pair<Keypoint>> matches, T colour) {
			this.im1 = im1;
			this.im2 = im2;
			this.frame = frame;
			this.matches = matches;
			this.colour = colour;
			this.allMode = false;
		}

		@Override
		public void mouseDragged(MouseEvent arg0) {
			// Do nothing
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			List<Pair<Keypoint>> toDisplay = null;
			if(allMode) {
				toDisplay = this.matches;
			} else {
				Point2d mousePoint = new Point2dImpl(arg0.getX()-im1.getWidth(),arg0.getY());
				toDisplay = new ArrayList<Pair<Keypoint>>();
				for(Pair<Keypoint> kpair : matches){
					Keypoint toCompare = kpair.firstObject();
					if(Line2d.distance(mousePoint, toCompare) < 10){
						toDisplay.add(kpair);
					}
				}
			}

			I image = MatchingUtilities.drawMatches(im1, im2, toDisplay, this.colour);
			DisplayUtilities.display(image,frame);
		}

		@Override
		public void keyPressed(KeyEvent key) {
			keyTyped(key);
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// Do nothing
		}

		@Override
		public void keyTyped(KeyEvent key) {
			if(key.getKeyCode() == KeyEvent.VK_SPACE) allMode = !allMode;
		}
	}

	/**
	 * Create an interactive display of matches between two images.
	 *
	 * @param <T> Pixel type
	 * @param <I> Image type
	 * @param im1 first image
	 * @param im2 second image
	 * @param matches matched features between images
	 * @param col the colour to draw in
	 */
	public static <T, I extends Image<T,I>> void displayMouseOverMatches(I im1, I im2,List<Pair<Keypoint>> matches, T col) {
		int newwidth = im1.getWidth() + im2.getWidth();
		int newheight = Math.max(im1.getHeight(), im2.getHeight());

		I out = im1.newInstance(newwidth, newheight);
		ImageRenderer<T, I> renderer = out.createRenderer();
		renderer.drawImage(im1, 0, 0);
		renderer.drawImage(im2, im1.getWidth(), 0);

		JFrame frame = DisplayUtilities.display(out);
		MouseOverFeatureListener<T, I> mofl = new MouseOverFeatureListener<T,I>(im1, im2, frame, matches, col);
		frame.addKeyListener(mofl);
		frame.getContentPane().addMouseMotionListener(mofl);
	}
}
