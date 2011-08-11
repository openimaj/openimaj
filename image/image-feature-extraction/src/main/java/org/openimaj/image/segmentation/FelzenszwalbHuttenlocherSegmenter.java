package org.openimaj.image.segmentation;

import gnu.trove.TObjectFloatHashMap;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processor.SinglebandImageProcessor;
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
public class FelzenszwalbHuttenlocherSegmenter<I extends Image<?,I> & SinglebandImageProcessor.Processable<Float, FImage, I>> implements Segmenter<I> {
	protected float sigma = 0.5f;
	protected float k = 500f / 255f;
	protected int minSize = 20;
	
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
		List<WeightedEdge<Pixel>> edges = new ArrayList<WeightedEdge<Pixel>>();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (x < width-1) {
					WeightedEdge<Pixel> p = new WeightedEdge<Pixel>();
					p.from = new Pixel(x, y);
					p.to = new Pixel(x+1, y);
					p.weight = diff(smooth, p.from, p.to);
					edges.add(p);
				}

				if (y < height-1) {
					WeightedEdge<Pixel> p = new WeightedEdge<Pixel>();
					p.from = new Pixel(x, y);
					p.to = new Pixel(x, y+1);
					p.weight = diff(smooth, p.from, p.to);
					edges.add(p);
				}

				if ((x < width-1) && (y < height-1)) {
					WeightedEdge<Pixel> p = new WeightedEdge<Pixel>();
					p.from = new Pixel(x, y);
					p.to = new Pixel(x+1, y+1);
					p.weight = diff(smooth, p.from, p.to);
					edges.add(p);
				}

				if ((x < width-1) && (y > 0)) {
					WeightedEdge<Pixel> p = new WeightedEdge<Pixel>();
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

	protected DisjointSetForest<Pixel> segmentGraph(int numVertices, List<WeightedEdge<Pixel>> edges) { 
		// sort edges by weight
		Collections.sort(edges, WeightedEdge.ASCENDING_COMPARATOR);

		// make a disjoint-set forest
		DisjointSetForest<Pixel> u = new DisjointSetForest<Pixel>(numVertices);

		for (WeightedEdge<Pixel> edge : edges) {
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
			WeightedEdge<Pixel> pedge = edges.get(i);

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
	
	public static void main(String [] args) throws IOException {
		//MBFImage img = ImageUtilities.readMBF(new URL("http://people.cs.uchicago.edu/~pff/segment/beach.gif"));
		MBFImage img = ImageUtilities.readMBF(new File("/Users/jsh2/Downloads/ukbench/full/ukbench00000.jpg"));
		
		FelzenszwalbHuttenlocherSegmenter<MBFImage> seg = new FelzenszwalbHuttenlocherSegmenter<MBFImage>(0.5f, 1.0f, 50);
		
		List<ConnectedComponent> ccs = seg.segment(img);
		MBFImage imgout = SegmentationUtilities.renderSegments(img.getWidth(), img.getHeight(), ccs);
		
		System.out.println(ccs.size());
		DisplayUtilities.display(img);
		DisplayUtilities.display(imgout);
	}
}
