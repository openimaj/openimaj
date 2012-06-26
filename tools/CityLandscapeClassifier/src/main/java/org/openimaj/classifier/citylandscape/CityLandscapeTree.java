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
package org.openimaj.classifier.citylandscape;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.analysis.algorithm.EdgeDirectionCoherenceVector;
import org.openimaj.knn.DoubleNearestNeighbours;
import org.openimaj.knn.approximate.DoubleNearestNeighboursKDTree;

/**
 * 	Originally written by Ajay Mehta for his third year project. Reworked for
 * 	inclusion into OpenIMAJ by David Dupplaw.
 * 	<p>
 * 	This class provides classification of objects. The idea in the classifier
 * 	is to classfy images between landscape images and cityscape images. Could
 * 	also be used as a natural vs. non-natural classifier as the technique used
 * 	is based on edge-direction coherence vectors (i.e. looking for strong lines).
 * 	The training set (src/main/resources/CityLS10000.2.no-decimal) 
 * 	provides 5000 examples of edge direction coherence vectors for both city
 * 	scape and landscape (the original images were crawled from Flickr based on
 * 	appropriate tags). Internally the classifier uses the nearest neighbour
 * 	to determine the class of the query.
 * 	<p>
 * 	To use, do something like this:
 * 	<pre>
 * 	{@code
 * 		CityLandscapeTree clt = new CityLandscapeTree( "city", "landscape",
 * 			getClass().getResourceAsStream( "/CityLS10000.2.no-decimal" ), 10000 );
 * 		String clazz = clt.classifyImage( ImageUtilities.readF( 
 * 			new File("myImage.jpg") ), 1 ); 
 * 	}
 * 	</pre>
 * 	<p>
 * 	A main method is supplied for the tool which will take an image filename
 * 	and classify the image as city or landscape.
 * 
 * 	@author Ajay Mehta (am24g08@ecs.soton.ac.uk)
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@created 2011
 * 	
 */
public class CityLandscapeTree
{
	/** Category 1 */
	private String cat1 = null;

	/** Category 2 */
	private String cat2 = null;

	/** The number of instances in the training set */
	private int trainingSetSize = 0;

	/** The nearest neighbour classifier */
	private DoubleNearestNeighbours dnn = null;
	
	/** The size of the vector generated for each image */
	private static int VECTOR_SIZE = 144;

	/** */
	private final static int NTREES = 768;

	/** */
	private final static int NCHECKS = 8;

	/**
	 * 	Default constructor that takes the two categories
	 * 	to classifier between.
	 * 
	 *	@param cat1 The first category
	 *	@param cat2 The second category
	 */
	public CityLandscapeTree( String cat1, String cat2 )
	{
		this.cat1 = cat1;
		this.cat2 = cat2;
	}
	
	/**
	 * 	Constructor that takes the two categories to classify between,
	 * 	an input stream that points to a training set file and the size
	 * 	of that training set. 
	 * 
	 *	@param cat1 The first classification category
	 *	@param cat2 The second classification category
	 *	@param trainingSet The training set
	 *	@param trainingSetSize The size of the training set
	 */
	public CityLandscapeTree( String cat1, String cat2,
			InputStream trainingSet, int trainingSetSize )
	{
		this( cat1, cat2 );
		try
		{
			// Load the given training set
			double[][] train = loadTrainingSet( trainingSet, trainingSetSize );
			
			// Construct DoubleNearestNeighbours Object
			dnn = new DoubleNearestNeighboursKDTree( train, NTREES, NCHECKS );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * 	From the given image filename, returns the edge direction coherence
	 * 	vector as a double array.
	 * 
	 *	@param imageName The image to process
	 *	@return A 2-dimensional vector with only one 
	 */
	private static double[][] getImageVectorAsArray( FImage crgbimage )
	{
		double[][] toReturn = new double[1][VECTOR_SIZE];
		
		// Calculate the Edge direction coherence on the image.
		EdgeDirectionCoherenceVector edcv = new EdgeDirectionCoherenceVector();
		edcv.setNumberOfBins( VECTOR_SIZE/2 );
		
		// Process the image
		crgbimage.analyseWith( edcv );
		
		// Get the histogram
		double[] d = edcv.getLastHistogram().asDoubleFV().asDoubleVector();
		
		// Normalise the vector by the total number of edge pixels
		double[] edgeCounter = new double[1];
		for( int j = 0; j < VECTOR_SIZE; j++ )
		{
			toReturn[0][j]  = d[j];
			edgeCounter[0] += d[j];
		}
		
		// Normalise the vector
		CityLandscapeUtilities.normaliseVector( toReturn, edgeCounter );

		return toReturn;
	}

	/**
	 * 	Classifies the given image.
	 * 
	 *	@param image The image to classify
	 *	@param k The number of nearest neighbours to interrogate
	 *	@return The expected category
	 */
	public String classifyImage( FImage image, int k )
	{
		System.out.println( "Classifying image... " );
		
		// Get the vector for the query image.
		double[][] query = getImageVectorAsArray( image );
		
		// Indexes and distances of nearest neighbours for the one query image
		int[][] indexes = new int[1][k];
		double[][] distances = new double[1][k];
		
		// KNN search for the query image
		dnn.searchKNN( query, k, indexes, distances );
		
		// Counters for the two categories (City vs Landscape)
		double cat1Counter = 0, cat2Counter = 0;

		// Loop through all the results
		for( int i = 0; i < distances[0].length; i++ )
		{
			// 
			if( indexes[0][i] < trainingSetSize / 2 )
					cat1Counter += 1 / distances[0][i];
			else	cat2Counter += 1 / distances[0][i];
		}

		if( cat1Counter > cat2Counter )
				return cat1;
		else if( cat2Counter > cat1Counter )
				return cat2;
		else	return "?";
	}

	/**
	 * 	Loads a training set of a given size from an input stream
	 * 
	 *	@param is The input stream to read the training set from
	 *	@param vecSize The size of the training set
	 *	@return A 2-dimensional double array of the training set
	 *	@throws IOException if the input stream could not be fully read
	 */
	public double[][] loadTrainingSet( InputStream is, int vecSize ) 
		throws IOException
	{
		this.trainingSetSize = vecSize;
		double[][] trainingVector = new double[trainingSetSize][VECTOR_SIZE];
		double[] totalEdges = new double[trainingSetSize];
		
		System.out.println( "Loading training data... " );
		
		// Read in each line from the training set data. The training set data
		// is set out as one-line per training image. The data in the line is
		// comma-separated double values, where the values are the histogram
		// bin values from the edge direction coherence vector. The number
		// of bins should be this.vectorSize/2 each for coherence and incoherent
		// histograms. A final value on each line contains the total number
		// of detected coherent edges in the image.
		BufferedReader br = new BufferedReader( new InputStreamReader( is ) );		
		int counter = 0;
		String line = null;
		while( (line = br.readLine()) != null )
		{
			String[] array = line.split( "," );

			for( int i = 0; i < VECTOR_SIZE/2; i++ )
				trainingVector[counter][i] = Double.parseDouble( array[i] );
			
			totalEdges[counter++] = Double.parseDouble( 
					array[array.length - 1] );
		}

		// Normalise the vector by the total number of edges.
		System.out.println( "Normalising training data..." );
		CityLandscapeUtilities.normaliseVector( trainingVector, totalEdges );
		
		return trainingVector;
	}
	
	/**
	 * 	Given an image filename, it will classify it.
	 *  @param args
	 */
	public static void main( String[] args )
    {
	    if( args.length < 1 )
	    {
	    	System.err.println( "Please supply an image filename." );
	    	System.exit(1);
	    }
	    
	    try
        {
	        CityLandscapeTree clt = new CityLandscapeTree( "City", "Landscape",
	        		CityLandscapeTree.class.getResourceAsStream( "/CityLS10000.2.no-decimal" ), 10000 );
	        String clazz = clt.classifyImage( ImageUtilities.readF( new File(args[0]) ), 1 );
	        System.out.println( "Classified as: "+clazz );
        }
        catch( IOException e )
        {
	        e.printStackTrace();
        }
    }
}
