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
/*
 * Created on 14-May-2005
 */
package org.openimaj.image.processing.face.recognition;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.Face;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * 
 * 
 * Added by David Dupplaw from http://uni.johnsto.co.uk/faces/
 * 
 * @author alan
 */
public class EigenFaceGenerator
{
	/** Logs processing progress */
	private Logger logger = Logger.getLogger( EigenFaceGenerator.class );

	/** Stores the average face; useful when probing the database */
	private Matrix averageFace;
	
	/** Stores all the sorted Eigen vectors from the training set */
	private Matrix eigVectors;
	
	/** Stores all the sorted Eigen values from the training set */
	private Matrix eigValues;
	
	/** Has a training set been provided yet? */
	private boolean trained = false;
	
	/** Number of Eigen vectors available */
	private int numEigenVecs = 0;

	private double getMatrixMax( Matrix m )
	{
		double[][] dd = m.getArray();
		double max = Double.MIN_VALUE;
		for( int x = 0; x < dd.length; x++ )
		{
			for( int y = 0; y < dd[x].length; y++ )
			{
				if( dd[x][y] > max )
					max = dd[x][y];
			}
		}
		return max;
	}
	
	/**
	 * 	Returns the average face
	 *	@return the average {@link Face} of the training set
	 */
	public Face getAverageFace()
	{
		double max = getMatrixMax( averageFace ); 
		double[][] dd = averageFace.getArray();
		FImage img = new FImage( dd[0].length, dd.length );
		for( int x = 0; x < dd.length; x++ ) {
			for( int y = 0; y < dd[x].length; y++ ) {
				float v = (float) (dd[x][y]/max);
				img.setPixel(x, y, v);
			}
		}
		
		Face f = new Face(new MBFImage(img, img, img));
		return f;
	}
	
	/**
	 * 
	 * @param faces array of pictures to be used for the training
	 */
	public void processTrainingSet( Face[] faces )
	{
		// TODO : there are errors that can be thrown when no fiels are parsed
		// into here
		// TODO : Check that all the images are the same size

		/**
		 * STEP 1 Read in the images, flatten them out into one row of values,
		 * and stack in a big matrix
		 */
		logger.debug( "Constructing matrix..." );
		double[][] dpix = new double[faces.length][faces[0].getBand(0)
		        .getDoublePixelVector().length];

		for( int i = 0; i < faces.length; i++ )
		{
			// for each picture in the set
			double[] pixels = faces[i].getBand(0).getDoublePixelVector();
			for( int j = 0; j < pixels.length; j++ )
			{
				dpix[i][j] = pixels[j];
			}
		}
		// make matrix of stacked flattened images
		Matrix matrix = new Matrix( dpix );

		/**
		 * STEP 2 Calculate the average face, and then take this away from each
		 * of the image effectivly calculating the difference form the average.
		 */
		logger.debug( "Calculating averages..." );
		
		// compute the average image
		averageFace = new Matrix( 1, matrix.getColumnDimension() );
		
		for( int i = 0; i < matrix.getRowDimension(); i++ )
		{
			averageFace.plusEquals( matrix.getMatrix( i, i, 0, matrix
			        .getColumnDimension() - 1 ) );
		}
		
		// Divide by the number of pixels to get the average
		averageFace.timesEquals( 1.0 / matrix.getRowDimension() );
		
		Matrix bigAvg = new Matrix( matrix.getRowDimension(), matrix
		        .getColumnDimension() );
		for( int i = 0; i < bigAvg.getRowDimension(); i++ )
		{
			bigAvg.setMatrix( i, i, 0, bigAvg.getColumnDimension() - 1,
			        averageFace );
		}
		
		// Compute the diference form the average face for each image
		Matrix A = matrix.minus( bigAvg ).transpose();

		/**
		 * STEP 3 Now compute the the patternwise (nexamp x nexamp) covariance
		 * matrix
		 */
		logger.debug( "Computing covariance matrix..." );
		
		// TODO : for the presentation work out why this is done, and what it's
		// telling us
		Matrix At = A.transpose();
		Matrix L = At.times( A );

		/**
		 * STEP 4 Calculate the eigen values and vectors of this covariance
		 * matrix
		 * 
		 * % Get the eigenvectors (columns of Vectors) and eigenvalues (diag of
		 * Values)
		 */
		logger.debug( "Calculating eigenvectors..." );
		EigenvalueDecomposition eigen = L.eig();
		eigValues = eigen.getD();
		eigVectors = eigen.getV();

		/**
		 * STEP 5 % Sort the vectors/values according to size of eigenvalue
		 */
		logger.debug( "Sorting eigenvectors..." );
		Matrix[] eigDVSorted = sortem( eigValues, eigVectors );
		eigValues = eigDVSorted[0];
		eigVectors = eigDVSorted[1];

		/**
		 * STEP 6 % Convert the eigenvectors of A'*A into eigenvectors of A*A'
		 */

		eigVectors = A.times( eigVectors );

		/**
		 * STEP 7 % Get the eigenvalues out of the diagonal matrix and %
		 * normalize them so the evalues are specifically for cov(A'), not A*A'.
		 */
		logger.debug( "Extracting eigenvalues..." );
		double[] values = diag( eigValues );
		for( int i = 0; i < values.length; i++ )
			values[i] /= A.getColumnDimension() - 1;

		/**
		 * STEP 8 % Normalize Vectors to unit length, kill vectors corr. to tiny
		 * evalues
		 */
		logger.debug( "Normalising eigenvectors..." );
		numEigenVecs = 0;
		for( int i = 0; i < eigVectors.getColumnDimension(); i++ )
		{
			Matrix tmp;
			if( values[i] < 0.0001 )
			{
				tmp = new Matrix( eigVectors.getRowDimension(), 1 );
			}
			else
			{
				tmp = eigVectors.getMatrix( 0,
				        eigVectors.getRowDimension() - 1, i, i ).times(
				        1 / eigVectors.getMatrix( 0,
				                eigVectors.getRowDimension() - 1, i, i )
				                .normF() );
				numEigenVecs++;
			}
			eigVectors.setMatrix( 0, eigVectors.getRowDimension() - 1, i, i,
			        tmp );
			// eigVectors.timesEquals(1 / eigVectors.getMatrix(0,
			// eigVectors.getRowDimension() - 1, i, i).normInf());
		}
		eigVectors = eigVectors.getMatrix( 0, eigVectors.getRowDimension() - 1,
		        0, numEigenVecs - 1 );

		trained = true;

		/*
		 * System.out.println("There are " + numGood +
		 * " eigenVectors\n\nEigenVectorSize");
		 * System.out.println(eigVectors.getRowDimension());
		 * System.out.println(eigVectors.getColumnDimension()); try {
		 * PrintWriter pw = new PrintWriter("c:\\tmp\\test.txt");
		 * eigVectors.print(pw, 8, 4); pw.flush(); pw.close(); } catch
		 * (Exception e) { e.printStackTrace(); }
		 * 
		 * int width = pics[0].img.getWidth(null); BufferedImage biAvg =
		 * imageFromMatrix(bigAvg.getArrayCopy()[0], width);
		 * 
		 * try { saveImage(new File("c:\\tmp\\test.jpg"), biAvg); } catch
		 * (IOException e1) { e1.printStackTrace(); }
		 */
	}

	/**
	 * Returns a number of eigenFace values to be used in a feature space
	 * 
	 * @param pic The face picture
	 * @param number number of eigen feature values.
	 * @return will be of length number or this.getNumEigenVecs whichever is the
	 *         smaller
	 */
	public double[] getEigenFaces( Face pic, int number )
	{
		// Adjust the number to the maximum number of eigen vectors available
		if( number > numEigenVecs ) 
			number = numEigenVecs;

		double[] ret = new double[number];

		double[] pixels = pic.getBand( 0 ).getDoublePixelVector();
		Matrix face = new Matrix( pixels, pixels.length );
		Matrix Vecs = eigVectors.getMatrix( 0,
		        eigVectors.getRowDimension() - 1, 0, number - 1 ).transpose();

		Matrix rslt = Vecs.times( face );

		for( int i = 0; i < number; i++ )
			ret[i] = rslt.get( i, 0 );

		return ret;
	}

	/**
	 * Gets the diagonal of a matrix
	 * 
	 * @param M matrix
	 * @return
	 */
	private double[] diag( Matrix M )
	{
		double[] dvec = new double[M.getColumnDimension()];
		for( int i = 0; i < M.getColumnDimension(); i++ )
			dvec[i] = M.get( i, i );
		return dvec;

	}

	/**
	 * Sorts the Eigenvalues and vectors in decending order
	 * 
	 * @param D eigen Values
	 * @param V eigen Vectors
	 * @return
	 */
	private Matrix[] sortem( Matrix D, Matrix V )
	{
		// dvec = diag(D); // get diagonal components
		double[] dvec = diag( D );

		// NV = zeros(size(V));

		// [dvec,index_dv] = sort(dvec); // sort dvec, maintain index in
		// index_dv

		class di_pair
		{
			double value;
			int index;
		}
		
		di_pair[] dvec_indexed = new di_pair[dvec.length];
		for( int i = 0; i < dvec_indexed.length; i++ )
		{
			dvec_indexed[i] = new di_pair();
			dvec_indexed[i].index = i;
			dvec_indexed[i].value = dvec[i];
		}

		Comparator<di_pair> di_pair_sort = new Comparator<di_pair>()
		{
			@Override
			public int compare( di_pair lt, di_pair rt )
			{
				double dif = (lt.value - rt.value);
				if( dif > 0 ) return -1;
				if( dif < 0 ) return 1;
				else return 0;
			}
		};
		Arrays.sort( dvec_indexed, di_pair_sort );

		// index_dv = flipud(index_dv);
		// for i = 1:size(D,1)
		// ND(i,i) = D(index_dv(i),index_dv(i));
		// NV(:,i) = V(:,index_dv(i));
		// end;

		Matrix D2 = new Matrix( D.getRowDimension(), D.getColumnDimension() );
		Matrix V2 = new Matrix( V.getRowDimension(), V.getColumnDimension() );

		for( int i = 0; i < dvec_indexed.length; i++ )
		{
			D2
			        .set( i, i, D.get( dvec_indexed[i].index,
			                dvec_indexed[i].index ) );
			int height = V.getRowDimension() - 1;
			Matrix tmp = V.getMatrix( dvec_indexed[i].index,
			        dvec_indexed[i].index, 0, height );
			V2.setMatrix( i, i, 0, height, tmp );
		}
		
		// TODO : Not sure why, but this has to be flipped - check this out
		// maybe?
		Matrix V3 = new Matrix( V.getRowDimension(), V.getColumnDimension() );
		for( int i = 0; i < V3.getRowDimension(); i++ )
		{
			for( int j = 0; j < V3.getColumnDimension(); j++ )
			{
				V3.set( i, j, V2.get( V3.getRowDimension() - i - 1, V3
				        .getColumnDimension()
				        - j - 1 ) );
			}
		}

		return new Matrix[] { D2, V3 };
	}

	/**
	 *	Returns whether the set has been trained. 
	 *  @return TRUE if the set has been trained; FALSE otherwise.
	 */
	public boolean isTrained()
	{
		return trained;
	}

	/**
	 * 	Returns the number of Eigenvectors.
	 *  @return the number of Eigenvectors.
	 */
	public int getNumEigenVecs()
	{
		return numEigenVecs;
	}
}
