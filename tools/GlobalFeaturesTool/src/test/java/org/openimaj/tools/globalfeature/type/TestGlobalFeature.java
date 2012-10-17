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
package org.openimaj.tools.globalfeature.type;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.openimaj.feature.FeatureVector;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.tools.globalfeature.type.MaxHistogramExtractor;

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
		MaxHistogramExtractor maxHist = new MaxHistogramExtractor(ColourSpace.RGB, Arrays.asList(4,4,4));
		FeatureVector redCol = maxHist.extract(redImage, null);
		MBFImage redBlock = new MBFImage(300,300,ColourSpace.RGB);
		redBlock.fill(toCol(redCol));
		
//		MBFImage greenImage = ImageUtilities.readMBF(TestGlobalFeature.class.getResourceAsStream("/org/openimaj/image/data/green-rose.jpeg"));
		MBFImage greenImage = ImageUtilities.readMBF(new URL("http://farm7.staticflickr.com/6184/6082019288_757e418187_z.jpg"));
		maxHist = new MaxHistogramExtractor(ColourSpace.RGB, Arrays.asList(4,4,4));
		FeatureVector greenCol = maxHist.extract(greenImage, null);
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
