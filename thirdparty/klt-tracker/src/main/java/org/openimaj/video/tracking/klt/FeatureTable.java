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
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A table storing features per frame
 * 
 * @author Stan Birchfield
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FeatureTable {
	/**
	 * The table of features
	 */
	public SortedMap<Integer, List<Feature>> features;
	
	/**
	 * The number of features 
	 */
	public int nFeatures;

	/*********************************************************************
	 * KLTCreateFeatureTable
	 * @param nFeatures 
	 *
	 */
	public FeatureTable(int nFeatures) {
		features = new TreeMap<Integer, List<Feature>>();
		this.nFeatures = nFeatures;
	}

	/**
	 * Store a list of features for the given frame number
	 * @param fl
	 * @param frame
	 */
	public void storeFeatureList(FeatureList fl, int frame) {
		ArrayList<Feature> list = new ArrayList<Feature>(fl.features.length);
		
		for (Feature f : fl.features)
			list.add(f.clone());
		
		features.put(frame, list);
	}

	/**
	 * Convert to a string representation.
	 * @param fmt
	 * @param comments
	 * @return the string representation.
	 */
	public String toString(String fmt, boolean comments) {
		String [] setup = IOUtils.setupTxtFormat(fmt);
		String format = setup[0];
		String type = setup[1];

		String s = IOUtils.getHeader(format, IOUtils.StructureType.FEATURE_TABLE, features.size(), nFeatures, comments);

		for (int j = 0 ; j < nFeatures; j++) {
			s += String.format("%7d | ", j);
			for (int i = 0 ; i < features.size(); i++)
				s += features.get(i).get(j).toString(format, type);
			s += "\n";
		}

		return s;
	}

	@Override
	public String toString() {
		return toString("%3d", false);
	}

	/**
	 * Write feature table to a file.
	 * @param fname
	 * @param fmt
	 * @throws IOException
	 */
	public void writeFeatureTable(File fname, String fmt) throws IOException
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

			dos.write(IOUtils.binheader_ft.getBytes("US-ASCII"));
			dos.writeInt(features.size());
			dos.writeInt(nFeatures);

			for (int j = 0 ; j < nFeatures ; j++)  {
				for (int i = 0 ; i < features.size() ; i++)  {
					features.get(j).get(i).writeFeatureBin(dos);
				}
			}

			dos.close();
		}
	}
}

