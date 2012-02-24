package org.openimaj.math.geometry.shape;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.algorithm.GeneralisedProcrustesAnalysis;
import org.openimaj.math.geometry.shape.algorithm.ProcrustesAnalysis;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis;
import org.openimaj.math.matrix.algorithm.pca.SvdPrincipalComponentAnalysis;

import Jama.Matrix;

/**
 * A 2d point distribution model learnt from a set of {@link PointList}s with
 * corresponding points (the ith point in each {@link PointList} is the same
 * landmark).
 * 
 * The pdm models the mean shape and the variance from the mean of the
 * top N principal components. The model is generative and can generate new
 * shapes from a scaling vector. To ensure that newly generated shapes are 
 * plausible, scaling vectors have {@link Constraint}s applied to them. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class PointDistributionModel {
	public interface Constraint {
		public double [] apply(double [] in, double [] lamda);
	}
	
	public class NullConstraint implements Constraint {
		@Override
		public double[] apply(double[] in, double [] lamda) {
			return in;
		}
	}
	
	public class BoxConstraint implements Constraint {
		double sigma;
		
		public BoxConstraint(double sigma) {
			this.sigma = sigma;
		}
		
		@Override
		public double[] apply(double[] in, double [] lamda) {
			double[] out = new double[in.length];
			
			for (int i=0; i<in.length; i++) {
				double w = sigma * Math.sqrt(lamda[i]);
				out[i] = in[i] > w ? w : in[i] < -w ? -w : in[i];
			}
			
			return out;
		}
	}
	
	public class EllipsoidConstraint implements Constraint {
		double dmax;
		
		@Override
		public double[] apply(double[] in, double [] lamda) {
			double dmsq = 0;
			for (int i=0; i<in.length; i++) {
				dmsq += in[i] * in[i] / lamda[i];
			}
			
			if (dmsq < dmax*dmax) {
				return in;
			}
			
			double sc = dmax / Math.sqrt(dmsq);
			double[] out = new double[in.length];
			for (int i=0; i<in.length; i++) {
				out[i] = in[i] * sc;
			}
			
			return out;
		}
	}
	
	protected Constraint constraint = new NullConstraint();
	protected PrincipalComponentAnalysis pc;
	protected PointList mean;
	protected int numComponents;

	public PointDistributionModel(List<PointList> data) {
		//align
		mean = GeneralisedProcrustesAnalysis.alignPoints(data, 5, 10);
		
		//build data matrix
		Matrix m = buildDataMatrix(data);
		
		//perform pca
		this.pc = new SvdPrincipalComponentAnalysis();
		pc.learnBasis(m);
		
		numComponents = this.pc.getEigenValues().length;
	}
	
	private Matrix buildDataMatrix(PointList data) {
		List<PointList> pls = new ArrayList<PointList>(1);
		pls.add(data);
		return buildDataMatrix(pls);
	}
	
	private Matrix buildDataMatrix(List<PointList> data) {
		final int nData = data.size();
		final int nPoints = data.get(0).size();
		
		Matrix m = new Matrix(nData, nPoints * 2);
		double[][] mData = m.getArray();
		
		for (int i=0; i<nData; i++) {
			PointList pts = data.get(i);
			for (int j=0, k=0; k<nPoints; j+=2, k++) {
				Point2d pt = pts.points.get(k);
				
				mData[i][j] = pt.getX();
				mData[i][j+1] = pt.getY();
			}
		}
		
		return m;
	}

	public PointList getMean() {
		return mean;
	}
	
	public void setNumComponents(int n) {
		pc.selectSubset(n);
		numComponents = this.pc.getEigenValues().length;
	}
	
	public PointList generateNewShape(double [] scaling) {
		PointList newShape = new PointList();
		
		double[] pts = pc.generate(constraint.apply(scaling, pc.getEigenValues()));
		
		for (int i=0; i<pts.length; i+=2) {
			float x = (float) pts[i];
			float y = (float) pts[i+1];
			
			newShape.points.add(new Point2dImpl(x, y));
		}
		
		return newShape;
	}
	
	public double [] getStandardDeviations(double sigma) {
		double[] rngs = pc.getStandardDeviations();
		
		for (int i = 0; i < rngs.length; i++) {
			rngs[i] = rngs[i] * sigma;
		}
		
		return rngs;
	}
	
	public double [] fitModel(PointList observed) {
		double [] model = new double[numComponents];
		double delta = 1.0;
		
		while (delta > 1e-6) {
			PointList instance = this.generateNewShape(model);
			
			ProcrustesAnalysis pa = new ProcrustesAnalysis(observed);
			Matrix pose = pa.align(instance);
			
			PointList projected = observed.transform(pose.inverse());

			//TODO: tangent???
			
			Matrix y = buildDataMatrix(projected);
			Matrix xbar = new Matrix(new double[][] { pc.getMean() });
//			double[] newModel = pc.getBasis().transpose().times((y.minus(xbar)).transpose()).getColumnPackedCopy();
			double[] newModel = (y.minus(xbar)).times(pc.getBasis()).getArray()[0];
			
			newModel = constraint.apply(newModel, pc.getEigenValues());
			
			delta = 0;
			for (int i=0; i<newModel.length; i++)
				delta += (newModel[i] - model[i])*(newModel[i] - model[i]);
			delta = Math.sqrt(delta);
			
			model = newModel;
		}
		
		return model;
	}
}
