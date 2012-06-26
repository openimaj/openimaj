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
package org.openimaj.image.segmentation;

import gnu.trove.map.hash.TObjectFloatHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.graph.SimpleWeightedEdge;
import org.openimaj.util.set.DisjointSetForest;

/**
 * Implementation of the segmentation algorithm described in:
 * Efficient Graph-Based Image Segmentation
 * Pedro F. Felzenszwalb and Daniel P. Huttenlocher
 * International Journal of Computer Vision, 59(2) September 2004.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <I> Type of {@link Image}
 */
@Reference(
		type = ReferenceType.Article,
		author = {"Felzenszwalb, Pedro F.", "Huttenlocher, Daniel P."},
		title = "Efficient Graph-Based Image Segmentation",
		journal = "Int. J. Comput. Vision",
		volume = "59",
		number = "2",
		month = "September",
		year = "2004",
		pages = {"167","181"},
		url = "http://dx.doi.org/10.1023/B:VISI.0000022288.19776.77",
		publisher = "Kluwer Academic Publishers"
)
public class FelzenszwalbHuttenlocherSegmenter<I extends Image<?,I> & SinglebandImageProcessor.Processable<Float, FImage, I>> implements Segmenter<I> {
	protected float sigma = 0.5f;
	protected float k = 500f / 255f;
	protected int minSize = 50;

	/**
	 * Default constructor
	 */
	public FelzenszwalbHuttenlocherSegmenter() {}

	/**
	 * Construct with the given parameters
	 * @param sigma amount of blurring
	 * @param k threshold
	 * @param minSize minimum allowed component size
	 */
	public FelzenszwalbHuttenlocherSegmenter(float sigma, float k, int minSize) {
		this.sigma = sigma;
		this.k = k;
		this.minSize = minSize;
	}

	@Override
	public List<ConnectedComponent> segment(I image) {
		if (((Object)image) instanceof MBFImage) {
			return segmentImage((MBFImage)((Object)image));
		} else {
			return segmentImage(new MBFImage((FImage)((Object)image)));
		}
	}

	private float diff(MBFImage image, Pixel p1, Pixel p2) {
		float sum = 0;

		for (FImage band : image.bands) {
			float d = band.pixels[p1.y][p1.x] - band.pixels[p2.y][p2.x];
			sum += d*d;
		}

		return (float) Math.sqrt(sum);
	}

	protected List<ConnectedComponent> segmentImage(MBFImage im) {
		int width = im.getWidth();
		int height = im.getHeight();

		MBFImage smooth = im.process(new FGaussianConvolve(sigma));

		// build graph
		List<SimpleWeightedEdge<Pixel>> edges = new ArrayList<SimpleWeightedEdge<Pixel>>();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (x < width-1) {
					SimpleWeightedEdge<Pixel> p = new SimpleWeightedEdge<Pixel>();
					p.from = new Pixel(x, y);
					p.to = new Pixel(x+1, y);
					p.weight = diff(smooth, p.from, p.to);
					edges.add(p);
				}

				if (y < height-1) {
					SimpleWeightedEdge<Pixel> p = new SimpleWeightedEdge<Pixel>();
					p.from = new Pixel(x, y);
					p.to = new Pixel(x, y+1);
					p.weight = diff(smooth, p.from, p.to);
					edges.add(p);
				}

				if ((x < width-1) && (y < height-1)) {
					SimpleWeightedEdge<Pixel> p = new SimpleWeightedEdge<Pixel>();
					p.from = new Pixel(x, y);
					p.to = new Pixel(x+1, y+1);
					p.weight = diff(smooth, p.from, p.to);
					edges.add(p);
				}

				if ((x < width-1) && (y > 0)) {
					SimpleWeightedEdge<Pixel> p = new SimpleWeightedEdge<Pixel>();
					p.from = new Pixel(x, y);
					p.to = new Pixel(x+1, y-1);
					p.weight = diff(smooth, p.from, p.to);
					edges.add(p);
				}
			}
		}

		// segment
		DisjointSetForest<Pixel> u = segmentGraph(width*height, edges);


		// post process small components
		for (int i = 0; i < edges.size(); i++) {
			Pixel a = u.find(edges.get(i).from);
			Pixel b = u.find(edges.get(i).to);

			if ((a != b) && ((u.size(a) < minSize) || (u.size(b) < minSize)))
				u.union(a, b);
		}

		Set<Set<Pixel>> subsets = u.getSubsets();
		List<ConnectedComponent> ccs = new ArrayList<ConnectedComponent>();
		for (Set<Pixel> sp : subsets) ccs.add(new ConnectedComponent(sp));

		return ccs;
	}

	protected DisjointSetForest<Pixel> segmentGraph(int numVertices, List<SimpleWeightedEdge<Pixel>> edges) { 
		// sort edges by weight
		Collections.sort(edges, SimpleWeightedEdge.ASCENDING_COMPARATOR);

		// make a disjoint-set forest
		DisjointSetForest<Pixel> u = new DisjointSetForest<Pixel>(numVertices);

		for (SimpleWeightedEdge<Pixel> edge : edges) {
			u.add(edge.from);
			u.add(edge.to);
		}

		// init thresholds
		TObjectFloatHashMap<Pixel> threshold = new TObjectFloatHashMap<Pixel>();
		for (Pixel p : u) {
			threshold.put(p, k);
		}

		// for each edge, in non-decreasing weight order...
		for (int i = 0; i < edges.size(); i++) {
			SimpleWeightedEdge<Pixel> pedge = edges.get(i);

			// components connected by this edge
			Pixel a = u.find(pedge.from);
			Pixel b = u.find(pedge.to);
			if (a != b) {
				if ((pedge.weight <= threshold.get(a)) && (pedge.weight <= threshold.get(b))) {
					a = u.union(a, b);
					threshold.put(a, pedge.weight + (k / u.size(a)));
				}
			}
		}

		return u;
	}
}
