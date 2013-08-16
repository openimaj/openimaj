package org.openimaj.ml.clustering.kdtree;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.la4j.decomposition.LUDecompositor;
import org.la4j.factory.CRSFactory;
import org.la4j.linear.LinearSystem;
import org.la4j.linear.SquareRootSolver;
import org.la4j.matrix.sparse.CRSMatrix;
import org.la4j.vector.dense.BasicVector;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.io.FileUtils;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.kdtree.ClusterTestDataLoader.TestStats;
import org.openimaj.ml.clustering.spectral.GraphLaplacian;

import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.Vector.Entry;
import ch.akuhn.matrix.eigenvalues.FewEigenvalues;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class FixEigenVector {
	private static final int EIGHEIGHT = 100;
	private static final int EIGWIDTH = 600;
	private double[][] testData;
	private int[][] testClusters;
	private TestStats testStats;
	/**
	 * @throws IOException
	 */
	@Before
	public void loadTest() throws IOException{
		String[] data = FileUtils.readlines(new File("/Users/ss/Development/java/openimaj/trunk/machine-learning/clustering/src/test/resources/org/openimaj/ml/clustering/dbscan/dbscandata"));
		ClusterTestDataLoader loader = new ClusterTestDataLoader();
		this.testStats = loader.readTestStats(data);
		this.testData = loader.readTestData(data);
		this.testClusters = loader.readTestClusters(data);
	}
	
	public static void main(String[] args) throws IOException {
		FixEigenVector tc = new FixEigenVector();
		tc.loadTest();
		tc.doTest();
	}

	/**
	 * @throws IOException 
	 *
	 */
	@Test
	public void doTest() throws IOException{
		
		GraphLaplacian gl = new GraphLaplacian.Normalised();
		SparseMatrix W = normalisedSimilarity();
		SparseMatrix L = gl.laplacian(W);
		writeSparse(L, "L");
		writeSparse(W, "W");
		FewEigenvalues fev = FewEigenvalues.of(L);
		
		MatFileReader reader = new MatFileReader(new File("/Users/ss/Experiments/python/test.mat"));
		Matrix b = MatlibMatrixUtils.fromMatlab(reader.getMLArray("b"));
		Matrix x = MatlibMatrixUtils.fromMatlab(reader.getMLArray("x"));
		
		System.out.println(b);
		System.out.println(x);
		CRSMatrix Lla4j = la4jMat(L);
		org.la4j.vector.Vector bla4j = la4jMat(b.column(0));
		org.la4j.vector.Vector sol = new SquareRootSolver().solve(new LinearSystem(Lla4j, bla4j), new CRSFactory() );
		org.la4j.matrix.Matrix[] lu = new LUDecompositor().decompose(Lla4j, new CRSFactory());
		
		
//		no.uib.cipr.matrix.Matrix Lmtj = MatlibMatrixUtils.toMTJ(L);
//		no.uib.cipr.matrix.Matrix bmtj = MatlibMatrixUtils.toMTJ(b);
//		no.uib.cipr.matrix.Matrix xmtj = new no.uib.cipr.matrix.DenseMatrix(bmtj.numRows(), bmtj.numColumns());
//		xmtj = Lmtj.solve(bmtj, xmtj); 
//		Jama.Matrix inv = PseudoInverse.pseudoInverse(MatlibMatrixUtils.toJama(L));
		
		
	}

	private org.la4j.vector.Vector la4jMat(Vector column) {
		double[] arr = new double[column.size()];
		column.storeOn(arr, 0);
		org.la4j.vector.Vector vec = new BasicVector(arr);
		
		return vec;
	}

	private CRSMatrix la4jMat(Matrix L) {
		CRSMatrix mat = new CRSMatrix(L.rowCount(), L.columnCount());
		int r = 0;
		for (Vector row : L.rows()) {
			for (Entry ent : row.entries()) {
				mat.set(r, ent.index, ent.value);
			}
			r++;
		}
		return mat;
	}
	


	private void writeSparse(final SparseMatrix laplacian, String name) {
		MLDouble arr = new MLDouble(name, laplacian.asDenseDoubleDouble());
		Collection<MLArray> a =new ArrayList<MLArray>();
		a.add(arr);
		try {
			MatFileWriter writ = new MatFileWriter(new File("/Users/ss/Experiments/python/" + name + ".mat"), a );
		} catch (IOException e) {
			
		}
	}


	
	private SparseMatrix normalisedSimilarity() {
		return normalisedSimilarity(this.testStats.eps);
	}
	private SparseMatrix normalisedSimilarity(double eps) {
		final SparseMatrix mat = new SparseMatrix(testData.length,testData.length);
		final DoubleFVComparison dist = DoubleFVComparison.EUCLIDEAN;
		double maxD = 0;
		for (int i = 0; i < testData.length; i++) {
			for (int j = i; j < testData.length; j++) {
				double d = dist.compare(testData[i], testData[j]);
				if(d>eps) d = Double.NaN;
				else{
					maxD = Math.max(d, maxD);
				}
				mat.put(i, j, d);
				mat.put(j, i, d);
			}
		}
		SparseMatrix mat_norm = new SparseMatrix(testData.length,testData.length);
		for (int i = 0; i < testData.length; i++) {
			for (int j = i+1; j < testData.length; j++) {
				double d = mat.get(i, j);
				if(Double.isNaN(d)){
					continue;
				}
				else{
					d/=maxD;
				}
				mat_norm.put(i, j, 1-d);
				mat_norm.put(j, i, 1-d);
			}
		}
		return mat_norm;
	}

	private void confirmClusters(IndexClusters res) {
		for (int i = 0; i < this.testClusters.length; i++) {
			assertTrue(toSet(this.testClusters[i]).equals(toSet(res.clusters()[i])));
		}
	}
	
	private Set<Integer> toSet(int[] is) {
		Set<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < is.length; i++) {
			set.add(is[i]);
		}
		return set;
	}
}
