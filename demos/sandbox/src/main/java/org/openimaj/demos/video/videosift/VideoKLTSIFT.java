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
package org.openimaj.demos.video.videosift;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.openimaj.demos.video.utils.PolygonDrawingListener;
import org.openimaj.demos.video.utils.PolygonExtractionProcessor;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.MatrixTransformProvider;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
import org.openimaj.math.geometry.transforms.residuals.SingleImageTransferResidual2d;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.tracking.klt.Feature;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;

import Jama.Matrix;

public class VideoKLTSIFT implements KeyListener, VideoDisplayListener<MBFImage> {
	enum Mode {
		TRACKING, LOOKING, NONE, START_LOOKING
	}

	private VideoCapture capture;
	private VideoDisplay<MBFImage> videoFrame;
	private KLTTracker tracker;
	private FeatureList fl;

	private FImage oldFrame;
	private int frameNumber = 0;
	private int nFeatures = 50;
	private int nOriginalFoundFeatures = -1;
	private DoGSIFTEngine engine;
	private PolygonDrawingListener polygonListener;
	private Mode mode = Mode.NONE;
	private FeatureList initialFeatures;
	private Polygon initialShape;
	private ConsistentLocalFeatureMatcher2d<Keypoint> siftMatcher;
	private MBFImage modelImage;
	private MBFImage overlayFrame = null;

	public VideoKLTSIFT() throws Exception {
		capture = new VideoCapture(640, 480);
		polygonListener = new PolygonDrawingListener();
		videoFrame = VideoDisplay.createVideoDisplay(capture);
		videoFrame.getScreen().addMouseListener(polygonListener);
		// videoFrame.getScreen().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		videoFrame.addVideoListener(this);
		SwingUtilities.getRoot(videoFrame.getScreen()).addKeyListener(this);

		reinitTracker();
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void beforeUpdate(MBFImage image) {
		this.polygonListener.drawPoints(image);
		if (videoFrame.isPaused())
		{
			return;
		}
		FImage greyFrame = null;
		greyFrame = Transforms.calculateIntensityNTSC(image);
		// If we are in looking mode, Use matcher to find a likely position
		// every 5th frame
		if (this.mode == Mode.LOOKING) {

			final Shape shape = findObject(greyFrame);
			if (shape == null) {
				this.oldFrame = greyFrame;
				return;
			}
			System.out.println("Object FOUND, switcihg to tracking mode");
			// If we find a likely position, init the tracker, we are now
			// tracking
			initTracking(greyFrame, shape);
			this.mode = Mode.TRACKING;
		}
		// If we are tracking, attempt to track the points every frame
		else if (this.mode == Mode.TRACKING) {

			continueTracking(greyFrame);
			// If we don't track enough points, look again.
			if (fl.countRemainingFeatures() == 0 || fl.countRemainingFeatures() < nOriginalFoundFeatures * 0.2)
			{
				System.out.println("Object LOST, switching to LOOKING mode");
				this.mode = Mode.LOOKING;
				reinitTracker();
			}
			// else if(fl.countRemainingFeatures() < nOriginalFoundFeatures *
			// 0.8){
			// initTracking(greyFrame,polygonToDraw);
			// }
		}
		else if (this.mode == Mode.START_LOOKING) {
			this.reinitTracker();
			final Polygon p = this.polygonListener.getPolygon().clone();
			this.polygonListener.reset();
			final MBFImage modelImage = capture.getCurrentFrame();
			this.initTracking(greyFrame, p);
			this.initObjectFinder(modelImage, p);
			this.oldFrame = greyFrame;
			mode = Mode.LOOKING;
			return;
		}

		if (overlayFrame != null) {
			drawOverlay(image);
		}
		else {
			drawDebug(image, greyFrame);
		}

		this.oldFrame = greyFrame;

	}

	private void drawOverlay(MBFImage image) {
		final ProjectionProcessor<Float[], MBFImage> proc = new ProjectionProcessor<Float[], MBFImage>();
		image.accumulateWith(proc);
		final Matrix model = this.estimateModel();
		if (model != null) {
			proc.setMatrix(model);
			this.overlayFrame.accumulateWith(proc);
		}
		image.internalAssign(proc.performProjection());
	}

	private void drawDebug(MBFImage image, FImage greyFrame) {
		this.polygonListener.drawPoints(image);

		final MBFImageRenderer renderer = image.createRenderer();

		if (this.initialShape != null) {
			renderer.drawPolygon(initialShape, RGBColour.RED);
		}
		if (this.initialFeatures != null) {
			image.internalAssign(MatchingUtilities.drawMatches(image, this.findAllMatchedPairs(), RGBColour.WHITE));
			final Matrix esitmatedModel = this.estimateModel();
			if (esitmatedModel != null)
			{
				final Polygon newPolygon = initialShape.transform(esitmatedModel);
				renderer.drawPolygon(newPolygon, RGBColour.GREEN);
				if (fl.countRemainingFeatures() < nOriginalFoundFeatures * 0.5) {
					reinitTracker();
					initTracking(greyFrame, newPolygon);
				}
			}
			estimateMovement();
		}
	}

	private Shape findObject(FImage capImg) {
		final float scaleImage = .5f;

		final ResizeProcessor resize = new ResizeProcessor(scaleImage);
		capImg = capImg.process(resize);
		Shape sh = null;
		if (siftMatcher != null && !videoFrame.isPaused() && engine != null) {
			final LocalFeatureList<Keypoint> kpl = engine.findFeatures(capImg);
			if (siftMatcher.findMatches(kpl)) {
				Matrix shTransform = ((MatrixTransformProvider) siftMatcher.getModel()).getTransform().copy();
				if (shTransform != null)
				{
					try {
						shTransform = TransformUtilities.scaleMatrix(1f / scaleImage, 1f / scaleImage).times(
								shTransform.inverse());
						sh = modelImage.getBounds().transform(shTransform);
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}

			}

		}
		return sh;

	}

	private void reinitTracker() {
		final TrackingContext tc = new TrackingContext();
		fl = new FeatureList(nFeatures);
		tracker = new KLTTracker(tc, fl);
		tracker.setVerbosity(0);

		tc.setSequentialMode(true);
		tc.setWriteInternalImages(false);
		tc.setAffineConsistencyCheck(-1); /*
										 * set this to 2 to turn on affine
										 * consistency check
										 */
		this.initialFeatures = null;
		this.initialShape = null;
	}

	public void initTracking(FImage greyFrame, Shape location) {
		frameNumber = 0;
		tracker.getTrackingContext().setTargetArea(location);
		tracker.selectGoodFeatures(greyFrame);
		nOriginalFoundFeatures = fl.countRemainingFeatures();
		initialFeatures = fl.clone();
		initialShape = location.asPolygon().clone();
	}

	public void continueTracking(FImage greyFrame) {
		tracker.trackFeatures(oldFrame, greyFrame);
		this.frameNumber++;
	}

	private Matrix estimateModel() {
		if (this.initialFeatures == null) {
			return null;
		}
		final List<? extends IndependentPair<Point2d, Point2d>> pairs = findAllMatchedPairs();
		final HomographyModel model = new HomographyModel();
		// model.estimate(pairs);
		final RANSAC<Point2d, Point2d, HomographyModel> fitter = new RANSAC<Point2d, Point2d, HomographyModel>(model,
				new SingleImageTransferResidual2d<HomographyModel>(),
				10.0, 1500,
				new RANSAC.PercentageInliersStoppingCondition(0.5), false);
		if (!fitter.fitData(pairs))
			return null;

		model.getTransform().print(5, 5);
		return model.getTransform();
	}

	private List<IndependentPair<Point2d, Point2d>> findAllMatchedPairs() {
		final List<IndependentPair<Point2d, Point2d>> pairs = new ArrayList<IndependentPair<Point2d, Point2d>>();
		for (int i = 0; i < this.initialFeatures.features.length; i++) {
			final Feature oldFeature = this.initialFeatures.features[i].clone();
			final Feature newFeature = fl.features[i].clone();
			if (oldFeature.val >= 0 && newFeature.val >= 0) {
				pairs.add(new IndependentPair<Point2d, Point2d>(oldFeature, newFeature));
			}
		}
		return pairs;
	}

	public Point2dImpl estimateMovement() {
		final Feature[] oldFeatures = this.initialFeatures.features;
		float sumX = 0;
		float sumY = 0;
		float total = 0;
		if (oldFeatures != null) {
			for (int i = 0; i < oldFeatures.length; i++) {
				final Feature oldFeature = oldFeatures[i];
				final Feature newFeature = fl.features[i];
				if (oldFeature.val >= 0 && newFeature.val >= 0) {
					sumX += newFeature.x - oldFeature.x;
					sumY += newFeature.y - oldFeature.y;
					total += 1f;
				}
			}
			sumX /= total;
			sumY /= total;
			System.out.println("Average displacement: " + sumX + "," + sumY);
		}
		return new Point2dImpl(sumX, sumY);
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public synchronized void keyPressed(KeyEvent key) {
		if (key.getKeyCode() == KeyEvent.VK_SPACE) {
			this.videoFrame.togglePause();
		} else if (key.getKeyChar() == 'c' && this.polygonListener.getPolygon().getVertices().size() > 2) {
			mode = Mode.START_LOOKING;
		} else if (key.getKeyChar() == 'd' && this.polygonListener.getPolygon().getVertices().size() > 2) {
			final Polygon p = this.polygonListener.getPolygon().clone();
			this.polygonListener.reset();
			overlayFrame = this.capture.getCurrentFrame().process(
					new PolygonExtractionProcessor<Float[], MBFImage>(p, RGBColour.BLACK));
		}
		else if (key.getKeyChar() == 'r') {
			this.mode = Mode.NONE;

		}
	}

	private void initObjectFinder(MBFImage frame, Polygon p) {
		modelImage = frame.process(new PolygonExtractionProcessor<Float[], MBFImage>(p, RGBColour.BLACK));

		// configure the matcher
		siftMatcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8));
		siftMatcher.setFittingModel(new RobustHomographyEstimator(10.0, 1500,
				new RANSAC.PercentageInliersStoppingCondition(0.5), HomographyRefinement.NONE));

		engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(true);

		final FImage modelF = Transforms.calculateIntensityNTSC(modelImage);
		siftMatcher.setModelFeatures(engine.findFeatures(modelF));
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public static void main(String args[]) throws Exception {
		new VideoKLTSIFT();
	}

}
