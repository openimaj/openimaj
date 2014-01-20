package org.openimaj.image.contour;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Given a binary image (1-connected and 0-connected regions) detect contours
 * and provide both the contours and a hierarchy of contour membership.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Suzuki, S.", "Abe, K."},
		title = "Topological Structural Analysis of Digitized Binary Image by Border Following",
		year = "1985",
		journal = "Computer Vision, Graphics and Image Processing",
		pages = { "32", "46" },
		month = "January",
		number = "1",
		volume = "30"
	)
public class SuzukiContourProcessor implements ImageProcessor<FImage>{
	private static enum BorderType{
		HOLE,
		OUTER
	}
	private static class Border{
		public BorderType type;
		Set<Border> children = new HashSet<Border>();
		public Border parent;
		
		private Pixel start;
		public Border(BorderType type) {
			this.type = type;
			this.start = new Pixel(0, 0);
		}
		public Border(BorderType type, int x, int y) {
			this.type = type;
			this.start = new Pixel(x, y);
		}
		
		public Border(BorderType type, Pixel p) {
			this.type = type;
			this.start = p;
		}
		public Border(int x, int y) {
			this.type = null;
			this.start = new Pixel(x,y);
		}
		public void setParent(Border bp) {
			this.parent = bp.parent;
			bp.children.add(this);
		}
		
	}
	
	Map<Integer,Border> borderMap;
	
	public SuzukiContourProcessor() {
		this.borderMap = new HashMap<Integer, Border>();
	}
	
	@Override
	public void processImage(FImage image) {
		int nbd = 2;
		int lnbd = 0;
		this.borderMap.put(lnbd, new Border(BorderType.HOLE));
		for (int i = 0; i < image.height; i++) {
			for (int j = 0; j < image.width; j++) {
				boolean isOuter = isOuterBorderStart(image, i, j);
				boolean isHole = isHoleBorderStart(image, i, j);
				if(!isOuter && !isHole) continue;
				Border border = new Border(j, i);
				Border borderPrime = this.borderMap.get(lnbd);
				if(isOuter){
					border.type = BorderType.OUTER;
					switch (borderPrime.type) {
					case OUTER:
						border.setParent(borderPrime.parent);
						break;
					case HOLE:
						border.setParent(borderPrime);
						break;
					}
				}
				else { // if(isHole)
					border.type = BorderType.HOLE;
					switch (borderPrime.type) {
					case OUTER:
						border.setParent(borderPrime);
						break;
					case HOLE:
						border.setParent(borderPrime.parent);
						break;
					}
				}
				
			}
		}
	}

	private boolean isOuterBorderStart(FImage image,int i, int j) {
		return( image.pixels[i][j] == 1 && (j == 0 || image.pixels[i][j-1] == 0));
	}
	private boolean isHoleBorderStart(FImage image,int i, int j) {
		return( image.pixels[i][j] >= 1 && (j == image.width-1 || image.pixels[i][j+1] == 0));
	}

}
