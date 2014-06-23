package org.openimaj.image.calibration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.matrix.NotConvergedException;

import org.openimaj.image.DisplayUtilities;
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

		final CameraCalibrationZhang calib = new CameraCalibrationZhang(pointMatches);
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
