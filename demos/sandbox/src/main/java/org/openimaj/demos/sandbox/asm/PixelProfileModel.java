package org.openimaj.demos.sandbox.asm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.DimensionMismatchException;
import org.apache.commons.math.random.MersenneTwister;
import org.apache.commons.math.stat.descriptive.MultivariateSummaryStatistics;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IndependentPair;

import cern.jet.random.Normal;

import Jama.Matrix;

public class PixelProfileModel {
	private MultivariateSummaryStatistics statistics;
	private int nsamples;
	
	private double [] mean;
	private Matrix invCovar;

	public PixelProfileModel(int nsamples) {
		this.nsamples = nsamples;
		this.statistics = new MultivariateSummaryStatistics(nsamples, true);
	}
	
	public void addSample(FImage image, Line2d line) {
		double [] samples = extractSamples(line, image, nsamples);
		try {
			statistics.addValue(samples);
		} catch (DimensionMismatchException e) {
			throw new RuntimeException(e);
		}
		
		invCovar = null;
		mean = null;
	}
	
	public double computeMahalanobis(double [] vector) {
		if (mean == null) {
			mean = statistics.getMean();
			invCovar = new Matrix(statistics.getCovariance().getData()).inverse();			
		}
		
		double [] meanCentered = new double[mean.length];
		for (int i=0; i<mean.length; i++) {
			meanCentered[i] = vector[i] - mean[i];
		}
		
		Matrix mct = new Matrix(new double[][] { meanCentered });
		Matrix mc = mct.transpose();
		
		Matrix dist = mct.times(invCovar).times(mc);
		
		return dist.get(0, 0);
	}
	
	public double computeMahalanobis(FImage image, Line2d line) {
		double [] samples = extractSamples(line, image, nsamples);
		return computeMahalanobis(samples);
	}

	public double [] computeMahalanobisWindowed(FImage image, Line2d line, int numSamples) {
		double [] samples = extractSamples(line, image, numSamples);
		return computeMahalanobisWindowed(samples);
	}
	
	public Point2dImpl computeNewBest(FImage image, Line2d line, int numSamples) {
		double[] resp = computeMahalanobisWindowed(image, line, numSamples);
		
		int minIdx = ArrayUtils.minIndex(resp);
		int offset = (numSamples - nsamples) / 2;

		if (resp[offset] == resp[minIdx])
			return (Point2dImpl) line.getCOG();
		
		float x = line.begin.getX();
		float y = line.begin.getY();
		float dxStep = (line.end.getX() - x) / (numSamples-1);
		float dyStep = (line.end.getY() - y) / (numSamples-1);
		
		return new Point2dImpl(x + (minIdx + offset) * dxStep, y + (minIdx + offset) * dyStep);
	}
	
	public double [] computeMahalanobisWindowed(double [] vector) {
		int maxShift = vector.length - nsamples + 1;
		
		double [] responses = new double[maxShift];
		double [] samples = new double[nsamples]; 
		for (int i=0; i<maxShift; i++) {
			System.arraycopy(vector, i, samples, 0, nsamples);
			
			responses[i] = computeMahalanobis(samples);
		}
		
		return responses;
	}
	
	public static double [] extractSamples(Line2d line, FImage image, int numSamples) {
		double[] samples = new double[numSamples+2];
		
		Point2d p1 = line.getBeginPoint();
		Point2d p2 = line.getEndPoint();
		float x = p1.getX();
		float y = p1.getY();
		float dxStep = (p2.getX() - x) / (numSamples-1);
		float dyStep = (p2.getY() - y) / (numSamples-1);
		
		for (int i=0; i<numSamples+2; i++) {
			samples[i] = image.getPixelInterpNative(x, y, 0);
			
			x += dxStep;
			y += dyStep;
		}
		
		double[] dsamples = new double[numSamples];
		double sum = 0;
		for (int i=0; i<numSamples; i++) {
			dsamples[i] = samples[i] - samples[i+2];
			sum+=Math.abs(dsamples[i]);
		}
		
		if (sum == 0) return dsamples;
		
		for (int i=0; i<numSamples; i++) {
			dsamples[i] /= sum;
		}
		
		return dsamples;
	}
	
	@Override
	public String toString() {
		return "\nPixelProfileModel[\n" +
				"\tcount = "+statistics.getN()+"\n" +
				"\tmean = "+Arrays.toString(statistics.getMean())+"\n" +
				"\tcovar = "+statistics.getCovariance()+"\n" +
						"]";
	}
	
	public static void main(String[] args) {
		MersenneTwister mt = new MersenneTwister();
		
		PixelProfileModel ppm = new PixelProfileModel(5);
		for (int i=0; i<1000; i++) {
			float gl = (float) (0.5 + (mt.nextGaussian() / 10.0));
			int x = (int) (100.0 + mt.nextGaussian());
			
			FImage img = new FImage(200, 200);
			img.drawShapeFilled(new Rectangle(100,0,100,200), gl);
			
			Line2d line = new Line2d(x+2, 50, x-2, 50);

			ppm.addSample(img, line);
		}
		System.out.println(ppm);
		
		int x = 102;
		FImage img = new FImage(200, 200);
		img.drawShapeFilled(new Rectangle(100,0,100,200), 0.5f);
		while (true) {
			Line2d line = new Line2d(x+4, 50, x-4, 50);
		
			int newx = (int) ppm.computeNewBest(img, line, 9).x;
			System.out.println(x);
			
			if (newx == x)
				break;
				
			x = newx;
		}
	}
}
