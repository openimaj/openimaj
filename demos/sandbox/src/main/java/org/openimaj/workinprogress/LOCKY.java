package org.openimaj.workinprogress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.watershed.feature.MomentFeature;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.feature.local.interest.EllipticInterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.pixel.IntValuePixel;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.Rectangle;

public class LOCKY implements InterestPointDetector<EllipticInterestPointData> {
	private static float DEFAULT_THRESHOLD_PERCENTAGE = 0.24f;
	protected BrightnessClusteringTransform bct = new BrightnessClusteringTransform();
	protected ConnectedComponentLabeler ccl = new ConnectedComponentLabeler(ConnectMode.CONNECT_4);
	protected int shiftx = 0;
	protected int shifty = 0;
	private float threshold;
	private ArrayList<EllipticInterestPointData> regions;

	public LOCKY() {
		this(DEFAULT_THRESHOLD_PERCENTAGE);
	}

	public LOCKY(float threshold) {
		this.threshold = threshold;
	}

	@Override
	public void findInterestPoints(FImage image) {
		final FImage bctImage = image.process(bct);
		bctImage.normalise();
		final FImage thresholdImage = bctImage.clone().threshold(this.threshold);
		final List<ConnectedComponent> components = ccl.findComponents(thresholdImage);

		this.regions = new ArrayList<EllipticInterestPointData>(components.size());
		for (final ConnectedComponent cc : components) {
			final EllipticInterestPointData ipd = new EllipticInterestPointData();
			ipd.score = findMaxValue(cc, bctImage);

			// TODO: utility method to get best fit ellipse of concomp
			final MomentFeature mf = new MomentFeature();
			final IntValuePixel tmp = new IntValuePixel(0, 0);
			for (final Pixel p : cc) {
				tmp.x = p.x;
				tmp.y = p.y;
				mf.addSample(tmp);
			}
			final Ellipse e = mf.getEllipse(2);
			// FIXME: need to factor out the scale
			ipd.transform = e.transformMatrix().getMatrix(0, 1, 0, 1);
			ipd.scale = 10;

			final Pixel centroid = cc.calculateCentroidPixel();
			ipd.x = centroid.x;
			ipd.y = centroid.y;

			regions.add(ipd);
		}

		DisplayUtilities.display(bctImage);
		DisplayUtilities.display(thresholdImage);

		Collections.sort(regions, new Comparator<EllipticInterestPointData>() {
			@Override
			public int compare(EllipticInterestPointData o1, EllipticInterestPointData o2) {
				return -Float.compare(o1.score, o2.score);
			}
		});
	}

	private float findMaxValue(ConnectedComponent cc, FImage bctImage) {
		float max = -1;
		for (final Pixel p : cc) {
			max = Math.max(max, bctImage.pixels[p.y][p.x]);
		}
		return max;
	}

	@Override
	public void findInterestPoints(FImage image, Rectangle window) {
		findInterestPoints(image.extractROI(window));
		shiftx = (int) window.x;
		shifty = (int) window.y;
	}

	@Override
	public List<EllipticInterestPointData> getInterestPoints(int npoints) {
		return regions.subList(0, Math.min(npoints, regions.size()));
	}

	@Override
	public List<EllipticInterestPointData> getInterestPoints(float threshold) {
		for (int i = regions.size(); i > 0; i++) {
			if (regions.get(i - 1).score < threshold)
				return regions.subList(0, i);
		}

		return new ArrayList<EllipticInterestPointData>(0);
	}

	@Override
	public List<EllipticInterestPointData> getInterestPoints() {
		return regions;
	}

	public static void main(String[] args) {
		final MBFImage image = new MBFImage(400, 400, ColourSpace.RGB);
		final MBFImageRenderer renderer = image.createRenderer();

		image.fill(RGBColour.BLACK);
		final List<Ellipse> ellipses = new ArrayList<Ellipse>();
		ellipses.add(new Ellipse(200, 100, 10, 8, Math.PI / 4));
		ellipses.add(new Ellipse(200, 300, 5, 3, -Math.PI / 4));
		ellipses.add(new Ellipse(100, 300, 3, 5, -Math.PI / 3));

		for (final Ellipse ellipse : ellipses) {
			renderer.drawShapeFilled(ellipse, RGBColour.WHITE);
		}

		final LOCKY locky = new LOCKY();
		locky.findInterestPoints(image.flatten());
		final List<EllipticInterestPointData> pts = locky.getInterestPoints();
		for (final EllipticInterestPointData pt : pts) {
			image.drawShape(pt.getEllipse(), RGBColour.RED);
		}

		DisplayUtilities.display(image);
	}
}
