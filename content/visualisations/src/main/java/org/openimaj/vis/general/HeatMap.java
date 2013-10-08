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
package org.openimaj.vis.general;

import org.openimaj.image.FImage;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.vis.VisualisationImpl;

/**
 * Visualise heat maps
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class HeatMap extends VisualisationImpl<FImage>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -79182296227101887L;
	private ColourMap cm;
	/**
	 * @param width
	 * @param height
	 * @param map the colour map
	 */
	public HeatMap(int width, int height, ColourMap map) {
		super(width, height);
		this.cm = map;
	}
	
	/**
	 * Uses {@link ColourMap#Hot} by default
	 * @param width
	 * @param height
	 * 
	 */
	public HeatMap(int width, int height) {
		this(width,height,ColourMap.Hot);
		
	}
	@Override
	public void update() {
		this.visImage.drawImage(cm.apply(data), 0, 0);
	}
	
	/**
	 * @param d
	 */
	public void setData(double[][] d){
		FImage in = new FImage(d.length,d[0].length);
		for (int y = 0; y < in.height; y++) {
			for (int x = 0; x < in.width; x++) {
				in.pixels[y][x] = (float) d[y][x];
			}
		}
		ResizeProcessor rp = new ResizeProcessor(this.getWidth(), this.getHeight());
		setData(in.process(rp));
	}
	
	

}
