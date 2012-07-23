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
package org.openimaj.tools.globalfeature;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.feature.ByteFV;
import org.openimaj.feature.ByteFVComparison;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FVComparator;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.feature.IntFV;
import org.openimaj.feature.IntFVComparison;
import org.openimaj.feature.ShortFV;
import org.openimaj.feature.ShortFVComparison;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

import Jama.Matrix;

/**
 *	This is a stand-alone tool that provides a means for comparing a
 *	collection of images against each other.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created Oct 21, 2011
 *	
 */
public class CollectionComparisonTool
{
	/**
	 * 	This mandatory argument provides the name of a directory in which
	 * 	all images will be found.
	 */
	@Option(name="--dir", aliases="-d", usage="Directory of images", 
			required=true )
			private String dir = null;

	/**
	 * 	This is a mandatory argument that is used to provide the feature
	 * 	comparison metric.
	 */
	@Option(name="--metric", aliases="-m", usage="Comparison metric", 
			required=true)
			private FeatureComparison compare = null;

	/**
	 * 	This is a mandatory argument that is used to provide the feature
	 * 	type to generate for each image.
	 */
	@Option(name="--feature-type", aliases="-f", 
			handler=ProxyOptionHandler.class, usage="Feature type", 
			required=true)
			private GlobalFeatureType feature = null;
			private GlobalFeatureExtractor featureOp;

	/**
	 * 	This optional argument provides the ability to 'binarise' the output
	 * 	such that distance values over the threshold will be set to 0,
	 * 	and distances under the threshold set to 1.
	 */
	@Option(name="--threshold", aliases="-t", usage="Threshold distances",
			required=false)
			private double threshold = -1;

	/**
	 * 	This optional argument provides a regular expression against which
	 * 	files in the directory will be filtered against.
	 */
	@Option(name="--regex", aliases="-r", usage="Filename regex pattern",
			required=false )
			private String regex = null;

	/**
	 * 	This is an optional parameter that allows the user to provide a single
	 * 	image against which all the other images will be compared. 
	 */
	@Option(name="--image", aliases="-i", usage="Single comparison image", 
			required=false)
			private String image = null;

	/**
	 * 	This optional parameter that shows verbose output
	 */
	@Option(name="--verbose", aliases="-v", usage="Verbose output",
			required=false)
			private boolean verbose = false;

	/**
	 * 	This optional parameter allows features to be cached
	 */
	@Option(name="--cache", usage="Cache features in RAM",
			required=false)
			private boolean cache = false;

	/**
	 * 	This optional parameter allows the clusters of 'similar' images
	 *  to be output rather than the matrix. The threshold must be set
	 *  for this to work.
	 */
	@Option(name="--printClusters", aliases="-pc", usage="Print the clusters rather than the matrix",
			required=false)
			private boolean printClusters = false;

	private Map<File, FeatureVector> cacheData = new HashMap<File, FeatureVector>();

	private FeatureVector getFeatureVector(File file) throws IOException {
		FeatureVector fv = cacheData.get(file);

		if (fv == null) {
			MBFImage im1 = ImageUtilities.readMBF( file );
			fv = featureOp.extract(im1);

			if (cache) {
				cacheData.put(file, fv);
			}
		}

		return fv;
	}

	/**
	 * 	Execute the tool.
	 */
	private String execute()
	{
		List<String> dir1 = null;

		// Get a list of files
		List<String> dir2 = getListOfFiles( dir, true );

		// If we're going to compare a single image against a collection,
		// then we will take a different route here.
		if( image != null )
		{
			dir1 = new ArrayList<String>();
			dir1.add( image );
		}
		// Otherwise we'll compare all against all.
		else dir1 = dir2;

		Matrix m = new Matrix( dir1.size(), dir2.size() );
		for( int y = 0; y < dir1.size(); y++ )
		{
			String s1 = dir1.get(y);
			try
			{
				FeatureVector fv1 = getFeatureVector( new File(s1) );
				FVComparator<FeatureVector> fvc = getComp(fv1, compare);

				int xx = 0;
				if( dir1 == dir2 )
					xx = y;	
				for( int x = xx; x < dir2.size(); x++ )
				{
					String s2 = dir2.get(x);
					if( y == 0 )
						System.out.println( ""+x+": "+s2 );

					if( verbose )
						System.out.println( "Comparing "+s1+" against "+s2 );

					try
					{
						FeatureVector fv2 = getFeatureVector( new File(s2) );

						double d = 0;
						if( compare == FeatureComparison.EQUALS ) 
						{
							if( Arrays.equals( 
									fv1.asDoubleVector(), 
									fv2.asDoubleVector() ) )
								d = 1;
							else	d = 0;
						} 
						else 
						{
							double v = fvc.compare(fv1, fv2);
							d = (threshold==-1?v:(v>threshold?0:1));
						}		

						// Symmetric matrix
						m.set( y, x, d );
						m.set( x, y, d );
					}
					catch( Exception e )
					{
						e.printStackTrace();
						continue;
					}
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
				continue;
			}
		}

		if (printClusters && this.threshold != -1) {
			return getClusterInfo(dir1, dir2, m);
		} else {
			StringWriter sw = new StringWriter();
			PrintWriter  pw = new PrintWriter( sw );
			m.print( pw, 3, 4 );
			return sw.toString();
		}
	}

	private String getClusterInfo(List<String> dir1, List<String> dir2, Matrix scores) {
		List<Set<String>> clusters = getClusters(dir1, dir2, scores);

		StringBuffer sb = new StringBuffer();

		sb.append("<html>\n\t<body>\n");
		for (Set<String> set : clusters) {
			if (set.size() > 1) {
				sb.append("\t\t<div>\n");
				
				for (String s : set) {
					sb.append("\t\t\t<img src=\""+s+"\" width=\"100\"/>\n");
//					String id = s.replace("/Users/jsh2/youtube/", "").replace("/hqdefault.jpg", "");
//					sb.append("\t\t\t<a href=\"http://www.youtube.com/watch?v="+id+"\">\n");
//					sb.append("\t\t\t\t<img src=\"http://i.ytimg.com/vi/"+id+"/hqdefault.jpg\" width=\"100\"/>\n");
//					sb.append("\t\t\t</a>\n");
				}
				
				sb.append("\t\t</div>");
				sb.append("\t\t<hr/>");
			}
		}
		sb.append("\t</body>\n</html>\n");
		
		return sb.toString();
	}

	private List<Set<String>> getClusters(List<String> dir1, List<String> dir2, Matrix scores) {
		UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

		for (String f : dir1) graph.addVertex(f);
		if (dir1 != dir2) for (String f : dir2) graph.addVertex(f);

		final double[][] matrixData = scores.getArray();
		for (int r=0; r<matrixData.length; r++) {
			for (int c=r; c<matrixData[0].length; c++) {
				String f1 = dir1.get(r);
				String f2 = dir2.get(c);
				if ( matrixData[r][c] != 0 && f1!=f2 ) {
					graph.addEdge(f1, f2);
				}
			}
		}

		ConnectivityInspector<String, DefaultEdge> conn = new ConnectivityInspector<String, DefaultEdge>(graph);
		return conn.connectedSets();
	}

	/**
	 * 	Returns a list of files that exist in the given dir.
	 *	@param dir The directory to start at
	 *	@param subdirs Whether to recurse into subdirs
	 *	@return The list of relative filenames
	 */
	private List<String> getListOfFiles( String dir, boolean subdirs )
	{
		// We'll stick all the files in here.
		List<String> files = new ArrayList<String>(); 

		// First get a list of all files. We don't filter them as we're
		// trying to get all the subdirs.
		File[] f = new File(dir).listFiles();
		for( File file : f )
		{
			// If it's a directory, we recurse
			if( file.isDirectory() )
			{
				files.addAll( 
						getListOfFiles( file.getAbsolutePath(), subdirs ) );
			}
			else
				// If there's no regex or the file matches our regex
				if( regex == null || 
						(regex != null && file.getName().matches( regex ) ) )
				{
					files.add( file.getAbsolutePath() );
				}
		}

		// Convert to array
		return files;
	}

	/**
	 * 	Get a feature comparison class for the given feature and metric.
	 *	@param fv The feature vector
	 *	@param type The feature comparison type
	 *	@return A comparable class
	 */
	@SuppressWarnings("unchecked")
	protected <T extends FeatureVector> FVComparator<T> 
	getComp( T fv, FeatureComparison type ) 
	{
		if (fv instanceof ByteFV) return (FVComparator<T>) ByteFVComparison.valueOf(type.name());
		if (fv instanceof ShortFV) return (FVComparator<T>) ShortFVComparison.valueOf(type.name());
		if (fv instanceof IntFV) return (FVComparator<T>) IntFVComparison.valueOf(type.name());
		if (fv instanceof FloatFV) return (FVComparator<T>) FloatFVComparison.valueOf(type.name());
		if (fv instanceof DoubleFV) return (FVComparator<T>) DoubleFVComparison.valueOf(type.name());
		return null;
	}

	/**
	 * 	Main method.
	 * 
	 * 	Example command line:
	 *  collectcomp -d D:\gfx -r .*\.jpg -f HISTOGRAM -c RGB -m EUCLIDEAN 4 4 4
	 * 
	 *	@param args Command-line arguments
	 */
	public static void main( String[] args )
	{
		// Instantiate the tool and parse the arguments
		CollectionComparisonTool cct = new CollectionComparisonTool();
		CmdLineParser parser = new CmdLineParser( cct );

		try
		{
			parser.parseArgument( args );			
			System.out.println( cct.execute() );
		}
		catch( CmdLineException e )
		{
			System.err.println(e.getMessage());
			System.err.println("Usage: collectiontool [options...]");
			parser.printUsage(System.err);

			if( cct.feature == null ) 
			{
				for( GlobalFeatureType m : GlobalFeatureType.values() ) 
				{
					System.err.println();
					System.err.println(m + " options: ");
					new CmdLineParser(m.getOptions()).printUsage(System.err);
				}
			}
		}
	}
}
