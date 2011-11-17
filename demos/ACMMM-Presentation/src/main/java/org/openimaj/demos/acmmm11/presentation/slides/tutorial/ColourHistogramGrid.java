package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.image.processing.algorithm.HistogramProcessor;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;
import org.openimaj.video.Video;

public class ColourHistogramGrid extends TutorialPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4894581289602770940L;
	private HaarCascadeDetector detector;

	public ColourHistogramGrid(Video<MBFImage> capture, int width, int height) {
		super("Colour Histogram", capture, width, height);
	}

	@Override
	public void doTutorial(MBFImage image) {
		HistogramModel model = new HistogramModel(10,4,1);
		MBFImage space = Transforms.RGB_TO_HSV(image);
		model.estimateModel(space);
		MultidimensionalHistogram feature = model.histogram;
		Float[][] colours = buildBinCols(feature);
		MBFImage colourGrid = new MBFImage(40,image.getHeight(),3);
		int sqW = (colourGrid.getWidth()/4);
		int sqH = (colourGrid.getHeight()/10);
		for(int y = 0; y < 4; y++){
			for(int k = 0; k < 10; k++){
				Rectangle draw = new Rectangle(y * sqW,sqH*k,sqW,sqH);
				colourGrid.drawShapeFilled(draw, colours[y * 10 + k]);
			}
		}
		
//		DisplayUtilities.displayName(colourGrid, "wang");
		image.drawImage(colourGrid, image.getWidth()-colourGrid.getWidth(), 0);
	}
	
	Float[][] buildBinCols(MultidimensionalHistogram feature) {
		Float[][] binCols = new Float[10*4*1][];
		double maxFeature = feature.max();
		if(maxFeature == 0) maxFeature = 1;
		for (int k=0; k<10; k++) {
			for (int j=0; j<4; j++) {
				float s = (float)j/4 + (0.5f/4);
				float h = (float)k/10 + (0.5f/10);
				
				MBFImage img = new MBFImage(1,1,ColourSpace.HSV);
				img.setPixel(0, 0, new Float[] {h,s,(float) (feature.get(k,j,0) / maxFeature)});
//				img.setPixel(0, 0, new Float[] {h,s,1f});
				
				img = Transforms.HSV_TO_RGB(img);
				
				binCols[j* 10 + k] = img.getPixel(0, 0);
			}
		}
		return binCols;
	}

}
