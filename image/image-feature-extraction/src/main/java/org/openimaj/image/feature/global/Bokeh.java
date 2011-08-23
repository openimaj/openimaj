package org.openimaj.image.feature.global;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processor.GridProcessor;
import org.openimaj.image.processor.ImageProcessor;

public class Bokeh implements ImageProcessor<FImage>, FeatureVectorProvider<DoubleFV> {
	class BokehProcessor implements GridProcessor<Float, FImage> {
		BlurredPixelProportion bpp = new BlurredPixelProportion();
		int nBlocksX = 5;
		int nBlocksY = 5;
		
		@Override
		public int getHorizontalGridElements() {
			return nBlocksX;
		}

		@Override
		public int getVericalGridElements() {
			return nBlocksY;
		}

		@Override
		public Float processGridElement(FImage patch) {
			patch.processInline(bpp);
			return (float) bpp.getBlurredPixelProportion();
		}
	}
	
	BokehProcessor processor = new BokehProcessor();
	double bokeh;
	
	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double [] { bokeh });
	}
	
	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		FImage blocks = image.process(processor);
		
		System.out.println(blocks);
		
		double Qbokeh = 0;
		for (int y=0; y<blocks.height; y++) {
			for (int x=0; x<blocks.width; x++) {
				Qbokeh += blocks.pixels[y][x] > 0.5 ? 1 : 0;
			}
		}
		Qbokeh /= (blocks.height * blocks.width);
			
		bokeh = Qbokeh;
	}
	
	public static void main(String [] args) throws MalformedURLException, IOException {
		Bokeh s = new Bokeh();
		FImage image = ImageUtilities.readF(new URL("http://upload.wikimedia.org/wikipedia/commons/8/8a/Josefina_with_Bokeh.jpg"));
		DisplayUtilities.display(image);
		image.process(s);
		System.out.println(s.bokeh);
	}
}
