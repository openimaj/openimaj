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
package org.openimaj.demos.twitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openimaj.data.RandomData;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.image.typography.FontStyle.HorizontalAlignment;
import org.openimaj.image.typography.FontStyle.VerticalAlignment;
import org.openimaj.image.typography.general.GeneralFont;
import org.openimaj.image.typography.general.GeneralFontStyle;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;

import twitter4j.Status;

public class PrettyTagRenderer implements Operation<Context> {

	private static final int HEIGHT = 1024;
	private static final int WIDTH = 1024;
	private static final int GRID_WH = 160;
	private static final float GRID_CIRCLE = 0.9f;
	private static final long BLIP_TIME = 1000;
	private Map<String,Circle> hashCircles = new HashMap<String, Circle>();
	private Map<String,Float[]> hashColours = new HashMap<String, Float[]>();
	private Map<String,HashAggregation> hashAggregations = new HashMap<String, HashAggregation>();
	
	private Video<MBFImage> video;
	private Random rand = new Random();
	private List<IndependentPair<Point2dImpl,MBFImage>> textLayers = new ArrayList<IndependentPair<Point2dImpl, MBFImage>>();
	static class HashAggregation{
		int total;
		long lastSeen;
	}
	public PrettyTagRenderer(String[] hashStrings) {
		int index = 0;
		int nx = WIDTH/GRID_WH; 
		int[] randCols = RandomData.getUniqueRandomInts(hashStrings.length, 0, hashStrings.length);
		GeneralFont f = new GeneralFont( "Helvetica", java.awt.Font.PLAIN);
		
		MBFImageRenderer tmpRenderer = new MBFImage(0,0,ColourSpace.RGBA).createRenderer();
		for (String hash : hashStrings) {
			float y = (index / nx) * GRID_WH;
			float x = (index - (index / nx) * nx) * GRID_WH;
			float offset = 0;
			if(x == 0){
				offset = offset(hashStrings.length,index,nx);
			}
			y += GRID_WH/2;
			x += GRID_WH/2 + offset;
			float rad = GRID_WH*GRID_CIRCLE/2;
			this.hashCircles.put(hash, new Circle(x,y,rad));
			this.hashColours.put(hash, ColourMap.Autumn.apply(randCols[index] / (float)hashStrings.length));
			this.hashAggregations.put(hash,new HashAggregation());
			GeneralFontStyle<Float[]> fs = new GeneralFontStyle<Float[]>(f, tmpRenderer);
			Rectangle textSize = fs.getRenderer(tmpRenderer).getSize(hash, fs);
			MBFImage textLayer = new MBFImage((int)textSize.width,(int)textSize.height,ColourSpace.RGBA);
			MBFImageRenderer aaTextRend = textLayer.createRenderer(RenderHints.ANTI_ALIASED);
			fs = new GeneralFontStyle<Float[]>(f, aaTextRend);
			fs.setColour(new Float[]{1f,1f,1f,1f});
			fs.setHorizontalAlignment(HorizontalAlignment.HORIZONTAL_LEFT);
			fs.setVerticalAlignment(VerticalAlignment.VERTICAL_TOP);
			fs.setFontSize(16);
			aaTextRend.drawText(hash, new Point2dImpl(0, textSize.height*2/3), fs);
			this.textLayers.add(IndependentPair.pair(new Point2dImpl(x-textSize.width*1/3, y-textSize.height/3),textLayer));
			index++;
		}
		
		final MBFImage output = new MBFImage(WIDTH,HEIGHT,ColourSpace.RGB);
		
		
		
		video = new Video<MBFImage>(){

			private MBFImage frame = output;

			@Override
			public MBFImage getNextFrame() {
				this.frame.fill(RGBColour.BLACK);
				redrawCircles(this.frame);
				return frame;
			}

			@Override
			public MBFImage getCurrentFrame() {
				return frame;
			}

			@Override
			public int getWidth() {
				return WIDTH;
			}

			@Override
			public int getHeight() {
				return HEIGHT;
			}

			@Override
			public long getTimeStamp() {
				return -1;
			}

			@Override
			public double getFPS() {
				return 30;
			}

			@Override
			public boolean hasNextFrame() {
				return true;
			}

			@Override
			public long countFrames() {
				return -1;
			}

			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
			
		};
		start();
	}

	private void start() {
		VideoDisplay.createVideoDisplay(video);
	}

	private float offset(int N, int index, int nx) {
		if(N - index >= nx) return 0;
		int diff = N - index;
		
		return (diff * GRID_WH ) ;
	}

	private void redrawCircles(MBFImage output) {
		long now = System.currentTimeMillis();
		output.fill(RGBColour.WHITE);
		MBFImageRenderer rend = output.createRenderer(RenderHints.ANTI_ALIASED);
		for (String hash: this.hashCircles.keySet()) {
			Circle circle = this.hashCircles.get(hash);
			Float[] col = this.hashColours.get(hash);
			float level = 2;
			long lastSeen = this.hashAggregations.get(hash).lastSeen;
			if(lastSeen!=0){				
				long diff = Math.abs(lastSeen - now);
				if(diff < BLIP_TIME){
					level -= (1 - ( diff / (float)BLIP_TIME));
				}
			}
			Float[] offCircleColour = dark(col,level);
			
			drawHashCircle(rend , hash, circle, offCircleColour);
		}
		for (IndependentPair<Point2dImpl, MBFImage> pTextLayer : this.textLayers) {
			
			MBFImage textLayer = pTextLayer.getSecondObject();
			Point2d p = pTextLayer.firstObject();
			output.drawImage(textLayer , (int)p.getX(), (int)p.getY());
		}
	}

	private void drawHashCircle(MBFImageRenderer rend, String hash, Circle circle, Float[] colour) {
		
		rend.drawShapeFilled(circle, colour);
		rend.drawShape(circle, 3, RGBColour.BLACK);
			
	}

	private Float[] dark(Float[] col){
		return dark(col,2.f);
	}
	private Float[] dark(Float[] col,float amount) {
		if(amount == 0) return col;
		Float[] hsiCol = ColourSpace.HSV.convertFromRGB(col);
		hsiCol[2]/=amount;
		return ColourSpace.HSV.convertToRGB(hsiCol);
	}

	private Float[] inverse(Float[] col) {
		Float[] ret = new Float[]{
			1 - col[0],
			1 - col[1],
			1 - col[2],
		};
		return ret;
	}

	@Override
	public void perform(Context object) {
		String hashtag = object.getTyped(HashTagMatch.HASHTAG_KEY);
		Status status = object.getTyped("status");
		if(!this.hashCircles.containsKey(hashtag)) return;
		activate(hashtag,status);
	}
	
	
	
	private void activate(String hashtag, Status status) {
//		synchronized (hashAggregations) {
			HashAggregation aggr = this.hashAggregations.get(hashtag);
			aggr.total ++;
			long time = System.currentTimeMillis();
			aggr.lastSeen = time;
//		}
	}
	

}
