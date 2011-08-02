package org.openimaj.image.processing.segmentation;

import gnu.trove.TObjectFloatHashMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processor.connectedcomponent.render.BlobRenderer;
import org.openimaj.util.graph.WeightedEdge;
import org.openimaj.util.set.DisjointSetForest;

/**
 * Implementation of the segmentation algorithm described in:
 * Efficient Graph-Based Image Segmentation
 * Pedro F. Felzenszwalb and Daniel P. Huttenlocher
 * International Journal of Computer Vision, 59(2) September 2004.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class FelzenszwalbHuttenlocherSegmentator {
	// dissimilarity measure between pixels
	static float diff(MBFImage image, int x1, int y1, int x2, int y2) {
		float dr = image.getBand(0).pixels[y1][x1] - image.getBand(0).pixels[y2][x2];
		float dg = image.getBand(1).pixels[y1][x1] - image.getBand(1).pixels[y2][x2];
		float db = image.getBand(2).pixels[y1][x1] - image.getBand(2).pixels[y2][x2];

		return (float) Math.sqrt(dr*dr + dg*dg + db*db);
	}

	/*
	 * Segment an image
	 *
	 * Returns a color image representing the segmentation.
	 *
	 * im: image to segment.
	 * sigma: to smooth the image.
	 * c: constant for treshold function.
	 * min_size: minimum component size (enforced by post-processing stage).
	 * num_ccs: number of connected components in the segmentation.
	 */
	List<ConnectedComponent> segment(MBFImage im, float sigma, float c, int min_size) {
		int width = im.getWidth();
		int height = im.getHeight();

		MBFImage smooth = im.process(new FGaussianConvolve(sigma));

		// build graph
		List<WeightedEdge<Pixel>> edges = new ArrayList<WeightedEdge<Pixel>>();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (x < width-1) {
					WeightedEdge<Pixel> p = new WeightedEdge<Pixel>();
					p.from = new Pixel(x, y);
					p.to = new Pixel(x+1, y);
					p.weight = diff(smooth, x, y, x+1, y);
					edges.add(p);
				}

				if (y < height-1) {
					WeightedEdge<Pixel> p = new WeightedEdge<Pixel>();
					p.from = new Pixel(x, y);
					p.to = new Pixel(x, y+1);
					p.weight = diff(smooth, x, y, x, y+1);
					edges.add(p);
				}

				if ((x < width-1) && (y < height-1)) {
					WeightedEdge<Pixel> p = new WeightedEdge<Pixel>();
					p.from = new Pixel(x, y);
					p.to = new Pixel(x+1, y+1);
					p.weight = diff(smooth, x, y, x+1, y+1);
					edges.add(p);
				}

				if ((x < width-1) && (y > 0)) {
					WeightedEdge<Pixel> p = new WeightedEdge<Pixel>();
					p.from = new Pixel(x, y);
					p.to = new Pixel(x+1, y-1);
					p.weight = diff(smooth, x, y, x+1, y-1);
					edges.add(p);
				}
			}
		}

		// segment
		DisjointSetForest<Pixel> u = segmentGraph(width*height, edges, c);
		
		
		// post process small components
		for (int i = 0; i < edges.size(); i++) {
			Pixel a = u.find(edges.get(i).from);
			Pixel b = u.find(edges.get(i).to);
			
			if ((a != b) && ((u.size(a) < min_size) || (u.size(b) < min_size)))
				u.union(a, b);
		}
		
		Set<Set<Pixel>> subsets = u.getSubsets();
		List<ConnectedComponent> ccs = new ArrayList<ConnectedComponent>();
		for (Set<Pixel> sp : subsets) ccs.add(new ConnectedComponent(sp));
		
		return ccs;
	}

	/*
	 * Segment a graph
	 *
	 * Returns a disjoint-set forest representing the segmentation.
	 *
	 * num_vertices: number of vertices in graph.
	 * num_edges: number of edges in graph
	 * edges: array of edges.
	 * c: constant for threshold function.
	 */
	DisjointSetForest<Pixel> segmentGraph(int num_vertices, List<WeightedEdge<Pixel>> edges, float c) { 
		// sort edges by weight
		Collections.sort(edges, WeightedEdge.ASCENDING_COMPARATOR);

		// make a disjoint-set forest
		DisjointSetForest<Pixel> u = new DisjointSetForest<Pixel>(num_vertices);

		for (WeightedEdge<Pixel> edge : edges) {
			u.add(edge.from);
			u.add(edge.to);
		}
		
		// init thresholds
		TObjectFloatHashMap<Pixel> threshold = new TObjectFloatHashMap<Pixel>();
		for (Pixel p : u) {
			threshold.put(p, c);
		}

		// for each edge, in non-decreasing weight order...
		for (int i = 0; i < edges.size(); i++) {
			WeightedEdge<Pixel> pedge = edges.get(i);

			// components connected by this edge
			Pixel a = u.find(pedge.from);
			Pixel b = u.find(pedge.to);
			if (a != b) {
				if ((pedge.weight <= threshold.get(a)) && (pedge.weight <= threshold.get(b))) {
					a = u.union(a, b);
					threshold.put(a, pedge.weight + (c / u.size(a)));
				}
			}
		}

		return u;
	}
	
	public static void main(String [] args) throws IOException {
		MBFImage img = ImageUtilities.readMBF(new File("/Users/jon/Pictures/Pictures/100_FUJI/DSCF0013.JPG"))
			.process(new ResizeProcessor(ResizeProcessor.Mode.HALF))
			.process(new ResizeProcessor(ResizeProcessor.Mode.HALF));
		
		FelzenszwalbHuttenlocherSegmentator seg = new FelzenszwalbHuttenlocherSegmentator();
		List<ConnectedComponent> ccs = seg.segment(img, 0.5f, 500f/255f, 20);
		
		MBFImage imgout = img.clone().fill(new Float[] {0f,0f,0f});
		for (ConnectedComponent cc : ccs) {
			BlobRenderer<Float[]> br = new BlobRenderer<Float[]>(imgout, RGBColour.randomColour());
			br.process(cc);
		}
		
		System.out.println(ccs.size());
		DisplayUtilities.display(img);
		DisplayUtilities.display(imgout);
	}
}
