package org.openimaj.image.contour;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.util.function.Operation;

public class MooreNeighborStrategy extends BorderFollowingStrategy{
	
	enum DIRECTION{
		NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST;
		int[] dirx = new int[]{
			0,1,1,1,0,-1,-1,-1
		};
		int[] diry = new int[]{
			1,1,0,-1,-1,-1,0,1
		};
		public DIRECTION clockwise(DIRECTION other){
			DIRECTION[] vals = DIRECTION.values();
			DIRECTION dir = vals[(other.ordinal() + 1) % vals.length];
			
			return dir;
		}
		
		public boolean active(FImage img, DIRECTION dir, int x, int y){
			int ord = dir.ordinal();
			int yy = y + diry[ord];
			int xx = x + dirx[ord];
			if(xx < 0 || xx >= img.width || yy < 0 || yy >= img.height) return false;
			float pix = img.pixels[yy][xx];
			return pix != 0;
		}
	}

	private DIRECTION entry;
	
	/**
	 * {@link MooreNeighborStrategy} which assumes the first pixel was entered from the EAST
	 */
	public MooreNeighborStrategy() {
		this.entry = DIRECTION.EAST;
	}
	
	/**
	 * @param dir the direction of the start pixel entry
	 */
	public MooreNeighborStrategy(DIRECTION dir) {
		this.entry = dir;
	}
	
	@Override
	public void border(FImage image, Pixel start, Operation<Pixel> operation) {
		DIRECTION ent = this.entry;
		
		while(true){
			DIRECTION startEnt = ent;
//			while()
		}
	}

	

}
