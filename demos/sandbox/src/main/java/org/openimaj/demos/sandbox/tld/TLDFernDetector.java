package org.openimaj.demos.sandbox.tld;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;

public class TLDFernDetector {

	private Random random;
	private double thrN;
	private int wBBOX;
	private int hBBOX;
	private int nTREES;
	private int nFEAT;
	private int nSCALE;
	private int iHEIGHT;
	private int iWIDTH;
	private Rectangle BBOX;
	private FImage integralImage;
	private FImage integralImage2;
	private Rectangle OFF;
	private List<List<Double>> WEIGHT;
	private List<List<Integer>> nP,nN;
	private TLDOptions opts;
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
		integralImage = null;
		integralImage2 = null;
		WEIGHT.clear();
		nP.clear();
		nN.clear();
		return;
	}
	
	public void init(FImage frame, Rectangle boundingBox){
		int min_win = (Integer) opts.model.get("min_win");
		
	}

}
