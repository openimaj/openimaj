package org.openimaj.demos.sandbox;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.content.animation.animator.DoubleArrayValueAnimator;
import org.openimaj.content.animation.animator.RandomLinearDoubleValueAnimator;
import org.openimaj.content.animation.animator.ValueAnimator;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.PointListConnections;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.AnimatedVideo;
import org.openimaj.video.VideoDisplay;

public class PDMDemo implements Slide {

	private VideoDisplay<FImage> display;
	ValueAnimator<double[]> a;
	volatile boolean animate = false;
	private double[] stdev;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		// the main panel
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		final PointListConnections connections = PDMTest.loadConnections();
		final List<PointList> pointData = PDMTest.loadData();

		final PointDistributionModel pdm = new PointDistributionModel(pointData);
		final int N = 5;
		pdm.setNumComponents(N);

		final JPanel sliderPanel = new JPanel();
		sliderPanel.setOpaque(false);
		sliderPanel.setLayout(new GridLayout(0, 1));
		final JSlider[] sliders = new JSlider[N];
		for (int i = 0; i < sliders.length; i++) {
			sliders[i] = new JSlider(-100, 100, 0);
			sliderPanel.add(sliders[i]);
		}

		stdev = pdm.getStandardDeviations(3);
		final double[] currentValue = new double[N];
		display = VideoDisplay.createVideoDisplay(new AnimatedVideo<FImage>(new FImage(600, 600)) {

			@Override
			protected void updateNextFrame(FImage frame) {
				frame.fill(0f);

				if (animate) {
					System.arraycopy(a.nextValue(), 0, currentValue, 0, currentValue.length);
				} else {
					for (int i = 0; i < N; i++) {
						final double v = sliders[i].getValue() * stdev[i] / 100.0;
						currentValue[i] = v;
					}
				}

				final PointList newShape = pdm.generateNewShape(currentValue);
				final PointList tfShape = newShape.transform(TransformUtilities.translateMatrix(300, 300).times(
						TransformUtilities.scaleMatrix(150, 150)));

				final List<Line2d> lines = connections.getLines(tfShape);
				frame.drawLines(lines, 1, 1f);

				for (final Point2d pt : tfShape) {
					frame.drawPoint(pt, 1f, 5);
				}

				for (int i = 0; i < N; i++) {
					final int newVal = (int) (100.0 * currentValue[i] / stdev[i]);
					sliders[i].setValue(newVal);
				}
			}
		}, base);

		final JPanel p = new JPanel();
		p.setOpaque(false);
		final JCheckBox cb = new JCheckBox();
		cb.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (cb.isSelected()) {
					a = makeAnimator(60, stdev, currentValue);
					animate = true;
				} else {
					animate = false;
				}
			}
		});
		p.add(new JLabel("animate:"));
		p.add(cb);
		final JSeparator vh = new JSeparator(SwingConstants.HORIZONTAL);
		sliderPanel.add(vh);
		sliderPanel.add(p);

		final JSeparator vs = new JSeparator(SwingConstants.VERTICAL);
		base.add(vs);
		base.add(sliderPanel);

		return base;
	}

	public static DoubleArrayValueAnimator makeAnimator(int duration, double[] maxs, double[] initial) {
		final RandomLinearDoubleValueAnimator[] animators = new RandomLinearDoubleValueAnimator[maxs.length];

		for (int i = 0; i < maxs.length; i++)
			animators[i] = new RandomLinearDoubleValueAnimator((-maxs[i]), maxs[i], duration, initial[i]);

		return new DoubleArrayValueAnimator(animators);
	}

	@Override
	public void close() {
		display.close();
	}

	public static void main(String[] args) throws IOException {
		new SlideshowApplication(new PDMDemo(), 1024, 768);
	}
}
