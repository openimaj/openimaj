package org.openimaj.demos;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.openimaj.feature.FloatFV;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.dense.gradient.dsift.FloatDSIFTKeypoint;
import org.openimaj.image.feature.local.aggregate.FisherVector;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis;
import org.openimaj.math.matrix.algorithm.pca.ThinSvdPrincipalComponentAnalysis;
import org.openimaj.math.statistics.distribution.DiagonalMultivariateGaussian;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;
import org.openimaj.util.array.ArrayUtils;

import Jama.Matrix;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLSingle;
import com.jmatio.types.MLStructure;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class FVFWCheckPCAGMM {
	
	private static final String GMM_MATLAB_FILE = "/Users/ss/Experiments/FVFW/data/gmm_512.mat";
	private static final String PCA_MATLAB_FILE = "/Users/ss/Experiments/FVFW/data/PCA_64.mat";
	private static final String[] FACE_DSIFTS = new String[]{
		"/Users/ss/Experiments/FVFW/data/Aaron_Eckhart_0001-pdfsift.bin"
	};
	

	public static void main(String[] args) throws IOException {
		MixtureOfGaussians mog = loadMoG();
		PrincipalComponentAnalysis pca = loadPCA();
		FisherVector<float[]> fisher = new FisherVector<float[]>(mog,true,true);
		for (String faceFile : FACE_DSIFTS) {
			MemoryLocalFeatureList<FloatDSIFTKeypoint> loadDSIFT = MemoryLocalFeatureList.read(new File(faceFile), FloatDSIFTKeypoint.class);
			projectPCA(loadDSIFT,pca);
			
			FloatFV fvec = fisher .aggregate(loadDSIFT);
			System.out.println(String.format("%s: %s",faceFile,fvec));
			System.out.println("Writing...");
			
			File out = new File(faceFile + ".fisher.mat");
			MLArray data = toMLArray(fvec);
			new MatFileWriter(out, Arrays.asList(data));
		}
	}

	private static MLArray toMLArray(FloatFV fvec) {
		MLDouble data = new MLDouble("fisherface", new int[]{fvec.values.length,1});
		for (int i = 0; i < fvec.values.length; i++) {
			data.set((double) fvec.values[i], i, 0);
		}
		return data;
	}

	private static void projectPCA(
			MemoryLocalFeatureList<FloatDSIFTKeypoint> loadDSIFT,
			PrincipalComponentAnalysis pca) {
		for (FloatDSIFTKeypoint kp : loadDSIFT) {
			kp.descriptor = ArrayUtils.convertToFloat(pca.project(ArrayUtils.convertToDouble(kp.descriptor)));
			int nf = kp.descriptor.length;
			kp.descriptor = Arrays.copyOf(kp.descriptor, nf+2);
			kp.descriptor[nf] = (kp.x / 125f) - 0.5f;
			kp.descriptor[nf+1] = (kp.y / 160f) - 0.5f;
		}
		loadDSIFT.resetVecLength();
	}

	static class LoadedPCA extends ThinSvdPrincipalComponentAnalysis{

		public LoadedPCA(Matrix basis, double[] mean) {
			super(basis.getRowDimension());
			this.basis = basis;
			this.mean = mean;
		}
		
		
	}

	private static PrincipalComponentAnalysis loadPCA() throws IOException {
		File f = new File(PCA_MATLAB_FILE);
		MatFileReader reader = new MatFileReader(f);
		MLSingle mean = (MLSingle) reader.getContent().get("mu");
		MLSingle eigvec = (MLSingle) reader.getContent().get("proj");
		Matrix basis = new Matrix(eigvec.getM(), eigvec.getN());
		double[] meand = new double[eigvec.getN()];
		for (int j = 0; j < eigvec.getN(); j++) {
//			meand[i] = mean.get(i,0); ignore the means
			meand[j] = 0;
			for (int i = 0; i < eigvec.getM(); i++) {
				basis.set(i, j, eigvec.get(i,j));
			}
		}
		PrincipalComponentAnalysis ret = new LoadedPCA(basis.transpose() ,meand);
		return ret ;
	}



	private static MixtureOfGaussians loadMoG() throws IOException {
		File f = new File(GMM_MATLAB_FILE);
		MatFileReader reader = new MatFileReader(f);
		MLStructure codebook = (MLStructure) reader.getContent().get("codebook");
		
		MLSingle mean = (MLSingle) codebook.getField("mean");
		MLSingle variance = (MLSingle) codebook.getField("variance");
		MLSingle coef = (MLSingle) codebook.getField("coef");
		
		int n_gaussians = mean.getN();
		int n_dims = mean.getM();
		
		MultivariateGaussian[] ret = new MultivariateGaussian[n_gaussians];
		double[] weights = new double[n_gaussians];
		for (int i = 0; i < n_gaussians; i++) {
			weights[i] = coef.get(i, 0);
			DiagonalMultivariateGaussian d = new DiagonalMultivariateGaussian(n_dims);
			for (int j = 0; j < n_dims; j++) {
				d.mean.set(0, j, mean.get(j, i));
				d.variance[j] = variance.get(j, i);
			}
			ret[i] = d;
		}
		
		return new MixtureOfGaussians(ret, weights);
	}

}
