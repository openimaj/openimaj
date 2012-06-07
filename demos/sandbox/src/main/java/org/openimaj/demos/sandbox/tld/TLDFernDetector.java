package org.openimaj.demos.sandbox.tld;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.math.IntRange;
import org.openimaj.data.RandomData;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class TLDFernDetector {

	private static int BBOX_STEP = 7;
	private static int nBIT = 1; // number of bits per feature
	private Random random;
	private double thrN;
	private int wBBOX;
	private int hBBOX;
	private int nTREES;
	private int nFEAT;
	private int nSCALE;
	private int iHEIGHT;
	private int iWIDTH;
	private int[] BBOX;
	private int[] OFF;
	private List<List<Double>> WEIGHT;
	private List<List<Integer>> nP,nN;
	private TLDOptions opts;
	private FImage IIMG;
	private FImage IIMG2;
	private int mBBOX;
	private int nBBOX;
	/**
	 * initialise the weight and 
	 */
	public TLDFernDetector(TLDOptions opts) {
		this.WEIGHT = new ArrayList<List<Double>>();
		this.nP = new ArrayList<List<Integer>>();
		this.nN = new ArrayList<List<Integer>>();
		this.opts = opts;
	}

	/**
	 * Reset and cleanup all internals
	 */
	public void cleanup() {
		this.random = new Random(0); // fix state of random generator

		this.thrN = 0;this.wBBOX = 0; this.hBBOX = 0; this.nTREES = 0; this.nFEAT = 0; this.nSCALE = 0; this.iHEIGHT = 0; this.iWIDTH = 0;

		BBOX = null;
		OFF = null;
		IIMG = null;
		IIMG2 = null;
		WEIGHT.clear();
		nP.clear();
		nN.clear();
		return;
	}
	
	public void init(FImage frame){
		iHEIGHT    = frame.height;
		iWIDTH     = frame.width;
		nTREES     = (Integer)opts.model.get("num_trees");
		nFEAT      = (Integer)opts.model.get("num_features"); // feature has 2 points: x1,y1,x2,y2
		thrN       = 0.5 * nTREES;
		nSCALE     = this.opts.scales[0].length;

		IIMG       = frame.clone();
		IIMG2      = frame.clone();

		// BBOX
		mBBOX      = opts.grid.length; 
		nBBOX      = opts.grid[0].length;
		BBOX	   = create_offsets_bbox(opts.grid);
		double [][] x  = this.opts.features.x.getArray();
		double [][] s  = this.opts.scales;
		OFF		   = create_offsets(s,x);

		int capacity = (int) Math.pow(2, nBIT*nFEAT);
		for (int i = 0; i<nTREES; i++) {
			WEIGHT.add(new ArrayList<Double>(capacity)); // WEIGHT.push_back(vector<double>(pow(2.0,nBIT*nFEAT), 0));
			nP.add(new ArrayList<Integer>(capacity));
			nN.add(new ArrayList<Integer>(capacity));
		}

		for (int i = 0; i<nTREES; i++) {
			List<Double> WEIGHTi = WEIGHT.get(i);
			List<Integer> nPi = nP.get(i);
			List<Integer> nNi = nN.get(i);
			for (int j = 0; j < WEIGHT.get(i).size(); j++) {
				WEIGHTi.set(j,0d);
				nPi.set(j,0);
				nNi.set(j,0);
			}
		}

		return;
	}

	private int[] create_offsets(double[][] scale0, double[][] x0) {
		int [] offsets = new int[nSCALE*nTREES*nFEAT*2];
		int off = 0;

		for (int k = 0; k < nSCALE; k++){
			double []scale = scale0[2*k];
			for (int i = 0; i < nTREES; i++) {
				for (int j = 0; j < nFEAT; j++) {
					double [] x  = x0[4*j + (4*nFEAT)*i];
					offsets[off++] = sub2idx((scale[0])*x[1],(scale[1])*x[0],iHEIGHT);
					offsets[off++] = sub2idx((scale[0])*x[3],(scale[1])*x[2],iHEIGHT);
				}
			}
		}
		return offsets;
	}

	private int[] create_offsets_bbox(double [][]bb0) {

		int []offsets = new int[BBOX_STEP*nBBOX];
		int off = 0;

		for (int i = 0; i < nBBOX; i++) {
			double[] bb = bb0[mBBOX*i];
			offsets[off++] = sub2idx(bb[1],bb[0],iHEIGHT);
			offsets[off++] = sub2idx(bb[3],bb[0],iHEIGHT);
			offsets[off++] = sub2idx(bb[1],bb[2],iHEIGHT);
			offsets[off++] = sub2idx(bb[3],bb[2],iHEIGHT);
			offsets[off++] = (int) ((bb[2]-bb[0])*(bb[3]-bb[1]));
			offsets[off++] = (int) (bb[4])*2*nFEAT*nTREES; // pointer to features for this scale
			offsets[off++] = (int) bb[5]; // number of left-right bboxes, will be used for searching neighbours
		}
		return offsets;
	}

	private int sub2idx(double row, double col, int height) {
		return ((int) (Math.floor((row)+0.5) + Math.floor((col)+0.5)*(height)));
	}

}
