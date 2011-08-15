package org.openimaj.image.feature.global;

import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.processing.CIEDE2000;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;

public class ColourContrast implements ImageProcessor<MBFImage>, FeatureVectorProvider<DoubleFV> {
	FelzenszwalbHuttenlocherSegmenter<MBFImage> segmenter = new FelzenszwalbHuttenlocherSegmenter<MBFImage>();
	double contrast;
	
	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { contrast });
	}

	@Override
	public void processImage(MBFImage image, Image<?, ?>... otherimages) {
		List<ConnectedComponent> ccs = segmenter.segment(image);
		MBFImage labImage = ColourSpace.convert(image, ColourSpace.CIE_Lab);
		float[][] avgs = new float[ccs.size()][3];
		int w = image.getWidth();
		int h = image.getHeight();
		
		//calculate patch average colours
		for (int i=0; i<avgs.length; i++) {
			for (Pixel p : ccs.get(i).pixels) {
				Float[] v = labImage.getPixel(p);
				
				avgs[i][0] += v[0];
				avgs[i][0] += v[1];
				avgs[i][0] += v[2];
			}
			int sz = ccs.get(i).pixels.size();
			avgs[i][0] /= sz;
			avgs[i][1] /= sz;
			avgs[i][2] /= sz;
		}
		
		for (int i=0; i<avgs.length; i++) {
			for (int j=i+1; j<avgs.length; j++) {
				ConnectedComponent ci = ccs.get(i);
				ConnectedComponent cj = ccs.get(i);
				float C = CIEDE2000.calculateDeltaE(avgs[i], avgs[j]);
				
				contrast += (1 - distance(ci, cj, w, h)) * ( C / ( ci.calculateArea() * cj.calculateArea() ) ); 
			}
		}
	}
	
	float distance(ConnectedComponent c1, ConnectedComponent c2, int w, int h) {
		double [] cen1 = c1.calculateCentroid();
		double [] cen2 = c2.calculateCentroid();
		
		double dx = (cen1[0] - cen2[0]) / w;
		double dy = (cen1[1] - cen2[1]) / h;
		
		return (float)( Math.sqrt(dx*dx + dy*dy) / Math.sqrt(2) );
	}
}
