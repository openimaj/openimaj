/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.demos.sandbox;

import gnu.trove.list.array.TIntArrayList;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.geometry.Vector3D;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.math.geometry.triangulation.DelaunayTriangulator;
import org.openimaj.math.matrix.PseudoInverse;
import org.openimaj.math.matrix.ThinSingularValueDecomposition;
import org.openimaj.video.FImageFileBackedVideo;
import org.openimaj.video.Video;
import org.openimaj.video.tracking.klt.Feature;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.FeatureTable;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class SFMOrtho {
	public Matrix R;
	public Matrix S;
	public FImage texture;
	public int[][] triangleDefs;
	private List<Feature> pts;

	public SFMOrtho(Video<FImage> video, int nFeatures) {
		texture = video.getCurrentFrame().clone();

		FeatureTable features = trackFeatures(video, nFeatures, false);
		features = filterNonTracked(features);

		pts = features.features.get(0);
		final List<Triangle> tris = DelaunayTriangulator.triangulate(pts);
		triangleDefs = new int[tris.size()][3];

		for (int i = 0; i < tris.size(); i++) {
			final Triangle t = tris.get(i);

			for (int j = 0; j < 3; j++) {
				triangleDefs[i][j] = pts.indexOf(t.vertices[j]);
			}
		}

		final Matrix w = buildMeasurementMatrix(features);
		factorise(w);
		applyMetricConstraint();
		alignWithFrame(0);
	}

	public List<Vector3D> getVertices() {
		final List<Vector3D> vertices = new ArrayList<Vector3D>(S.getColumnDimension());

		for (int i = 0; i < S.getColumnDimension(); i++) {
			vertices.add(new Vector3D(S.get(0, i), S.get(1, i), S.get(2, i)));
		}

		return vertices;
	}

	public String toObjString() {
		final StringBuffer sb = new StringBuffer();

		final List<Vector3D> vertices = getVertices();
		for (final Vector3D v : vertices) {
			sb.append("v " + v.getX() + " " + v.getY() + " " + v.getZ() + "\n");
		}

		for (final int[] td : triangleDefs) {
			sb.append("f " + (td[0] + 1) + " " + (td[1] + 1) + " " + (td[2] + 1) + "\n");
		}

		return sb.toString();
	}

	private void applyMetricConstraint() {
		final Matrix Q = calculateOrthometricConstraint(R);

		R = R.times(Q);
		S = Q.inverse().times(S);
	}

	private void alignWithFrame(int frame) {
		Vector3D i1 = new Vector3D(R.get(frame, 0), R.get(frame, 1), R.get(frame, 2));
		i1 = i1.scalarMultiply(1 / i1.getNorm());

		final int f = R.getRowDimension() / 2;
		Vector3D j1 = new Vector3D(R.get(frame + f, 0), R.get(frame + f, 1), R.get(frame + f, 2));
		j1 = j1.scalarMultiply(1 / j1.getNorm());

		final Vector3D k1 = Vector3D.crossProduct(i1, j1);
		k1.scalarMultiply(1 / k1.getNorm());

		final Matrix R0 = new Matrix(new double[][] {
				{ i1.getX(), j1.getX(), k1.getX() },
				{ i1.getY(), j1.getY(), k1.getY() },
				{ i1.getZ(), j1.getZ(), k1.getZ() },
		});

		R = R.times(R0);
		S = R0.inverse().times(S);
	}

	private void factorise(Matrix w) {
		final ThinSingularValueDecomposition svd = new ThinSingularValueDecomposition(w, 3);

		final Matrix s_sqrt = svd.getSmatrixSqrt();
		this.R = svd.U.times(s_sqrt);
		this.S = s_sqrt.times(svd.Vt);
	}

	FeatureTable trackFeatures(Video<FImage> video, int nFeatures, boolean replace) {
		final TrackingContext tc = new TrackingContext();
		final FeatureList fl = new FeatureList(nFeatures);
		final FeatureTable ft = new FeatureTable(nFeatures);
		final KLTTracker tracker = new KLTTracker(tc, fl);

		tc.setSequentialMode(true);
		tc.setWriteInternalImages(false);
		tc.setAffineConsistencyCheck(-1);

		FImage prev = video.getCurrentFrame();
		tracker.selectGoodFeatures(prev);
		ft.storeFeatureList(fl, 0);

		while (video.hasNextFrame()) {
			final FImage next = video.getNextFrame();
			tracker.trackFeatures(prev, next);

			if (replace)
				tracker.replaceLostFeatures(next);

			prev = next;

			ft.storeFeatureList(fl, video.getCurrentFrameIndex());
		}

		return ft;
	}

	FeatureTable filterNonTracked(FeatureTable ft) {
		final int nFrames = ft.features.size();
		final TIntArrayList tracksToRemove = new TIntArrayList();

		for (int i = 0; i < ft.nFeatures; i++) {
			int sum = 0;

			for (int f = 1; f < nFrames; f++) {
				sum += ft.features.get(f).get(i).val;
			}

			if (sum != 0) {
				tracksToRemove.add(i);
			}
		}

		final FeatureTable filtered = new FeatureTable(ft.nFeatures - tracksToRemove.size());
		for (int f = 0; f < nFrames; f++) {
			final FeatureList fl = new FeatureList(filtered.nFeatures);

			for (int i = 0, j = 0; i < ft.nFeatures; i++) {
				if (!tracksToRemove.contains(i))
					fl.features[j++] = ft.features.get(f).get(i);
			}
			filtered.storeFeatureList(fl, f);
		}

		return filtered;
	}

	Matrix buildMeasurementMatrix(FeatureTable ft) {
		final int p = ft.nFeatures; // number of features tracked
		final int f = ft.features.size(); // number of frames

		final Matrix W = new Matrix(2 * f, p);
		final double[][] Wdata = W.getArray();

		for (int r = 0; r < f; r++) {
			for (int c = 0; c < p; c++) {
				Wdata[r][c] = ft.features.get(r).get(c).x;
				Wdata[r + f][c] = ft.features.get(r).get(c).y;
			}
		}

		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter("/Users/jsh2/Desktop/measurement.txt"));
			W.print(pw, 5, 5);
			pw.close();
		} catch (final IOException e) {
		}

		for (int r = 0; r < 2 * f; r++) {
			double mean = 0;

			for (int c = 0; c < p; c++) {
				mean += Wdata[r][c];
			}

			mean /= p;

			for (int c = 0; c < p; c++) {
				Wdata[r][c] -= mean;
			}
		}

		return W;
	}

	double[] gT(double[] a, double[] b) {
		return new double[] {
				a[0] * b[0],
				a[0] * b[1] + a[1] * b[0],
				a[0] * b[2] + a[2] * b[0],
				a[1] * b[1],
				a[1] * b[2] + a[2] * b[1],
				a[2] * b[2] };
	}

	Matrix calculateOrthometricConstraint(Matrix R) {
		final int f = R.getRowDimension() / 2;

		final double[][] ihT = R.getMatrix(0, f - 1, 0, 2).getArray();
		final double[][] jhT = R.getMatrix(f, (2 * f) - 1, 0, 2).getArray();

		final Matrix G = new Matrix(3 * f, 6);
		final Matrix c = new Matrix(3 * f, 1);
		for (int i = 0; i < f; i++) {
			G.getArray()[i] = gT(ihT[i], ihT[i]);
			G.getArray()[i + f] = gT(jhT[i], jhT[i]);
			G.getArray()[i + 2 * f] = gT(ihT[i], jhT[i]);

			c.set(i, 0, 1);
			c.set(i + f, 0, 1);
		}

		final Matrix I = PseudoInverse.pseudoInverse(G).times(c);

		final Matrix L = new Matrix(new double[][] {
				{ I.get(0, 0), I.get(1, 0), I.get(2, 0) },
				{ I.get(1, 0), I.get(3, 0), I.get(4, 0) },
				{ I.get(2, 0), I.get(4, 0), I.get(5, 0) }
		});

		// enforcing positive definiteness
		// see
		// http://www-cse.ucsd.edu/classes/sp04/cse252b/notes/lec16/lec16.pdf
		final Matrix Lsym = L.plus(L.transpose()).times(0.5);

		final EigenvalueDecomposition eigs = Lsym.eig();

		final Matrix Dsqrt = eigs.getD();
		final double[][] Darr = Dsqrt.getArray();
		for (int r = 0; r < Darr.length; r++) {
			if (Darr[r][r] < 0)
				Darr[r][r] = 0.0000001;

			Darr[r][r] = Math.sqrt(Darr[r][r]);
		}

		return eigs.getV().times(Dsqrt);
	}

	public static void main(String[] args) throws IOException {
		final FImageFileBackedVideo video = new FImageFileBackedVideo(
				"/Users/jsh2/Downloads/assignment4_part2_data/frame%08d.jpg", 1, 102);

		final SFMOrtho o = new SFMOrtho(video, 200);

		// double[][] data = o.S.getArray();
		// System.out.print("ListPointPlot3D[{");
		// for (int i=0; i<data[0].length; i++) {
		// System.out.print("{"+data[0][i]+","+data[1][i]+","+data[2][i]+"}");
		// if (i<data[0].length-1) System.out.print(",");
		// }
		// System.out.println("}, BoxRatios->{1,1,1}]");

		System.out.println(o.toObjString());
	}
}
