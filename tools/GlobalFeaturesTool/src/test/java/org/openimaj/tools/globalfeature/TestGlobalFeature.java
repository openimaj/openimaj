package org.openimaj.tools.globalfeature;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.openimaj.feature.FeatureVector;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), 
 *
 */
public class TestGlobalFeature {
	
	/**
	 * @throws IOException 
	 * 
	 */
	public void testMaxHistogram() throws IOException{
		MBFImage redImage = ImageUtilities.readMBF(TestGlobalFeature.class.getResourceAsStream("/org/openimaj/image/data/red-rose.jpeg"));
		MaxHistogramGlobalFeatureActor maxHist = new MaxHistogramGlobalFeatureActor(ColourSpace.RGB, Arrays.asList(4,4,4));
		FeatureVector redCol = maxHist.enact(redImage, null);
		MBFImage redBlock = new MBFImage(300,300,ColourSpace.RGB);
		redBlock.fill(toCol(redCol));
		
//		MBFImage greenImage = ImageUtilities.readMBF(TestGlobalFeature.class.getResourceAsStream("/org/openimaj/image/data/green-rose.jpeg"));
		MBFImage greenImage = ImageUtilities.readMBF(new URL("http://farm7.staticflickr.com/6184/6082019288_757e418187_z.jpg"));
		maxHist = new MaxHistogramGlobalFeatureActor(ColourSpace.RGB, Arrays.asList(4,4,4));
		FeatureVector greenCol = maxHist.enact(greenImage, null);
		MBFImage greenBlock = new MBFImage(300,300,ColourSpace.RGB);
		greenBlock.fill(toCol(greenCol));
//		DisplayUtilities.display(greenImage);
//		DisplayUtilities.display(greenBlock);
		MBFImage a = new MBFImage(600,300,ColourSpace.RGB);
		a.fill(new Float[]{0.125f,0.125f,0.125f});
		a.drawShapeFilled(new Rectangle(300,0,300,300), new Float[]{0.825f,0.825f,0.825f});
//		DisplayUtilities.display(a);
		
//		FeatureVector greenCol = maxHist.enact(redImage, null);
	}

	private Float[] toCol(FeatureVector redCol) {
		float[] f= (float[]) redCol.getVector();
		Float[] c = new Float[f.length];
		for (int i = 0; i < f.length; i++) {
			c[i] = f[i];
		}
		return c;
	}
	
//	public static void main(String[] args) throws IOException {
//		TestGlobalFeature blah = new TestGlobalFeature();
//		blah.testMaxHistogram();
//	}
}
