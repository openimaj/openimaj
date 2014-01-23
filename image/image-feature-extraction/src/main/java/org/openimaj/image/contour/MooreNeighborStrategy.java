package org.openimaj.image.contour;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.util.function.Operation;

/**
 * The Moore Neighborhood border tracing strategy as described by 
 * http://www.imageprocessingplace.com/downloads_V3/root_downloads/tutorials/contour_tracing_Abeer_George_Ghuneim/moore.html
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class MooreNeighborStrategy extends BorderFollowingStrategy{
	
	enum DIRECTION{
		NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST;
		static int[] dirx = new int[]{ 0, 1, 1, 1, 0,-1,-1,-1};
		static int[] diry = new int[]{-1,-1, 0, 1, 1, 1, 0,-1};
		static DIRECTION[] entry = new DIRECTION[]{
			WEST,WEST,NORTH,NORTH,EAST,EAST,SOUTH,SOUTH
		};
		public DIRECTION clockwise(){
			DIRECTION[] vals = DIRECTION.values();
			DIRECTION dir = vals[(ordinal() + 1) % vals.length];
			
			return dir;
		}
		
		public boolean active(FImage img, Pixel point){
			int ord = ordinal();
			int yy = point.y + diry[ord];
			int xx = point.x + dirx[ord];
			if(xx < 0 || xx >= img.width || yy < 0 || yy >= img.height) return false;
			float pix = img.pixels[yy][xx];
			return pix != 0;
		}
		
		public DIRECTION clockwiseEntryDirection(){
			return entry[ordinal()];
		}

		public static DIRECTION fromTo(Pixel from, Pixel to) {
			if(from.equals(to))return null;
			if(from.y == to.y){
				if(from.x < to.x)return EAST;
				else return WEST;
			}
			else if(from.y<to.y){
				if(from.x == to.x)return SOUTH;
				else if(from.x<to.x) return SOUTH_EAST;
				else return SOUTH_WEST;
			}
			else{
				if(from.x == to.x)return NORTH;
				if(from.x<to.x)return NORTH_EAST;
				else return NORTH_WEST;
			}
		}

		public Pixel pixel(Pixel from) {
			return new Pixel(from.x + dirx[ordinal()],from.y + diry[ordinal()]);
		}
	}

	
	@Override
	public void border(FImage image, Pixel start, Pixel from, Operation<Pixel> operation) {
		Pixel p = start;
		if(image.pixels[start.y][start.x] == 0) return;
		operation.perform(start);
		DIRECTION cdirStart = DIRECTION.fromTo(p,from);
		DIRECTION firstCdir = cdirStart;
		DIRECTION cdir = cdirStart.clockwise();
		while(cdir != cdirStart){
			if(cdir.active(image, p)){
				Pixel c = cdir.pixel(p);
				cdirStart = cdir.clockwiseEntryDirection();
				if(c.equals(start) && firstCdir == cdirStart){
					return;
				}
				operation.perform(c);
				p = c;
				cdir = cdirStart.clockwise();				
			}
			else{
				cdir = cdir.clockwise();
			}
		}
	}

	

}
