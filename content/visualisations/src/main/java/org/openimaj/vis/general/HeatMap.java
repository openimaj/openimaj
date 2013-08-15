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
