/**
 * This source code file is part of a direct port of Stan Birchfield's implementation
 * of a Kanade-Lucas-Tomasi feature tracker. The original implementation can be found
 * here: http://www.ces.clemson.edu/~stb/klt/
 *
 * As per the original code, the source code is in the public domain, available
 * for both commercial and non-commercial use.
 */
package org.openimaj.video.tracking.klt;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Rectangle;


/**
 * A list of features
 * 
 * @author Stan Birchfield
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FeatureList implements Iterable<Feature> {
	/**
	 * The list of features
	 */
	public Feature[] features;

	/*********************************************************************
	 * KLTCreateFeatureList
	 * @param nFeatures 
	 */
	public FeatureList(int nFeatures)
	{
		features = new Feature[nFeatures];

		for (int i = 0 ; i < nFeatures ; i++) {
			features[i] = new Feature();
		}
	}

	/*********************************************************************
	 * KLTCountRemainingFeatures
	 * @return the number of remaining features
	 */
	public int countRemainingFeatures()
	{
		int count = 0;

		for (Feature f : features)
			if (f.val >= 0) count++;

		return count;
	}
	
	@Override
	public FeatureList clone(){
		FeatureList ret = new FeatureList(this.features.length);
		for(int i = 0; i < this.features.length; i++){
			ret.features[i] = this.features[i].clone();
		}
		return ret;
		
	}
	
	/*********************************************************************
	 * KLTWriteFeatureListToPPM
	 * @param img 
	 * @return a new image 
	 */
	public MBFImage drawFeatures(FImage img)
	{
		/* Allocate memory for component images */
		FImage redimg = img.clone();
		FImage grnimg = img.clone();
		FImage bluimg = img.clone();  

		/* Overlay features in red */
		for (int i = 0 ; i < features.length ; i++)
			if (features[i].val >= 0)  {
				int x = (int) (features[i].x + 0.5);
				int y = (int) (features[i].y + 0.5);
				for (int yy = y - 1 ; yy <= y + 1 ; yy++)
					for (int xx = x - 1 ; xx <= x + 1 ; xx++)  
						if (xx >= 0 && yy >= 0 && xx < img.width && yy < img.height)  {
							redimg.pixels[yy][xx] = 1f;
							grnimg.pixels[yy][xx] = 0f;
							bluimg.pixels[yy][xx] = 0f;
						}
			}

		return new MBFImage(redimg, grnimg, bluimg);
	}
	
	/*********************************************************************
	 * KLTWriteFeatureListToPPM
	 * @param img 
	 * @return input image
	 */
	public MBFImage drawFeatures(MBFImage img)
	{
		/* Allocate memory for component images */
		MBFImage out = img;

		/* Overlay features in red */
		for (int i = 0 ; i < features.length ; i++)
			if (features[i].val >= 0)  {
				int x = (int) (features[i].x + 0.5);
				int y = (int) (features[i].y + 0.5);
				for (int yy = y - 1 ; yy <= y + 1 ; yy++)
					for (int xx = x - 1 ; xx <= x + 1 ; xx++)  
						if (xx >= 0 && yy >= 0 && xx < img.getWidth()&& yy < img.getHeight())  {
							out.bands.get(0).setPixel(xx, yy, 1.0f);
						}
			}

		return out;
	}

	/*********************************************************************
	 * KLTWriteFeatureList()
	 * 
	 * Writes features to file or to screen.
	 *
	 * INPUTS
	 * @param fname name of file to write data; if NULL, then print to stderr
	 * @param fmt   format for printing (e.g., "%5.1f" or "%3d");
	 *        if NULL, and if fname is not NULL, then write to binary file.
	 * @throws IOException 
	 */
	public void writeFeatureList(File fname, String fmt) throws IOException
	{
		if (fmt != null) {  /* text file or stderr */ 
			if (fname != null) {
				PrintWriter bw = new PrintWriter(new FileOutputStream(fname)); 
				bw.write(toString(fmt, true));
				bw.close();
			} else {
				System.out.print(toString(fmt, false));
			}
		} else {  /* binary file */
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(fname));
			
			dos.write(IOUtils.binheader_fl.getBytes("US-ASCII"));
			dos.writeInt(features.length);
			for (Feature f : features)  {
				f.writeFeatureBin(dos);
			}
			
			dos.close();
		}
	}


	/**
	 * Convert to a string representation with the given format.
	 * @param fmt
	 * @param comments
	 * @return the string representation.
	 */
	public String toString(String fmt, boolean comments) {
		String [] setup = IOUtils.setupTxtFormat(fmt);
		String format = setup[0];
		String type = setup[1];

		String s = IOUtils.getHeader(format, IOUtils.StructureType.FEATURE_LIST, 0, features.length, comments);

		for (int i = 0 ; i < features.length; i++)  {
			s += String.format("%7d | ", i);
			s += features[i].toString(format, type);
			s += String.format("\n");
		}

		return s;
	}

	@Override
	public String toString() {
		return toString("%3d", false);
	}

	@Override
	public Iterator<Feature> iterator() {
		Iterator<Feature> iterator = new Iterator<Feature>() {
			private int index = 0;
			
			@Override
			public boolean hasNext() {
				int newindex = index;
				while (newindex<features.length) {
					if (features[newindex].val>=0) 
						return true;
					newindex++;
				}

				return false;
			}

			@Override
			public Feature next() {
				int newindex = index;
				while (newindex<features.length) {
					if (features[newindex].val>=0) 
						break;
					newindex++;
				}
				
				Feature f = features[newindex];
				
				index++;
				
				return f;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Remove not supported.");
			}			
		};
		
		return iterator;
	}
	
	/**
	 * 	Returns the bounding box of the features
	 *  @return the bounding box of the features
	 */
	public Rectangle getBounds()
	{
		float minX = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = Float.MIN_VALUE;
		
		for( Feature f : features )
		{
			if( f.val >= 0 )
			{
				minX = Math.min( minX, f.x );
				maxX = Math.max( maxX, f.x );
				minY = Math.min( minY, f.y );
				maxY = Math.max( maxY, f.y );
			}
		}
		
		return new Rectangle( minX, minY, maxX-minX, maxY-minY );
	}
}
