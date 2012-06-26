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
package org.openimaj.image.processing.morphology;

import java.util.HashSet;
import java.util.Set;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.KernelProcessor;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Morphological hit-and-miss transform of connected 
 * components and (assumed binary) FImages.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HitAndMiss implements ConnectedComponentProcessor, KernelProcessor<Float, FImage> {
	/**
	 * Hit and miss operator for detecting convex corners
	 */
	public final static HitAndMiss CONVEX_CORNERS;
	static {
		CONVEX_CORNERS = new HitAndMiss(
				new StructuringElement(
						new Pixel[] {new Pixel(0,-1), new Pixel(0,0), new Pixel(1,0)}, 
						new Pixel[] {new Pixel(-1,0), new Pixel(-1,1), new Pixel(0,1)}),
						new StructuringElement(
								new Pixel[] {new Pixel(0,-1), new Pixel(0,0), new Pixel(-1,0)}, 
								new Pixel[] {new Pixel(1,0), new Pixel(1,1), new Pixel(0,1)}),
								new StructuringElement(
										new Pixel[] {new Pixel(-1,0), new Pixel(0,0), new Pixel(0,1)}, 
										new Pixel[] {new Pixel(0,-1), new Pixel(1,-1), new Pixel(1,0)}),
										new StructuringElement(
												new Pixel[] {new Pixel(0,1), new Pixel(0,0), new Pixel(1,0)}, 
												new Pixel[] {new Pixel(-1,-1), new Pixel(0,-1), new Pixel(-1,0)})
		);
	}

	protected Set<Pixel> outputPixels = new HashSet<Pixel>();
	protected StructuringElement [] elements;
	protected int cx;
	protected int cy;
	protected int sw=0;
	protected int sh=0;

	/**
	 * Construct a hit and miss operator with the given
	 * structuring elements.
	 * @param ses the structuring elements
	 */
	public HitAndMiss(StructuringElement... ses) {
		this.elements = ses;

		for (StructuringElement se : ses) {
			int [] sz = se.size();

			if (sw < sz[0]) sw = sz[0];
			if (sh < sz[1]) sh = sz[1];
		}
		cx = sw/2;
		cy = sw/2;
	}

	@Override
	public void process(ConnectedComponent cc) {
		//hit and miss transform; doesn't affect original pixels.
		outputPixels.clear();

		for (StructuringElement element : elements) {
			Set<Pixel> pixels = cc.getPixels();
			int [] se_size = element.size();
			Rectangle cc_bb = cc.calculateRegularBoundingBox();
			for (int j=(int) (cc_bb.y-se_size[1]); j<=cc_bb.y+se_size[1]+cc_bb.height; j++) {
				for (int i=(int) (cc_bb.x-se_size[0]); i<=cc_bb.x+se_size[0]+cc_bb.width; i++) {
					Pixel p = new Pixel(i, j);

					if (element.matches(p, pixels)) {
						outputPixels.add(p);
					}
				}
			}
		}
	}

	/**
	 * Get the pixels selected by the hit and miss operator
	 * @return a list of selected pixels
	 */
	public Set<Pixel> getPixels() {
		return outputPixels;
	}

	@Override
	public int getKernelHeight() {
		return sh;
	}

	@Override
	public int getKernelWidth() {
		return sw;
	}

	@Override
	public Float processKernel(FImage patch) {

		for (StructuringElement element : elements) {
			int count = 0;
			for (Pixel p : element.positive) {
				int px = cx - p.x;
				int py = cy - p.y;
				if (px>=0 && py>=0 && px<sw && py<sh && patch.pixels[py][px] == 1)
					count++;
			}

			for (Pixel p : element.negative) {
				int px = cx - p.x;
				int py = cy - p.y;
				if (px>=0 && py>=0 && px<sw && py<sh && patch.pixels[py][px] == 0)
					count++;
			}
			
			if (count == element.positive.size()+element.negative.size()) return 1f;
		}

		return 0f;
	}
}
