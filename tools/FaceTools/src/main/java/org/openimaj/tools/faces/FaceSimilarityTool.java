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
/**
 * 
 */
package org.openimaj.tools.faces;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.similarity.FaceSimilarityEngine;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.matrix.similarity.SimilarityMatrix;

/**
 * Face similarity tool compares the faces in all given images and gives a score
 * for each comparison. The tool can be made to match only the first image
 * against all other images or comparing all images against all others.
 * <p>
 * This tool can be used both from the command-line and programmatically.
 * Programmatically, there are some convenience functions for comparing
 * {@link List}s of {@link File}s and lists of {@link FImage}s, however, you are
 * welcome to give the
 * {@link #getDistances(List, boolean, ImageGetter, FaceSimilarityEngine)}
 * method any {@link List} as long as you supply an {@link ImageGetter} that can
 * return {@link FImage}s from that list.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 
 * @created 30 May 2011
 */
public class FaceSimilarityTool {
	/**
	 * An interface to get images and names from a list of things. This allows
	 * us to pass in a list of stuff and compare against them without having to
	 * assume what the stuff is until later.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * 
	 * @created 3 Jun 2011
	 * @param <T>
	 */
	public interface ImageGetter<T> {
		/**
		 * Get the image associated with the item
		 * @param list list of items
		 * @param index index of item we're interested in
		 * @return the image
		 */
		public FImage getImage(List<T> list, int index);

		/**
		 * Get the name of the item
		 * @param list list of items
		 * @param index index of item we're interested in
		 * @return the name
		 */
		public String getName(List<T> list, int index);
	}

	/**
	 * Calculates the distance between all the faces in the first image with all
	 * the faces in the given images.
	 * 
	 * Faces are identified by their image file and the index into the file, so
	 * if an image does not contain any faces, it will return null from the map
	 * for that filename.
	 * 
	 * @param first The query image
	 * @param others The list of files to compare against
	 * @param strategy The strategy
	 * @return A Map giving the distance of every face with every other.
	 */
	public Map<String, Map<String, Double>> getDistances(File first,
			List<File> others, FaceSimilarityEngine<?, ?, FImage> strategy) {
		List<File> x = new ArrayList<File>();
		x.add(first);
		x.addAll(others);

		return this.getDistances(x, true, strategy);
	}

	/**
	 * Calculates the distance between faces in the given images. Faces are
	 * identified by their image file and the index into the file, so if an
	 * image does not contain any faces, it will return null from the map for
	 * that filename.
	 * 
	 * @param inputFiles The list of files to process
	 * @param strategy the strategy
	 * @return A Map giving the distance of every face with every other.
	 */
	public Map<String, Map<String, Double>> getDistances(List<File> inputFiles,
			FaceSimilarityEngine<?, ?, FImage> strategy) {
		return getDistances(inputFiles, false, strategy);
	}

	/**
	 * Calculates the distance between faces in the given images. Faces are
	 * identified by their image file and the index into the file, so if an
	 * image does not contain any faces, it will return null from the map for
	 * that filename.
	 * 
	 * @param inputFiles
	 *            The list of files to process
	 * @param withFirst
	 *            if TRUE, the first image in the list will be matched against
	 *            all others, otherwise all images are matches against each
	 *            other.
	 * @param strategy The strategy
	 * @return A Map giving the distance of every face with every other.
	 */
	public Map<String, Map<String, Double>> getDistances(List<File> inputFiles,
			boolean withFirst,
			FaceSimilarityEngine<?, ?, FImage> strategy) {
		return getDistances(inputFiles, withFirst, 
				new ImageGetter<File>() {
					@Override
					public FImage getImage(List<File> list, int index) {
						try {
//							System.out.println("Reading: " + list.get(index));
							FImage fi = ImageUtilities.readF(list.get(index));
							return fi;
						} catch (IOException e) {
							e.printStackTrace();
							return null;
						}
					}

					@Override
					public String getName(List<File> list, int index) {
						return list.get(index).getName();
					}

				}, strategy);
	}

	/**
	 * Calculates the distance between faces in the given images. Faces are
	 * identified by their image file and the index into the file, so if an
	 * image does not contain any faces, it will return null from the map for
	 * that filename.
	 * 
	 * @param imageIdentifiers
	 *            A list of image names
	 * @param inputImages
	 *            The list of images to process
	 * @param withFirst
	 *            if TRUE, the first image in the list will be matched against
	 *            all others, otherwise all images are matches against each
	 *            other.
	 * @param strategy The strategy.
	 * @return A Map giving the distance of every face with every other.
	 */
	public Map<String, Map<String, Double>> getDistances(
			List<String> imageIdentifiers, List<FImage> inputImages,
			boolean withFirst, 
			FaceSimilarityEngine<?, ?, FImage> strategy) {
		return getDistances(inputImages, withFirst, 
				new ImageGetter<FImage>() {
					@Override
					public FImage getImage(List<FImage> list, int index) {
						return list.get(index);
					}

					@Override
					public String getName(List<FImage> list, int index) {
						return "image" + index;
					}
				}, strategy);
	}

	/**
	 * This is the actual comparison function that performs the nested loops as
	 * necessary to match all the faces against each other.
	 * 
	 * @param <T>
	 *            The type of thing in the input list
	 * @param inputList
	 *            A list of things to process
	 * @param withFirst
	 *            Whether to compare the first against all others (TRUE) or
	 *            compare all against each other (FALSE)
	 * @param iGetter
	 *            The getter that can make FImages from the input list.
	 * @param strategy The strategy
	 * @return A Map giving the distance of every face with every other.
	 */
	public <T> Map<String, Map<String, Double>> getDistances(List<T> inputList,
			boolean withFirst, 
			ImageGetter<T> iGetter,
			FaceSimilarityEngine<?, ?, FImage> strategy) {

		// If we're only comparing the images against the first one,
		// the outer loop only needs to be perfomed once.
		int xx = 0;
		if (withFirst)
			xx = 1;
		else
			xx = inputList.size();

		for (int i = 0; i < xx; i++) {
			// Read the first image and extract the faces.
			FImage f1 = iGetter.getImage(inputList, i);
			String f1id = iGetter.getName(inputList, i);

			strategy.setQuery(f1, f1id);

			// Now loop through all the other images.
			for (int j = 0; j < inputList.size(); j++) {
				// If the two images we're comparing are the same one,
				// we can avoid doing an extra extraction here.
				if (i != j) {
					// Read the other image and imageid.
	                FImage f2 = iGetter.getImage( inputList, j);
	                String f2id = iGetter.getName(inputList,j);
					strategy.setTest(f2, f2id);
				} else {
					strategy.setQueryTest();
				}

				strategy.performTest();
			}
		}

		return strategy.getSimilarityDictionary();
	}

	//
	// /**
	// * Compares one set of facial features against another.
	// * Side-affects (and returns) the input results map that maps
	// * face to other face and score.
	// *
	// * @param m The results map to populate
	// * @param file1id The identifier of the first image
	// * @param file2id The identifier of the second image
	// * @param f1faces The faces in the first image
	// * @param f2faces The faces in the second image
	// * @return The input parameter <code>m</code>
	// */
	// public Map<String,Map<String,Double>> compareFaces(
	// Map<String,Map<String,Double>> m,
	// String file1id, String file2id,
	// List<KEDetectedFace> f1faces,
	// List<KEDetectedFace> f2faces,
	// FloatFVComparison comparisonFunction )
	// {
	// // Now compare all the faces in the first image
	// // with all the faces in the second image.
	// for( int ii = 0; ii < f1faces.size(); ii++ )
	// {
	// String face1id = file1id+":"+ii;
	// KEDetectedFace f1f = f1faces.get(ii);
	//
	// // NOTE that the distance matrix will be symmetrical
	// // so we only have to do half the comparisons.
	// for( int jj = 0; jj < f2faces.size(); jj++ )
	// {
	// double d = 0;
	// String face2id = null;
	//
	// // If we're comparing the same face in the same image
	// // we can assume the distance is zero. Saves doing a match.
	// if( f1faces == f2faces && ii == jj )
	// {
	// d = 0;
	// face2id = face1id;
	// }
	// else
	// {
	// // Compare the two feature vectors using the chosen
	// // distance metric.
	// KEDetectedFace f2f = f2faces.get(jj);
	// face2id = file2id+":"+jj;
	//
	// //TODO: other types of feature
	// FaceFVComparator<FacePatchFeature> comparator = new
	// FaceFVComparator<FacePatchFeature>(comparisonFunction);
	// FacePatchFeature.Factory factory = new FacePatchFeature.Factory();
	// FacePatchFeature f1fv = factory.createFeature(f1f, false);
	// FacePatchFeature f2fv = factory.createFeature(f2f, false);
	//
	// d = comparator.compare( f1fv, f2fv );
	// }
	//
	// // Put the result in the result map
	// Map<String,Double> mm = m.get( face1id );
	// if( mm == null )
	// m.put( face1id,
	// mm = new HashMap<String,Double>() );
	// mm.put( face2id, d );
	// }
	// }
	//
	// return m;
	// }

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            The arguments to parse
	 * @return The tool options class
	 */
	private static FaceSimilarityToolOptions parseArgs(String[] args) {
		FaceSimilarityToolOptions fdto = new FaceSimilarityToolOptions();
		CmdLineParser parser = new CmdLineParser(fdto);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err
					.println("java FaceSimilarityTool [options...] IMAGE-FILES");
			parser.printUsage(System.err);
			return null;
		}

		return fdto;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		FaceSimilarityToolOptions o = parseArgs(args);
		if(o == null) return;

//		Map<String, Rectangle> bb = new HashMap<String, Rectangle>();
		FaceSimilarityEngine<?, ?, FImage> strat = o.strategy.strategy();
		strat.setCache(o.cache);
		new FaceSimilarityTool().getDistances(o.inputFiles, o.withFirst, strat);
		// System.out.println( "Map:" +m);

		if (o.boundingBoxes) {
			Map<String, Rectangle> bb = strat.getBoundingBoxes();
			for (String k : bb.keySet()) {
				Rectangle r = bb.get(k);
				System.out.println(k + ":" + r.x + "," + r.y + "," + r.width
						+ "," + r.height);
			}
		}
		SimilarityMatrix similarityMatrix = strat.getSimilarityMatrix(o.invertIfRequired);
		if(o.output!=null){
			try {
				IOUtils.writeBinary(o.output, similarityMatrix);
			} catch (IOException e) {
				System.err.println("Couldn't output file: " + e.getMessage());
			}
		}
		
		System.out.println(similarityMatrix);
		
//		// Pretty print the matrix
//		Set<String> xx = null;
//		if (o.withFirst)
//			xx = m.get(m.keySet().iterator().next()).keySet();
//		else
//			xx = m.keySet();
//
//		int maxLen = 0;
//		for (String f : xx)
//			if (f.length() > maxLen)
//				maxLen = f.length();
//
//		System.out.print(new PrintfFormat("%+" + maxLen + "s").sprintf(""));
//		for (String f : m.keySet())
//			System.out.print(new PrintfFormat("%8s").sprintf(f.substring(f
//					.lastIndexOf(":"))));
//		System.out.println();
//
//		for (String f : xx) {
//			String s = f;
//			if (f.length() < maxLen)
//				s = new PrintfFormat("%+" + maxLen + "s").sprintf(f);
//			System.out.print(s + ":");
//
//			Set<String> zz = m.keySet();
//
//			// Iterate over the outer map so that we get the same ordering
//			// of the inner map
//			boolean first = true;
//			for (String ff : zz) {
//				Double d = null;
//				if (m.get(f) != null)
//					d = m.get(f).get(ff);
//
//				if (d == null)
//					if (m.get(ff) != null)
//						d = m.get(ff).get(f);
//
//				if (d == null)
//					System.out.print("        ");
//				else {
//					if (!first)
//						System.out.print(",");
//					System.out.print(new PrintfFormat("%7.2f").sprintf(d));
//					first = false;
//				}
//			}
//
//			System.out.println();
//		}
	}
}
