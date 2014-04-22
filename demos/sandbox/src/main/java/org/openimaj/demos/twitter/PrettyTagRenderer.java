package org.openimaj.demos.twitter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.openimaj.content.animation.animator.LinearFloatValueAnimator;
import org.openimaj.data.RandomData;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.Font;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.image.typography.FontStyle.HorizontalAlignment;
import org.openimaj.image.typography.FontStyle.VerticalAlignment;
import org.openimaj.image.typography.general.GeneralFont;
import org.openimaj.image.typography.general.GeneralFontStyle;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.video.AnimatedVideo;
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
	private GeneralFontStyle<Float[]> fs;
	private MBFImage textLayer;
	static class HashAggregation{
		int total;
		long lastSeen;
	}
	public PrettyTagRenderer(String[] hashStrings) {
		int index = 0;
		int nx = WIDTH/GRID_WH; 
		int[] randCols = RandomData.getUniqueRandomInts(hashStrings.length, 0, hashStrings.length);
		this.textLayer = new MBFImage(WIDTH,HEIGHT,ColourSpace.RGBA);
		this.textLayer.fill(new Float[]{0f,0f,0f,0f});
		GeneralFont f = new GeneralFont( "Ariel", java.awt.Font.PLAIN);
		this.fs = new GeneralFontStyle<Float[]>(f, textLayer.createRenderer());
		fs.setColour(RGBColour.WHITE);
		fs.setHorizontalAlignment(HorizontalAlignment.HORIZONTAL_CENTER);
		fs.setVerticalAlignment(VerticalAlignment.VERTICAL_TOP);
		fs.setFontSize(16);
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
			this.hashColours.put(hash, ColourMap.Rainbow.apply(randCols[index] / (float)hashStrings.length));
			this.hashAggregations.put(hash,new HashAggregation());
			this.textLayer.drawText(hash, new Point2dImpl(x, y), fs);
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
			
			drawHashCircle(output, hash, circle, offCircleColour);
		}
//		output.drawImage(textLayer, 0, 0);
	}

	private void drawHashCircle(MBFImage output, String hash, Circle circle, Float[] colour) {
		
		output.drawShapeFilled(circle, colour);
			
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
