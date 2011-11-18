package org.openimaj.demos.acmmm11.presentation.slides;

import java.awt.Component;
import java.io.IOException;
import java.util.List;

import org.openimaj.demos.acmmm11.presentation.OpenIMAJ_ACMMM2011;
import org.openimaj.demos.utils.slideshowframework.Slide;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
import org.openimaj.image.feature.local.detector.dog.collector.Collector;
import org.openimaj.image.feature.local.detector.dog.collector.OctaveKeypointCollector;
import org.openimaj.image.feature.local.detector.dog.extractor.DominantOrientationExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.GradientFeatureExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.OrientationHistogramExtractor;
import org.openimaj.image.feature.local.detector.dog.pyramid.BasicOctaveExtremaFinder;
import org.openimaj.image.feature.local.detector.dog.pyramid.DoGOctaveExtremaFinder;
import org.openimaj.image.feature.local.detector.dog.pyramid.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.engine.ALTDoGSIFTEngine;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.engine.DoGSIFTEngineOptions;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.processing.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.transforms.AffineTransformModel;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.Pair;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import Jama.Matrix;

public class SIFTAltSIFTSlide implements Slide, VideoDisplayListener<MBFImage> {

	private MBFImage outFrame;
	private DoGSIFTEngine normalEngine;
	private DoGSIFTEngine altEngine;
	private FImage carpetGrey;
	private MBFImage carpet;
	private ConsistentLocalFeatureMatcher2d<Keypoint> normalmatcher;
	private ConsistentLocalFeatureMatcher2d<Keypoint> altmatcher;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		carpet = ImageUtilities.readMBF(SIFTAltSIFTSlide.class.getResource("rabbit.jpeg"));
//		carpet.processInline(new ResizeProcessor(0.3f));
		this.carpetGrey = carpet.flatten();
		
		SpinningImageVideo spinning = new SpinningImageVideo(carpet,0.005f);
		
		outFrame = new MBFImage(spinning.getWidth() * 2, spinning.getHeight() * 2,3);
		normalEngine = new DoGSIFTEngine();
		normalEngine.getOptions().setDoubleInitialImage(false);
		altEngine = new ALTDoGSIFTEngine();
		altEngine.getOptions().setDoubleInitialImage(false);
		
		LocalFeatureList<Keypoint> carpetNormalKPTs = normalEngine.findFeatures(carpetGrey);
		normalmatcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
				new FastBasicKeypointMatcher<Keypoint>(8),
				new RANSAC<Point2d, Point2d>(new AffineTransformModel(5), 100, new RANSAC.BestFitStoppingCondition(), true)
		);
		normalmatcher.setModelFeatures(carpetNormalKPTs);
		
		
		LocalFeatureList<Keypoint> carpetAltKPTs = altEngine.findFeatures(carpetGrey);
		altmatcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
				new FastBasicKeypointMatcher<Keypoint>(8),
				new RANSAC<Point2d, Point2d>(new AffineTransformModel(5), 100, new RANSAC.BestFitStoppingCondition(), true)
		);
		altmatcher.setModelFeatures(carpetAltKPTs);
		
		VideoDisplay.createOffscreenVideoDisplay(spinning).addVideoListener(this);
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String args[]) throws IOException{
		SIFTAltSIFTSlide s = new SIFTAltSIFTSlide();
		s.getComponent(100, 100);
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		outFrame.fill(RGBColour.BLACK);
		FImage fGrey = frame.flatten();
		LocalFeatureList<Keypoint> normalKPTs = normalEngine.findFeatures(fGrey);
		LocalFeatureList<Keypoint> altKPTs = altEngine.findFeatures(fGrey);
		
		Matrix carpetOffset = TransformUtilities.translateMatrix(frame.getWidth()/4,frame.getHeight()*2/3);
		Matrix normalOffset = TransformUtilities.translateMatrix(frame.getWidth(),0);
		Matrix altOffset = TransformUtilities.translateMatrix(frame.getWidth(), frame.getHeight());
		
		outFrame.drawImage(carpet,frame.getWidth()/4,frame.getHeight()*2/3);
		outFrame.drawImage(frame, frame.getWidth(), 0);
		outFrame.drawImage(frame, frame.getWidth(), frame.getHeight());
		
		normalmatcher.findMatches(normalKPTs);
		List<Pair<Keypoint>> normalMatches = normalmatcher.getMatches();
		for(Pair<Keypoint> kps : normalMatches){
			Keypoint p1 = kps.firstObject().transform(normalOffset);
			Keypoint p2 = kps.secondObject().transform(carpetOffset);
			
			outFrame.drawPoint(p1, RGBColour.RED, 3);
			outFrame.drawPoint(p2, RGBColour.RED, 3);
			
			outFrame.drawLine(new Line2d(p1,p2), 3, RGBColour.GREEN);
		}
		
		altmatcher.findMatches(normalKPTs);
		List<Pair<Keypoint>> altMatches = normalmatcher.getMatches();
		for(Pair<Keypoint> kps : altMatches){
			Keypoint p1 = kps.firstObject().transform(altOffset);
			Keypoint p2 = kps.secondObject().transform(carpetOffset);
			
			outFrame.drawPoint(p1, RGBColour.RED, 3);
			outFrame.drawPoint(p2, RGBColour.RED, 3);
			
			outFrame.drawLine(new Line2d(p1,p2), 3, RGBColour.BLUE);
		}
		
		DisplayUtilities.displayName(outFrame,"wang");
	}

}
