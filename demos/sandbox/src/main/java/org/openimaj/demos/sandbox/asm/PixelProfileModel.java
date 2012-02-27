package org.openimaj.demos.sandbox.asm;

import java.util.Arrays;

import org.apache.commons.math.DimensionMismatchException;
import org.apache.commons.math.stat.descriptive.MultivariateSummaryStatistics;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;

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
		
		Matrix mc = new Matrix(new double[][] { meanCentered });
		Matrix mct = mc.transpose();
		
		Matrix dist = mct.times(invCovar).times(mc);
		
		return dist.get(0, 0);
	}

	public double [] computeMahalanobisWindowed(double [] vector) {
		int maxShift = vector.length - nsamples;
		
		double [] responses = new double[maxShift];
		double [] samples = new double[nsamples]; 
		for (int i=0; i<maxShift; i++) {
			System.arraycopy(vector, i, samples, 0, nsamples);
			
			responses[i] = computeMahalanobis(samples);
		}
		
		return responses;
	}
	
	public static double [] extractSamples(Line2d line, FImage image, int numSamples) {
		double[] samples = new double[numSamples];
		
		Point2d p1 = line.getBeginPoint();
		Point2d p2 = line.getEndPoint();
		float x = p1.getX();
		float y = p1.getY();
		float dxStep = (p2.getX() - x) / numSamples;
		float dyStep = (p2.getX() - y) / numSamples;
		
		for (int i=0; i<numSamples; i++) {
			samples[i] = image.getPixelInterpNative(x, y, 0);
			
			x += dxStep;
			y += dyStep;
		}
		
		return samples;
	}
}
