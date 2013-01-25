package org.openimaj.demos.sandbox.gmm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.data.RandomData;
import org.openimaj.demos.sandbox.gmm.GaussianMixtureModelGenerator.Generated;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;

import Jama.Matrix;

/**
 * Starting with the log liklihood function:
 * ln P(X | pi, mu, sigma) = Sum[N:n=1]{ln Sum[K:k=1]{ pi_k Norm(x_n | mu_k, sigma_k)}}
 *
 * We differentiate for the values pi, mu and sigma to get the maximum (i.e. when dif == 0)
 * for mu, sigma and pi.
 *
 * This works out to:
 *
 * mu_k = Sum[N:n=1] { g(z_nk) * x_n } / N_k
 * sigma_k = Sum[N:=1] { g(z_nk) * (x_n - mu_k) * (x_n - mu_k)^T} / N_k
 * pi_k = N_k/N
 *
 * where g(z_nk) is the posterior of the kth class given the nth data, i.e.
 *
 * g(z_nk) = P(z_k = 1 | x_n ) = P(x_n | z_k = 1) * P(z_k = 1) / P(x_n)
 * 					 		   = Norm(x_n | mu_k, sigma_k) * pi_k / Sum(K:j=1) {pi_j * Norm(x_n | mu_j, sigma_j)
 *
 * and N_k is basically N * pi_k and also Sum(N:n=1){g(z_nk)}, i.e. the "number" of points assigned to the Nth gaussian
 *
 * Note that g(z_nk) relies on mu, pi and sigma while mu and sigma rely on g(z_nk) and so does pi.
 *
 * We allow for this by randomly initialising mu, pi and sigma and finding the probability of each class given each data item
 * We then find a new value of mu, pi and sigma given the probability of each class and each data item!
 *
 * The first step calculates the expectation
 * The second step maximises the parameters
 *
 * Expectation, Maximisation
 * EM.
 *
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GaussianMixtureModelEM {
	private static final double SMALL = 0.00000001;
	private int nGaus;
	private double[][] data;
	private Matrix gausPosterior;
	private double[] gausPrior;
	private List<MultivariateGaussian> gaussians;
	private double[] sumPosterior;

	public GaussianMixtureModelEM(double[][] data,int nGaus){
		this.data = data;
		this.nGaus = nGaus;
		init();
	}

	public GaussianMixtureModelEM(GaussianMixtureModelGenerator generator, int samples, int nGaus){
		this.data = new double[samples][generator.dimentions()];
		for (int i = 0; i < data.length; i++) {
			System.arraycopy(generator.generate().point, 0, data[i], 0, data[i].length);
		}
		this.nGaus = nGaus;

		init();
	}

	private void init() {
		this.gausPosterior = new Matrix(this.data.length,this.nGaus);
		this.gausPrior = new double[this.nGaus];
		this.gaussians = new ArrayList<MultivariateGaussian>();
		this.sumPosterior = new double[this.nGaus];
		int[] randomData = RandomData.getRandomIntArray(this.nGaus, 0, this.data.length);
		Matrix covar = Matrix.identity(data[0].length, data[0].length).times(800f);
		for (int i = 0; i < this.nGaus; i++) {
			this.gausPrior[i] = 1f/this.nGaus;
			double[] dataItem = data[randomData[i]];
			Matrix mean = new Matrix(new double[][]{Arrays.copyOf(dataItem,dataItem.length)});
			this.gaussians.add(new MultivariateGaussian(mean, covar.copy()));
		}
	}

	private void e_step(){
		float[] holder = new float[this.data[0].length];
		double[] liklihoods = new double[this.nGaus];
		// Reset the sum posterior
		this.sumPosterior = new double[this.nGaus];
		for (int dataIndex = 0; dataIndex < this.data.length; dataIndex++) {
			double[] dataItem = data[dataIndex];
			double norm = 0;
			for (int gausIndex = 0; gausIndex < this.nGaus; gausIndex++) {
				liklihoods[gausIndex] = this.gaussians.get(gausIndex).estimateProbability(dataAsFloat(dataItem,holder));
				norm += (this.gausPrior[gausIndex] * liklihoods[gausIndex]);
			}
			if(norm < SMALL){
				norm = 1;
			}
			for (int gausIndex = 0; gausIndex < this.nGaus; gausIndex++) {
				double liklihood = liklihoods[gausIndex];

				double posterior = this.gausPrior[gausIndex] * liklihood / norm;
				if(Double.isNaN(posterior)){
					System.err.println("NaN!");
				}
				this.gausPosterior.set(dataIndex, gausIndex, posterior);
				this.sumPosterior[gausIndex] += posterior ;
			}
		}
	}

	private void m_step(){
		for (int gausIndex = 0; gausIndex < this.nGaus; gausIndex++) {
			Matrix newMean = new Matrix(1,this.data[0].length);
			for (int dataIndex = 0; dataIndex < this.data.length; dataIndex++) {
				double[] dataItem = this.data[dataIndex];
				Matrix dataItemMat = new Matrix(new double[][]{dataItem});
				double posterior = this.gausPosterior.get(dataIndex, gausIndex);
				newMean.plusEquals(dataItemMat.times(posterior));
			}
			double sumPosteriorGausIndex = this.sumPosterior[gausIndex];
			newMean.timesEquals(1d/sumPosteriorGausIndex);
			Matrix newCovar = this.gaussians.get(gausIndex).getCovar().times(0);
			for (int dataIndex = 0; dataIndex < this.data.length; dataIndex++) {
				double[] dataItem = this.data[dataIndex];
				Matrix dataItemMat = new Matrix(new double[][]{dataItem});
				Matrix centered = dataItemMat.minus(newMean).transpose();
				double posterior = this.gausPosterior.get(dataIndex, gausIndex);
				newCovar.plusEquals(centered.times(centered.transpose()).times(posterior));
			}
			newCovar.timesEquals(1d/sumPosteriorGausIndex);
			this.gaussians.set(gausIndex, new MultivariateGaussian(newMean, newCovar));
			this.gausPrior[gausIndex] = sumPosteriorGausIndex / this.data.length;
		}
	}

	public void step(){
		this.e_step();
		this.m_step();
	}

	private float[] dataAsFloat(double[] dataItem, float[] holder) {
		for (int i = 0; i < holder.length; i++) {
			holder[i] = (float) dataItem[i];
		}
		return holder;
	}

	public static void main(String[] args) throws InterruptedException {
		Ellipse e1 = new Ellipse(200, 200, 40, 20, Math.PI/3);
		Ellipse e2 = new Ellipse(220, 150, 60, 20, -Math.PI/3);
		Ellipse e3 = new Ellipse(180, 200, 80, 20, -Math.PI/3);
		Float[][] colours = new Float[][]{RGBColour.RED,RGBColour.GREEN,RGBColour.BLUE};
		MBFImage image = new MBFImage(400,400,3);
		image.drawShape(e1, RGBColour.RED);
		image.drawShape(e2, RGBColour.GREEN);
		image.drawShape(e3, RGBColour.BLUE);

		GaussianMixtureModelGenerator2D gmm = new GaussianMixtureModelGenerator2D(e1,e2,e3);
		MBFImage imageUnblended = image.clone();
		MBFImage imageBlended = image.clone();
		MBFImage imageEM = image.clone();
		int N_POINTS = 1000;
		double[][] data = new double[N_POINTS][2];
		for (int i = 0; i < N_POINTS ; i++) {
			Generated gen = gmm.generate();
			data[i] = gen.point;
			Point2d p = new Point2dImpl((float)gen.point[0],(float) gen.point[1]);
			imageUnblended.drawPoint(p, colours[gen.distribution], 3);
			Float[] weightedColour = new Float[3];
			for (int j = 0; j < weightedColour.length; j++) {
				weightedColour[j] = 0f;
			}
			for (int colour = 0; colour < colours.length; colour++) {
				for (int channel = 0; channel < colours[colour].length; channel++) {
					weightedColour[channel] = (float) (weightedColour[channel] + colours[colour][channel] * gen.responsibilities[colour]);
				}
			}
			double sumWeight = 0;
			for (int j = 0; j < weightedColour.length; j++) {
				sumWeight += weightedColour[j];
			}
			for (int j = 0; j < weightedColour.length; j++) {
				weightedColour[j] = (float) (weightedColour[j] / sumWeight);
			}
			imageBlended.drawPoint(p, weightedColour, 3);
		}

		DisplayUtilities.display(imageUnblended);
		DisplayUtilities.display(imageBlended);
		GaussianMixtureModelEM em = new GaussianMixtureModelEM(data, 3);


		for (int i = 0; i < 100; i++) {
			imageEM.fill(RGBColour.BLACK);
			for (int j = 0; j < 3; j++) {
				MultivariateGaussian gaussian = em.gaussians.get(j);
				Ellipse e = EllipseUtilities.ellipseFromGaussian(gaussian,2.f);
				imageEM.drawShape(e, colours[j]);
			}
			DisplayUtilities.displayName(imageEM,"EM Progress");
			Thread.sleep(500);
			em.step();
		}
	}
}
