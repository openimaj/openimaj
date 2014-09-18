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
package org.openimaj.image.camera.calibration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openimaj.image.camera.Camera;
import org.openimaj.image.camera.CameraIntrinsics;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.util.iterator.TextLineIterable;
import org.openimaj.util.pair.IndependentPair;

/**
 * Test {@link CameraCalibrationZhang}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class CameraCalibrationZhangTest {
	private static final double EPS_PIX = 0.1;
	private static final double EPS = 0.01;

	/**
	 * Test against Zhang's published data
	 * (http://research.microsoft.com/en-us/um/people/zhang/Calib/)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testZhang() throws Exception {
		final List<Point2dImpl> modelPoints = loadModelPoints();
		final List<List<Point2dImpl>> points = loadImagePoints();

		final List<List<? extends IndependentPair<? extends Point2d, ? extends Point2d>>> pointMatches =
				new ArrayList<List<? extends IndependentPair<? extends Point2d, ? extends Point2d>>>();

		for (int i = 0; i < points.size(); i++) {
			final List<IndependentPair<Point2dImpl, Point2dImpl>> data =
					IndependentPair.pairList(modelPoints, points.get(i));
			pointMatches.add(data);
		}

		final CameraCalibrationZhang calib = new CameraCalibrationZhang(pointMatches, 640, 480);
		final CameraIntrinsics intrisics = calib.getIntrisics();

		assertEquals(832.5, intrisics.getFocalLengthX(), EPS_PIX);
		assertEquals(0.204494, intrisics.getSkewFactor(), EPS);
		assertEquals(832.53, intrisics.getFocalLengthY(), EPS_PIX);
		assertEquals(303.959, intrisics.getPrincipalPointX(), EPS_PIX);
		assertEquals(206.585, intrisics.getPrincipalPointY(), EPS_PIX);

		assertEquals(-0.228601, intrisics.k1, EPS);
		assertEquals(0.190353, intrisics.k2, EPS);

		final double[][] e1 = {
				{ 0.992759, -0.026319, 0.117201 },
				{ 0.0139247, 0.994339, 0.105341 },
				{ -0.11931, -0.102947, 0.987505 } };
		final double[] t1 = { -3.84019, 3.65164, 12.791 };
		checkValues(e1, t1, calib.getCameras().get(0));

		final double[][] e2 = {
				{ 0.997397, -0.00482564, 0.0719419 },
				{ 0.0175608, 0.983971, -0.17746 },
				{ -0.0699324, 0.178262, 0.981495 } };
		final double[] t2 = { -3.71693, 3.76928, 13.1974 };
		checkValues(e2, t2, calib.getCameras().get(1));

		final double[][] e3 = {
				{ 0.915213, -0.0356648, 0.401389 },
				{ -0.00807547, 0.994252, 0.106756 },
				{ -0.402889, -0.100946, 0.909665 } };
		final double[] t3 = { -2.94409, 3.77653, 14.2456 };
		checkValues(e3, t3, calib.getCameras().get(2));

		final double[][] e4 = {
				{ 0.986617, -0.0175461, -0.16211 },
				{ 0.0337573, 0.994634, 0.0977953 },
				{ 0.159524, -0.101959, 0.981915 } };
		final double[] t4 = { -3.40697, 3.6362, 12.4551 };
		checkValues(e4, t4, calib.getCameras().get(3));

		final double[][] e5 = {
				{ 0.967585, -0.196899, -0.158144 },
				{ 0.191542, 0.980281, -0.0485827 },
				{ 0.164592, 0.0167167, 0.98622 } };
		final double[] t5 = { -4.07238, 3.21033, 14.3441 };
		checkValues(e5, t5, calib.getCameras().get(4));
	}

	private void checkValues(double[][] r, double[] t, Camera camera) {
		assertArrayEquals(r[0], camera.rotation.getArray()[0], EPS);
		assertArrayEquals(r[1], camera.rotation.getArray()[1], EPS);
		assertArrayEquals(r[2], camera.rotation.getArray()[2], EPS);

		assertEquals(t[0], camera.translation.getX(), EPS);
		assertEquals(t[1], camera.translation.getY(), EPS);
		assertEquals(t[2], camera.translation.getZ(), EPS);
	}

	private static List<Point2dImpl> loadModelPoints() throws MalformedURLException {
		return loadData(CameraCalibrationZhang.class.getResource("Model.txt"));
	}

	private static List<List<Point2dImpl>> loadImagePoints() throws MalformedURLException {
		final List<List<Point2dImpl>> data = new ArrayList<List<Point2dImpl>>();

		data.add(loadData(CameraCalibrationZhang.class.getResource("data1.txt")));
		data.add(loadData(CameraCalibrationZhang.class.getResource("data2.txt")));
		data.add(loadData(CameraCalibrationZhang.class.getResource("data3.txt")));
		data.add(loadData(CameraCalibrationZhang.class.getResource("data4.txt")));
		data.add(loadData(CameraCalibrationZhang.class.getResource("data5.txt")));

		return data;
	}

	private static List<Point2dImpl> loadData(URL url) {
		final List<Point2dImpl> pts = new ArrayList<Point2dImpl>();

		for (final String line : new TextLineIterable(url))
		{
			final String[] p = line.split("[ ]+");
			pts.add(new Point2dImpl(Float.parseFloat(p[0]), Float.parseFloat(p[1])));
			pts.add(new Point2dImpl(Float.parseFloat(p[2]), Float.parseFloat(p[3])));
			pts.add(new Point2dImpl(Float.parseFloat(p[4]), Float.parseFloat(p[5])));
			pts.add(new Point2dImpl(Float.parseFloat(p[6]), Float.parseFloat(p[7])));
		}
		return pts;
	}
}
