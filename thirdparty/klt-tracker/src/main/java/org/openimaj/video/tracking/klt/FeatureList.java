package org.openimaj.video.tracking.klt;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;



public class FeatureList implements Iterable<Feature> {
	public Feature[] features;

	/*********************************************************************
	 * KLTCreateFeatureList
	 *
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
	 * fname: name of file to write data; if NULL, then print to stderr
	 * fmt:   format for printing (e.g., "%5.1f" or "%3d");
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
}
