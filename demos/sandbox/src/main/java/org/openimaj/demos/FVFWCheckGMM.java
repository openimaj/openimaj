package org.openimaj.demos;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.openimaj.feature.FloatFV;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.dense.gradient.dsift.FloatDSIFTKeypoint;
import org.openimaj.image.feature.local.aggregate.FisherVector;
import org.openimaj.math.statistics.distribution.DiagonalMultivariateGaussian;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;

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
public class FVFWCheckGMM {

	private static final String GMM_MATLAB_FILE = "/Users/ss/Experiments/FVFW/data/gmm_512.mat";
	private static final String[] FACE_DSIFTS_PCA = new String[] {
			"/Users/ss/Experiments/FVFW/data/aaron-pcadsiftaug.mat"
	};

	public static void main(String[] args) throws IOException {
		final MixtureOfGaussians mog = loadMoG();
		final FisherVector<float[]> fisher = new FisherVector<float[]>(mog, true, true);
		for (final String faceFile : FACE_DSIFTS_PCA) {
			final MemoryLocalFeatureList<FloatDSIFTKeypoint> loadDSIFTPCA = loadDSIFTPCA(faceFile);

			final FloatFV fvec = fisher.aggregate(loadDSIFTPCA);
			System.out.println(String.format("%s: %s", faceFile, fvec));
			System.out.println("Writing...");

			final File out = new File(faceFile + ".fisher.mat");
			final MLArray data = toMLArray(fvec);
			new MatFileWriter(out, Arrays.asList(data));
		}
	}

	private static MemoryLocalFeatureList<FloatDSIFTKeypoint> loadDSIFTPCA(String faceFile) throws IOException {
		final File f = new File(faceFile);
		final MatFileReader reader = new MatFileReader(f);
		final MLSingle feats = (MLSingle) reader.getContent().get("feats");
		final int nfeats = feats.getN();
		final MemoryLocalFeatureList<FloatDSIFTKeypoint> ret = new MemoryLocalFeatureList<FloatDSIFTKeypoint>();
		for (int i = 0; i < nfeats; i++) {
			final FloatDSIFTKeypoint feature = new FloatDSIFTKeypoint();
			feature.descriptor = new float[feats.getM()];
			for (int j = 0; j < feature.descriptor.length; j++) {
				feature.descriptor[j] = feats.get(j, i);
			}
			ret.add(feature);
		}

		return ret;
	}

	private static MLArray toMLArray(FloatFV fvec) {
		final MLDouble data = new MLDouble("fisherface", new int[] { fvec.values.length, 1 });
		for (int i = 0; i < fvec.values.length; i++) {
			data.set((double) fvec.values[i], i, 0);
		}
		return data;
	}

	private static MixtureOfGaussians loadMoG() throws IOException {
		final File f = new File(GMM_MATLAB_FILE);
		final MatFileReader reader = new MatFileReader(f);
		final MLStructure codebook = (MLStructure) reader.getContent().get("codebook");

		final MLSingle mean = (MLSingle) codebook.getField("mean");
		final MLSingle variance = (MLSingle) codebook.getField("variance");
		final MLSingle coef = (MLSingle) codebook.getField("coef");

		final int n_gaussians = mean.getN();
		final int n_dims = mean.getM();

		final MultivariateGaussian[] ret = new MultivariateGaussian[n_gaussians];
		final double[] weights = new double[n_gaussians];
		for (int i = 0; i < n_gaussians; i++) {
			weights[i] = coef.get(i, 0);
			final DiagonalMultivariateGaussian d = new DiagonalMultivariateGaussian(n_dims);
			for (int j = 0; j < n_dims; j++) {
				d.mean.set(0, j, mean.get(j, i));
				d.variance[j] = variance.get(j, i);
			}
			ret[i] = d;
		}

		return new MixtureOfGaussians(ret, weights);
	}

}
