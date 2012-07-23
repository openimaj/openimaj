package org.openimaj.image.colour;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

public class ColourMaps {
	private static int[] t_gamma = new int[2048];
	private static int[] t_gamma_mm = new int[10000];
	static{
		
		for (int i=0; i<2048; i++) {
			float v = i/2048.0f;
			v = (float) (Math.pow(v, 3)* 6);
			t_gamma[i] = (int) (v*6*256);
		}
		int i;
		for (i=0; i<10000; i++) {
			float v = i/2048.0f;
			v = (float) (Math.pow(v, 3)* 6);
			t_gamma_mm[i] = (int) (v*6*256);
		}
	}
	
	/**
	 * Convert a greyscale image to a pseudo-colour image by applying a colourmap.
	 * @param input image to convert.
	 * @return colourised image.
	 */
	public static MBFImage Grey_To_Colour_MM(FImage input){
		MBFImage image = new MBFImage(input.width,input.height,3);
		
		final float [][] rb = image.getBand(0).pixels;
		final float [][] gb = image.getBand(1).pixels;
		final float [][] bb = image.getBand(2).pixels;
		
		for(int y = 0; y < input.height; y++){
			for(int x = 0; x < input.width; x++){
				int depth = (int) input.pixels[y][x];
				float r,g,b;
				int pval = t_gamma_mm[depth];
				int lb = pval & 0xff;
				switch (pval>>8) {
					case 0:
						r = 255;
						g = 255-lb;
						b = 255-lb;
						break;
					case 1:
						r = 255;
						g = lb;
						b = 0;
						break;
					case 2:
						r = 255-lb;
						g = 255;
						b = 0;
						break;
					case 3:
						r = 0;
						g = 255;
						b = lb;
						break;
					case 4:
						r = 0;
						g = 255-lb;
						b = 255;
						break;
					case 5:
						r = 0;
						g = 0;
						b = 255-lb;
						break;
					default:
						r = 0;
						g = 0;
						b = 0;
						break;
				}
				
				rb[y][x] = r/255.0f;
				gb[y][x] = g/255.0f;
				bb[y][x] = b/255.0f;
			}
		}
		return image;
		
	}
	
	/**
	 * Convert a greyscale image to a pseudo-colour image by applying a colourmap.
	 * @param input image to convert.
	 * @return colourised image.
	 */
	public static MBFImage Grey_To_Colour(FImage input){
		MBFImage image = new MBFImage(input.width,input.height,3);
		
		final float [][] rb = image.getBand(0).pixels;
		final float [][] gb = image.getBand(1).pixels;
		final float [][] bb = image.getBand(2).pixels;
		
		for(int y = 0; y < input.height; y++){
			for(int x = 0; x < input.width; x++){
				int depth = (int) input.pixels[y][x];
				float r,g,b;
				int pval = t_gamma[depth];
				int lb = pval & 0xff;
				switch (pval>>8) {
					case 0:
						r = 255;
						g = 255-lb;
						b = 255-lb;
						break;
					case 1:
						r = 255;
						g = lb;
						b = 0;
						break;
					case 2:
						r = 255-lb;
						g = 255;
						b = 0;
						break;
					case 3:
						r = 0;
						g = 255;
						b = lb;
						break;
					case 4:
						r = 0;
						g = 255-lb;
						b = 255;
						break;
					case 5:
						r = 0;
						g = 0;
						b = 255-lb;
						break;
					default:
						r = 0;
						g = 0;
						b = 0;
						break;
				}
				
				rb[y][x] = r/255.0f;
				gb[y][x] = g/255.0f;
				bb[y][x] = b/255.0f;
			}
		}
		return image;
		
	}

	/**
	 * Convert a greyscale image to a pseudo-colour image by applying a colourmap.
	 * @param input image to convert.
	 * @return colourised image.
	 */
	public static MBFImage Grey_TO_HeatRGB(FImage input){
		MBFImage image = new MBFImage(input.width,input.height,3);
		
		final float [][] rb = image.getBand(0).pixels;
		final float [][] gb = image.getBand(1).pixels;
		final float [][] bb = image.getBand(2).pixels;
		
		for(int y = 0; y < input.height; y++){
			for(int x = 0; x < input.width; x++){
				float prop;
				float r,g,b;
				if(input.pixels[y][x] < 0.5){
					prop = input.pixels[y][x] / 0.5f;
					r = 1f - prop;
					g = prop;
					b = 0f;
				}
				else{
					prop = (input.pixels[y][x]  - 0.5f) / 0.5f;
					r = 0;
					g = 1f - prop;
					b = prop;
				}
				rb[y][x] = r;
				gb[y][x] = g;
				bb[y][x] = b;
			}
		}
		return image;
	}
}
