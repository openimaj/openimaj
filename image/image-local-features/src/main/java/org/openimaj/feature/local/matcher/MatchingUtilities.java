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

import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;


public class MatchingUtilities {
	public static <T, I extends Image<T,I>> I drawMatches(I im1, I im2, List<? extends Pair<? extends Point2d>> matches, T col) {
		int newwidth = im1.getWidth() + im2.getWidth();
		int newheight = Math.max(im1.getHeight(), im2.getHeight());
		
		I out = im1.newInstance(newwidth, newheight);
		out.drawImage(im1, 0, 0);
		out.drawImage(im2, im1.getWidth(), 0);

		if (matches!=null) {
			for (Pair<? extends Point2d> p : matches) {
				out.drawLine(	(int)p.firstObject().getX() + im1.getWidth(), 
								(int)p.firstObject().getY(), 
								(int)p.secondObject().getX(), 
								(int)p.secondObject().getY(),
								col);
			}
		}
		
		return out;
	}
	
	public static <T, I extends Image<T,I>> I drawMatches(I image, List<IndependentPair<Point2d, Point2d>> list, T white) {
		
		I out = image.clone();

		if (list!=null) {
			for (IndependentPair<Point2d, Point2d> p  : list) {
				out.drawLine(	(int)p.firstObject().getX(), 
								(int)p.firstObject().getY(), 
								(int)p.secondObject().getX(), 
								(int)p.secondObject().getY(),
								white);
			}
		}
		
		return out;
	}
}
