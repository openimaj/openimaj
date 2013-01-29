package org.openimaj.demos.sandbox.gmm;


import java.util.Arrays;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.GammaDistribution;
import org.apache.commons.math.distribution.GammaDistributionImpl;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.special.Gamma;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;



/**
 * P( mean , variance ) = P(mean | variance) P(variance) 
 * 
 * P(mean | variance ) = Norm (mean | mean_0, variance)
 * P(variance) = Gamma (variance | a_0, b_0)
 * 
 * @author Jonathan Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk), David Duplaw (dpd@ecs.soton.ac.uk)
 *
 */
public class UnivariateGaussianModelGenerator {
	private static final double EPS = 0.000001;
	private double variance;
	private double mean0;
	private GammaDistributionImpl gamma;
	private double gammaAlpha;

	public UnivariateGaussianModelGenerator(double mean0, double a0, double b0) {
		this.gamma = new GammaDistributionImpl(a0, b0);
		this.gammaAlpha = Math.exp(Gamma.logGamma(a0));
		this.variance = a0 * b0;
		this.mean0 = mean0;
	}
	
	public double sample() throws MathException{
		double var;
		var = this.gamma.sample();
		NormalDistributionImpl norm = new NormalDistributionImpl(mean0, var);
		double mean = norm.sample();
		NormalDistributionImpl liklihood = new NormalDistributionImpl(mean, Math.sqrt(var));
		return liklihood.sample();
	}
	
	public double joinProbability(double mean, double variance) throws MathException{
		variance += EPS;
		double jp = jointProbUn(mean,variance);
		double norm = jointProbUn(this.expectedMean(),this.expectedVar());
		return jp;
	}
	
	private double jointProbUn(double mean, double variance) {
		double pvar = this.gamma.density(variance);
		double pmean_var = new NormalDistributionImpl(mean0, 1/variance).density(mean);
		return pvar * pmean_var;
	}

	public static void main(String[] args) throws MathException {
		UnivariateGaussianModelGenerator gen = new UnivariateGaussianModelGenerator(0, 3,0.2	);
		System.out.println("Expected variance: " + gen.expectedVar());
		System.out.println("Expected mean: " + gen.expectedMean());
		float[] gamm = new float[100];
		for (int i = 0; i < gamm.length; i++) {
			gamm[i] = (float) gen.gamma.density(i);
		}
		
		System.out.println(Arrays.toString(gamm));
		MBFImage img = new MBFImage(400,400,3);
		MBFImage contours = new MBFImage(400,400,3);
		Float[] ONE = RGBColour.GREEN;
		Float[] ZERO = RGBColour.RED;
		int DIM = 400;
		int CENTERX = DIM/2;
		int CENTERY = DIM;
		int CXRNG = 6;
		int CYRNG = 6;
		float WEIGHTX = DIM/(float)CXRNG;
		float WEIGHTY = DIM/(float)CYRNG;
		double sumJoint = 0d; 
		for (int y = 0; y < DIM; y++) {
			for (int x = 0; x < DIM; x++) {
				float cx = (CENTERX-x)/WEIGHTX;
				float cy = (CENTERY-y)/WEIGHTY;
				
//				System.out.printf("x,y,cx,cy: %1.3f, %1.3f, %1.3f, %1.3f\n",(float)x,(float)y,cx,cy);
				
				float val = (float) gen.joinProbability(cx, cy);
				sumJoint+=val;
				float inv = 1 - val;
				Float[] col = new Float[]{
					(Float)(ONE[0] * val + ZERO[0] * inv),
					(Float)(ONE[1] * val + ZERO[1] * inv),
					(Float)(ONE[2] * val + ZERO[2] * inv)
				};
				
				img.setPixel(x, y, col);
				double rng = 0.05;
				if(val > rng-0.003 && val < rng + 0.003){
					contours.setPixel(x, y, RGBColour.WHITE);
				}
			}
		}
		DisplayUtilities.display(img);
		DisplayUtilities.display(contours);
	}

	private double expectedMean() {
		return this.mean0;
	}

	private double expectedVar() {
		return this.gamma.getAlpha()*this.gamma.getBeta();
	}
}
