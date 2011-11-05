package org.openimaj.demos;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.openimaj.feature.DoubleFV;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointVisualizer;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.ml.clustering.random.RandomByteCluster;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

enum Mode {
	RGB_HISTOGRAM {
		HistogramModel model = new HistogramModel(4,4,4);
		Float[][] binCols = null;
		
		@Override
		public DoubleFV createFeature(MBFImage image) {
			model.estimateModel(image);
			return model.histogram;
		}

		void buildBinCols() {
			binCols = new Float[4*4*4][3];
			for (int k=0; k<4; k++) {
				for (int j=0; j<4; j++) {
					for (int i=0; i<4; i++) {
						binCols[k*4*4 + j*4 + i][0] = (float)i/4 + (0.5f/4);
						binCols[k*4*4 + j*4 + i][1] = (float)j/4 + (0.5f/4);
						binCols[k*4*4 + j*4 + i][2] = (float)k/4 + (0.5f/4);
					}
				}
			}
		}
		
		@Override
		public Float[] colourForBin(int bin) {
			if (binCols == null) buildBinCols();
			
			return binCols[bin];
		}
	},
//	HSV_HISTOGRAM {
//		HistogramModel model = new HistogramModel(10,4,4);
//		Float[][] binCols = null;
//		
//		@Override
//		public DoubleFV createFeature(MBFImage image) {
//			image = Transforms.RGB_TO_HSV(image);
//			model.estimateModel(image);
//			return model.histogram;
//		}
//		
//		void buildBinCols() {
//			binCols = new Float[10*4*4][];
//			for (int k=0; k<10; k++) {
//				for (int j=0; j<4; j++) {
//					for (int i=0; i<4; i++) {
//						float h = (float)i/10 + (0.5f/10);
//						float s = (float)j/4 + (0.5f/4);
//						float v = (float)k/4 + (0.5f/4);
//						
//						MBFImage img = new MBFImage(1,1,ColourSpace.HSV);
//						img.setPixel(0, 0, new Float[] {h,s,v});
//						
//						img = Transforms.HSV_TO_RGB(img);
//						
//						binCols[k*4*4 + j*4 + i] = img.getPixel(0, 0);
//					}
//				}
//			}
//		}
//		
//		@Override
//		public Float[] colourForBin(int bin) {
//			if (binCols == null) buildBinCols();
//			
//			return binCols[bin];
//		}
//	}
	SIFT {
		RandomByteCluster rabc = null;
		DoubleFV fv = null;
		DoGSIFTEngine engine = new DoGSIFTEngine();
		
		@Override
		public DoubleFV createFeature(MBFImage image) {
			if (rabc == null) {
				try {
					rabc = IOUtils.read(Mode.class.getResourceAsStream("random-100-highfield-codebook.voc"), RandomByteCluster.class);
					rabc.optimize(false);
					fv = new DoubleFV(rabc.getNumberClusters());
					engine.getOptions().setDoubleInitialImage(false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			FImage img = Transforms.calculateIntensity(image);
			img = ResizeProcessor.halfSize(img);
			List<Keypoint> keys = engine.findFeatures(img);
			
//			KeypointVisualizer<Float[], MBFImage> vis = new KeypointVisualizer<Float[],MBFImage>(image, keys);
//			image.internalAssign(vis.drawCenter(RGBColour.RED));
			for (Keypoint keypoint : keys) {
				image.drawPoint(new Point2dImpl(keypoint.x * 2f, keypoint.y * 2f), RGBColour.RED, 3);
			}
			
			Arrays.fill(fv.values, 0);
			
			for (Keypoint k : keys) {
				fv.values[rabc.push_one(k.ivec)]++;
			}
			
			return fv;
		}

		@Override
		public Float[] colourForBin(int bin) {
			return RGBColour.RED;
		}
		
	}
	;
	public abstract DoubleFV createFeature(MBFImage image);
	public abstract Float[] colourForBin(int bin);
}


public class VideoBagOfWords implements VideoDisplayListener<MBFImage>, KeyListener {
	private VideoCapture capture;
	private VideoDisplay<MBFImage> videoDisplay;
	private Mode mode = Mode.RGB_HISTOGRAM;
	private JFrame histogramFrame;
	private MBFImage histogramImage;
	
	public VideoBagOfWords() throws IOException {
		histogramImage = new MBFImage(640, 60, ColourSpace.RGB);
		histogramFrame = DisplayUtilities.display(histogramImage, "BoVW - " + mode);
		histogramFrame.setLocation(0, 523);
		
		capture = new VideoCapture(640, 480);
		
		videoDisplay = VideoDisplay.createVideoDisplay(capture);
		videoDisplay.addVideoListener(this);
		((JFrame) SwingUtilities.getRoot(videoDisplay.getScreen())).addKeyListener(this);
	}
	
	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		
	}

	@Override
	public synchronized void beforeUpdate(MBFImage frame) {
		DoubleFV histogram = mode.createFeature(frame);
		
		drawHistogramImage(histogram);
		DisplayUtilities.display(histogramImage, histogramFrame);
	}
	
	private void drawHistogramImage(DoubleFV histogram) {
		histogram = histogram.normaliseFV();
		
		final int width = histogramImage.getWidth();
		final int height = histogramImage.getHeight();
		
		int bw = width / histogram.length();
		
		histogramImage.zero();
		MBFImageRenderer renderer = histogramImage.createRenderer();
		Rectangle s = new Rectangle();
		s.width = bw;
		for (int i=0; i<histogram.values.length; i++) {
			int rectHeight = (int) (histogram.values[i] * height);
			int remHeight = height - rectHeight;
			
			s.x = i * bw;
			s.y = remHeight;
			s.height = rectHeight;
			renderer.drawShapeFilled(s, mode.colourForBin(i));
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public synchronized void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == ' ') {
			int newOrdinal = mode.ordinal() + 1;
			if (newOrdinal >= Mode.values().length)
				newOrdinal = 0;
			
			mode = Mode.values()[newOrdinal];
			
			histogramFrame.setTitle("BoVW - " + mode.toString());
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) throws IOException {
		new VideoBagOfWords();
	}
}
