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
package org.openimaj.tools.ocr;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.knn.DoubleNearestNeighbours;
import org.openimaj.knn.approximate.DoubleNearestNeighboursKDTree;
import org.openimaj.tools.ocr.FontSimulator.FontSimListener;
import org.openimaj.util.pair.Pair;

/**
 *	A class and tester for testing KNN classification of characters 
 *	which are built randomly from the {@link FontSimulator}.
 *	<p> 
 *	In this base implementation, the pixels of the character images are 
 *	used as the vector for classification which will achieve somewhere in the
 *	region of 60% correct classification for the characters 0-9 and somewhat
 *	worse for larger character ranges. 
 *	<p>
 *	Override the {@link #getImageVector(FImage)} method to try different 
 *	features. This class provides the training, classification and testing.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 19 Aug 2011
 *	
 */
public class KNNCharacterClassifier
{
	/**
	 *	This class is the {@link FontSimListener} implementation that receives
	 *	each character as an image in some randomised font. When it receives
	 *	an image, it creates the feature vector by calling
	 *	{@link KNNCharacterClassifier#getImageVector(FImage)} and stores that
	 *	into a double array that can be used as input to the nearest neighbour
	 *	classifier for training. That array can be retrieved using 
	 *	{@link #getVector()}. 
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 19 Aug 2011
	 *	
	 */
	protected class ImageTrainer implements FontSimListener<FImage>
	{
		/** The output vector that can be used as input to the NN classifier */
		private double[][] vector = null;
		
		/** The current index being processed */
		private int index = 0;

		/**
		 * 	Constructor that takes the size of the output vector (that is the
		 * 	total number of training instances).
		 * 
		 *	@param n The number of training instances
		 */
		public ImageTrainer( int n )
		{
			vector = new double[n][];
		}

		/**
		 * 	Implementation of the {@link FontSimListener} interface.
		 *	@param img The character image.
		 */
		@Override
		public void imageCreated( FImage img )
		{
			vector[index++] = getImageVector(img);
			if( index%25 == 0 )
				System.out.print("..."+index );
		}
		
		/**
		 * 	Retrieve the set of training data as a double array.
		 *	@return the training data
		 */
		public double[][] getVector()
		{
			return vector;
		}
	}
	
	private int NTREES = 768;
	private int NCHECKS = 8;
	
	/** The nearest neighbour classifier */
	private DoubleNearestNeighbours nn = null;
	
	/** What size to resize the images to for the feature vector */
	private int resize = 32;
	
	/** Number of examples per class to generate */
	private int nExamplesPerClass = 100;
	
	/** Start character in the range */
	private int startChar = '0';
	
	/** End character in the range */
	private int endChar = '9';
	
	/**
	 * 	Default constructor
	 */
	public KNNCharacterClassifier()
	{
	}
	
	/**
	 * 	Get the feature vector for a single image. Can be overridden to 	
	 * 	try different feature vectors for classification.	
	 * 
	 *	@param img The character image.
	 *	@return The feature vector for this image.
	 */
	public double[] getImageVector( FImage img )
	{
		// Resize the image (stretch) to a standard shape
		FImage ff = ResizeProcessor.resample( img, resize, resize );
		return ff.getDoublePixelVector();		
	}
	
	/**
	 * 	Train the classifier by generating a bunch of random training examples
	 * 	of characters in various fonts (using the {@link FontSimulator}) 
	 * 	and using the features extracted from those images, train a nearest
	 * 	neighbour classifier.
	 */
	public void train()
	{
		// Create the FontSimListener that receives images from the FontSimulator
		ImageTrainer it = new ImageTrainer(
				(endChar-startChar)*nExamplesPerClass);
		
		System.out.println( "Created vector of length "+
				((endChar-startChar)*nExamplesPerClass) );
		System.out.println( "Creating character data" );
		
		// For each of the characters produce nExamplesPerCase training examples
		for( int i = startChar; i < endChar; i++ )
		{
			FontSimulator<Float, FImage> fs = 
				new FontSimulator<Float,FImage>( 
						Character.toString( (char)i ) );
			fs.setFontPointSize( 48 );
			fs.makeRuns( nExamplesPerClass, it, new FImage(1,1) );
		}

		// Train the classifier
		System.out.println( "\nCreating KDTree...");
		nn = new DoubleNearestNeighboursKDTree( it.getVector(), NTREES, NCHECKS );
	}
	
	/**
	 * 	Classify the given image with the nearest neighbour classifier.
	 * 
	 *	@param img the character image to classify
	 *	@return The classified character.
	 */
	public char classify( FImage img )
	{
		// Create the input vector
		double[][] v = new double[1][];
		v[0] = getImageVector( img );
		
		// Setup the output variables
		int k = 1;
		int[][] indices  = new int[1][k];
		double[][] dists = new double[1][k];
		
		// Do the search
		nn.searchKNN( v, k, indices, dists );
		
		// Work out what character the best index represents
		System.out.println( "Best index: "+indices[0][0] );
		char c = (char)(48+indices[0][0]/nExamplesPerClass);
		System.out.println( "So, I think it's "+c );
		
		return c;
	}
	
	/**
	 * 	Run a bunch of tests to determine how good the classifier is. It does
	 * 	this by creating a load of random examples of random characters using
	 * 	the {@link FontSimulator} and classifies them.
	 * 
	 *	@param nTestRuns The number of tests to run
	 */
	public void test( int nTestRuns )
	{
		// First in the pair is the incorrectly classified count,
		// the second is the correctly classified count.
		final Pair<Integer> results = new Pair<Integer>(0,0);
		
		// Loop through all the tests
		for( int j = 0; j < nTestRuns; j++ )
		{
			// Choose a randome character that's in our training set
			final int i = startChar + (int)(Math.random()*(endChar-startChar));
			
			// Create a single random image of the chosen character
			FontSimulator<Float, FImage> fs = new FontSimulator<Float,FImage>( 
						Character.toString( (char)i ) );
			fs.setFontPointSize( 48 );
			FImage x = fs.generate( new FImage(1,1) );
			
			// Classify that character
			System.out.println( "Classifying character "+(char)i );
			char c = classify( x );
			
			// Update the test count
			if( c != i )
					results.setFirstObject( results.firstObject().intValue()+1 );
			else	results.setSecondObject( results.secondObject().intValue()+1 );
		}
		
		// Show the test results
		int nWrong = results.firstObject();
		int nCorrect = results.secondObject();
		
		System.out.println( "===========================================");
		System.out.println( "           T E S T   R E S U L T S         ");
		System.out.println( "===========================================");
		System.out.println( "Number of runs: "+nTestRuns );
		System.out.println( "Correct: "+nCorrect+" ("+(100*nCorrect/nTestRuns)+"%)" );
		System.out.println( "Wrong:   "+nWrong+" ("+(100*nWrong/nTestRuns)+"%)" );
		System.out.println( "===========================================");
		
	}
	
	/**
	 * 	The main does a training run then a test run.
	 *  
	 *	@param args
	 */
	public static void main( String[] args )
	{
		final KNNCharacterClassifier ocr = new KNNCharacterClassifier();
		ocr.train();
		ocr.test( 100 );
	}
}
