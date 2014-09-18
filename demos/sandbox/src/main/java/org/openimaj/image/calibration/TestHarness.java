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
package org.openimaj.image.calibration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.matrix.NotConvergedException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.camera.calibration.CameraCalibrationZhang;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.util.iterator.TextLineIterable;
import org.openimaj.util.pair.IndependentPair;

public class TestHarness {
	public static void main(String[] args) throws IOException, NotConvergedException {
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
		// final CameraCalibrationZhang calib = new
		// CameraCalibration(pointMatches);

		System.out.println(calib.getIntrisics());

		System.out.println(calib.calculateError());

		final MBFImage img = new MBFImage(640, 480);
		for (int i = 0; i < pointMatches.get(0).size(); i++) {
			final Point2d model = pointMatches.get(0).get(i).firstObject();
			final Point2d observed = pointMatches.get(0).get(i).secondObject();

			final Point2d proj = calib.getCameras().get(0).project(model);
			img.drawPoint(proj, RGBColour.RED, 1);
			img.drawPoint(observed, RGBColour.GREEN, 1);
		}
		DisplayUtilities.display(img);

		final FImage img1 = ImageUtilities.readF(new URL(
				"http://research.microsoft.com/en-us/um/people/zhang/Calib/Calibration/CalibIm1.gif"));
		DisplayUtilities.display(img1);
		final FImage img2 = calib.getIntrisics().undistort(img1);
		DisplayUtilities.display(img2);
	}

	private static List<Point2dImpl> loadModelPoints() throws MalformedURLException {
		return loadData(new URL(
				"http://research.microsoft.com/en-us/um/people/zhang/Calib/Calibration/Model.txt"));
	}

	private static List<List<Point2dImpl>> loadImagePoints() throws MalformedURLException {
		final List<List<Point2dImpl>> data = new ArrayList<List<Point2dImpl>>();

		data.add(loadData(new URL("http://research.microsoft.com/en-us/um/people/zhang/Calib/Calibration/data1.txt")));
		data.add(loadData(new URL("http://research.microsoft.com/en-us/um/people/zhang/Calib/Calibration/data2.txt")));
		data.add(loadData(new URL("http://research.microsoft.com/en-us/um/people/zhang/Calib/Calibration/data3.txt")));
		data.add(loadData(new URL("http://research.microsoft.com/en-us/um/people/zhang/Calib/Calibration/data4.txt")));
		data.add(loadData(new URL("http://research.microsoft.com/en-us/um/people/zhang/Calib/Calibration/data5.txt")));

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
