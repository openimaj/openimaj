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
package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;
import org.openimaj.video.Video;

/**
 * Slide illustrating colour histogram extraction.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ColourHistogramGrid extends TutorialPanel {
	private static final long serialVersionUID = 4894581289602770940L;

	/**
	 * Default constructor
	 * 
	 * @param capture
	 * @param width
	 * @param height
	 */
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
		MBFImage colourGrid = new MBFImage(80,image.getHeight(),3);
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
