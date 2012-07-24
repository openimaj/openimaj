package org.openimaj.image.processing.face.detection;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.openimaj.image.FImage;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

import com.jsaragih.CLM;
import com.jsaragih.FDet;
import com.jsaragih.IO;
import com.jsaragih.MFCheck;
import com.jsaragih.Tracker;

/**
 * Face detector based on a constrained local model. Fits
 * a 3D face model to each detection.
 * 
 * @see CLM
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class CLMFaceDetector implements FaceDetector<CLMDetectedFace, FImage> {
	/**
	 * Configuration for the face detector
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class Configuration {
		/** The constrained local model */
		public CLM clm;

		/** The reference shape */
		public Matrix referenceShape;

		/** The current shape */
		public Matrix shape;

		/** The Face detector */
		public FDet faceDetector;

		/** The failure checker */
		public MFCheck failureCheck;

		/** Initialisation similarity */
		double[] similarity;

		/** The face mesh */
		public int[][] triangles = null;

		/** The face connections */
		public int[][] connections = null;

		/** Whether to use the face check (using pixels as a face classifier) */
		public boolean fcheck = false;

		/** Search window sizes */
		public int[] windowSize = { 11, 9, 7 };

		/** Number of iterations to use for model fitting */
		public int nIter = 5;

		/** Number of standard deviations from the mean face to allow in the model */
		public double clamp = 3;

		/** Model fitting optimisation tolerance */
		public double fTol = 0.01;
		
		private void read(final InputStream in) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(in));
				Scanner sc = new Scanner(br);
				read(sc, true);
			} finally {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
		
		private void read(Scanner s, boolean readType) {
			if (readType) {
				int type = s.nextInt();
				assert (type == IO.Types.TRACKER.ordinal());
			}
			
			clm = CLM.read(s, true);
			faceDetector = FDet.read(s, true);
			failureCheck = MFCheck.read(s, true);
			referenceShape = IO.readMat(s);
			similarity = new double[] { s.nextDouble(), s.nextDouble(), s.nextDouble(), s.nextDouble() };
			shape = new Matrix(2 * clm._pdm.nPoints(), 1);
			clm._pdm.identity(clm._plocal, clm._pglobl);
		}
		
		/**
		 * Construct with the default model parameters
		 */
		public Configuration() {
			read(Tracker.class.getResourceAsStream("face2.tracker"));
			triangles = IO.loadTri(Tracker.class.getResourceAsStream("face.tri"));
			connections = IO.loadCon(Tracker.class.getResourceAsStream("face.con"));
		}
	}
	
	private Configuration config;
	
	/**
	 * Default constructor
	 */
	public CLMFaceDetector() {
		config = new Configuration();
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		config = IOUtils.read(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		IOUtils.write(config, out);
	}

	@Override
	public List<CLMDetectedFace> detectFaces(FImage image) {
		List<Rectangle> detRects = config.faceDetector.detect(image);
		List<CLMDetectedFace> faces = new ArrayList<CLMDetectedFace>();
		
		for (Rectangle f : detRects) {
			if ((f.width == 0) || (f.height == 0)) {
				continue;
			}

			initShape(f, config.shape, config.referenceShape);
			config.clm._pdm.calcParams(config.shape, config.clm._plocal, config.clm._pglobl);
			

			config.clm.fit(image, config.windowSize, config.nIter, config.clamp, config.fTol);
			config.clm._pdm.calcShape2D(config.shape, config.clm._plocal, config.clm._pglobl);

			if (config.fcheck) {
				if (!config.failureCheck.check(config.clm.getViewIdx(), image, config.shape)) {
					continue;
				}
			}

			faces.add(new CLMDetectedFace(f, config.shape.copy(), config.clm._pglobl.copy(), config.clm._plocal.copy(), image));
		}
		
		return faces;
	}
	
	/**
	 * Initialise the shape within the given rectangle based on the given
	 * reference shape.
	 * 
	 * @param r
	 *            The rectangle
	 * @param shape
	 *            The shape to initialise
	 * @param _rshape
	 *            The reference shape
	 */
	private void initShape(final Rectangle r, final Matrix shape, final Matrix _rshape) {
		assert ((shape.getRowDimension() == _rshape.getRowDimension()) && (shape
				.getColumnDimension() == _rshape.getColumnDimension()));

		int n = _rshape.getRowDimension() / 2;

		double a = r.width * Math.cos(config.similarity[1]) * config.similarity[0] + 1;
		double b = r.width * Math.sin(config.similarity[1]) * config.similarity[0];

		double tx = r.x + (int) (r.width / 2) + r.width * config.similarity[2];
		double ty = r.y + (int) (r.height / 2) + r.height * config.similarity[3];

		double[][] s = _rshape.getArray();
		double[][] d = shape.getArray();

		for (int i = 0; i < n; i++) {
			d[i][0] = a * s[i][0] - b * s[i + n][0] + tx;
			d[i + n][0] = b * s[i][0] + a * s[i + n][0] + ty;
		}
	}
}
