package org.openimaj.ml.clustering.spectral;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.MultiviewSimilarityClusterer;
import org.openimaj.util.pair.IndependentPair;

import ch.akuhn.matrix.DenseMatrix;
import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.SparseMatrix;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
@Reference(
		type = ReferenceType.Incollection,
		author = { "Abhishek Kumar", "Piyush Rai", "Hal Daume III" },
		title = "Co-regularized Multi-view Spectral Clustering",
		year = "2011",
		booktitle = "Advances in Neural Information Processing Systems 24",
		pages = { "1413", "", "1421" },
		editor = { "J. Shawe-Taylor", "R.S. Zemel", "P. Bartlett", "F.C.N. Pereira", "K.Q. Weinberger" })
public class DoubleMultiviewSpectralClustering implements MultiviewSimilarityClusterer<IndexClusters> {

	private MultiviewSpectralClusteringConf<double[]> conf;

	/**
	 * @param conf
	 *            cluster the eigen vectors
	 */
	public DoubleMultiviewSpectralClustering(MultiviewSpectralClusteringConf<double[]> conf) {
		this.conf = conf;
	}

	@Override
	public IndexClusters cluster(List<SparseMatrix> data) {
		final DoubleSpectralClustering dsp = new DoubleSpectralClustering(conf);

		if (data.size() == 1) {
			return dsp.cluster(data.get(0));
		}

		// Solve the spectral clustering for each view
		final ArrayList<IndependentPair<double[], double[][]>> answers = new ArrayList<IndependentPair<double[], double[][]>>(data.size());
		PreparedSpectralClustering prep = new PreparedSpectralClustering(conf);
		for (int i = 0; i < data.size(); i++) {
			answers.add(prep.bestCols(dsp.spectralCluster(data.get(i))));
		}
		while (!conf.stop.stop(answers)) {
			for (int i = 0; i < answers.size(); i++) {
				// L
				final SparseMatrix laplacian = dsp.laplacian(data.get(i));
				// lambda * (Sum_w!=v u_w . u_w^T)
				SparseMatrix ujujSum = null;
				for (int j = 0; j < answers.size(); j++) {
					if (i == j)
						continue;
					final Matrix uj = new DenseMatrix(answers.get(j).secondObject());
					final SparseMatrix ujuj = MatlibMatrixUtils.dotProductTranspose(uj, uj,
							new SparseMatrix(uj.rowCount(), uj.rowCount()));
					if (ujujSum == null) {
						ujujSum = ujuj;
					}
					else {
						MatlibMatrixUtils.plusInplace(ujujSum, ujuj);
					}
				}
				// L + lambda * (Sum_w!=v u_w . u_w^T)
				MatlibMatrixUtils.plusInplace(laplacian, MatlibMatrixUtils.scaleInplace(ujujSum, conf.lambda));
				// eig
				answers.add(i, prep.bestCols(dsp.laplacianEigenVectors(laplacian)));
			}
		}
		// Concatenate the eigen spaces and cluster using the conf clusterer
		// return
		// dsp.eigenspaceCluster(ArrayUtils.concatenate(answers.toArray(new
		// double[answers.size()][][])));
		return null;
	}

	@Override
	public int[][] performClustering(List<SparseMatrix> data) {
		return this.cluster(data).clusters();
	}

}
