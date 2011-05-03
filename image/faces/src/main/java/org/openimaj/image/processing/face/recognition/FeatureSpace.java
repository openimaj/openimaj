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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.image.processing.face.Face;


/**
 * 	Added by David Dupplaw from http://uni.johnsto.co.uk/faces/
 * 	@author alan
 */
public class FeatureSpace
{
	
	private Logger logger = Logger.getLogger( FeatureSpace.class );

	public static final DistanceMeasure EUCLIDEAN_DISTANCE = new DistanceMeasure()
	{

		@Override
		public double DistanceBetween( FeatureVector obj1, FeatureVector obj2 )
		{
			int num = obj1.getFeatureVector().length;
			num = (obj2.getFeatureVector().length > num ? obj2
			        .getFeatureVector().length : num);
			double dist = 0;
			for( int i = 0; i < num; i++ )
			{
				dist += ((obj1.getFeatureVector()[i] - obj2.getFeatureVector()[i]) * (obj1
				        .getFeatureVector()[i] - obj2.getFeatureVector()[i]));
			}
			return Math.sqrt( dist );
		}

	};

	private ArrayList<FeatureVector> featureSpace;
	private List<Object> classifications;

	public FeatureSpace()
	{
		featureSpace = new ArrayList<FeatureVector>();
		classifications = new ArrayList<Object>();
	}

	public void insertIntoDatabase( Face face, double[] featureVector )
	{
		if( !classifications.contains( face.getBelongsTo() ) ) 
			classifications.add( face.getBelongsTo() );
		
		int clas = classifications.indexOf( face.getBelongsTo() );
		logger.debug( "Inserting classification "+clas+" ("+face.getBelongsTo()+") into database" );

		FeatureVector obj = new FeatureVector();
		obj.setClassification( clas );
		obj.setFace( face );
		obj.setFeatureVector( featureVector );

		featureSpace.add( obj );
	}

	public Object closestFeature( DistanceMeasure measure, FeatureVector obj )
	{
		if( getFeatureSpaceSize() < 1 ) return null;

		Object ret = classifications.get( featureSpace.get( 0 ).getClassification() );
		
		double dist = measure.DistanceBetween( obj, featureSpace.get( 0 ) );
		
		for( int i = 1; i < featureSpace.size(); i++ )
		{
			double d = measure.DistanceBetween( obj, featureSpace.get( i ) );
			if( d < dist )
			{
				dist = d;
				ret = classifications.get( featureSpace.get( i )
				        .getClassification() );
			}
		}
		
		return ret;
	}

	public Object knn( DistanceMeasure measure, FeatureVector obj, int nn )
	{
		if( getFeatureSpaceSize() < 1 ) return null;
		class di_pair
		{
			double dist;
			FeatureVector obj;
		}
		
		di_pair[] dp = new di_pair[featureSpace.size()];

		for( int i = 0; i < featureSpace.size(); i++ )
		{
			dp[i] = new di_pair();
			dp[i].obj = featureSpace.get( i );
			dp[i].dist = measure.DistanceBetween( obj, featureSpace.get( i ) );
		}

		Comparator<di_pair> diCompare = new Comparator<di_pair>()
		{
			@Override
			public int compare( di_pair a, di_pair b )
			{
				return (int)a.dist - (int)b.dist;
			}

		};

		Arrays.sort( dp, diCompare );

		int[] accm = new int[classifications.size()];
		for( int i = 0; i < classifications.size(); i++ )
			accm[i] = 0;

		int max = 0;
		int ind = 0;

		// find the most common neighbouring classification
		for( int i = 0; i < nn; i++ )
		{
			int c = dp[i].obj.getClassification();
			accm[c]++;
			if( accm[c] > max )
			{
				max = accm[c];
				ind = c;
			}
		}
		return classifications.get( dp[ind].obj.getClassification() );
	}

	class fd_pair
	{
		public Face face;
		public double dist;
	}

	/**
	 * 	Sorts the database by how close it is to the distance measure
	 * 
	 * 	@param measure the distance measure to use (ie euclidean)
	 * 	@param obj feature vector of the of the probe into the database
	 * 	@return a sorted array list of faces (closest first)
	 */
	public fd_pair[] orderByDistance( DistanceMeasure measure, FeatureVector obj )
	{
		ArrayList<fd_pair> orderedList = new ArrayList<fd_pair>();
		if( getFeatureSpaceSize() < 1 ) return null;

		class di_pair
		{
			double dist;
			FeatureVector obj;
		}
		
		di_pair[] dp = new di_pair[featureSpace.size()];

		for( int i = 0; i < featureSpace.size(); i++ )
		{
			dp[i] = new di_pair();
			dp[i].obj = featureSpace.get( i );
			dp[i].dist = measure.DistanceBetween( obj, featureSpace.get( i ) );
		}

		Comparator<di_pair> diCompare = new Comparator<di_pair>()
		{
			@Override
			public int compare( di_pair a, di_pair b )
			{
				return (int)a.dist - (int)b.dist;
			}

		};

		Arrays.sort( dp, diCompare );

		for( di_pair dfp : dp )
		{
			fd_pair fd = new fd_pair();
			fd.face = dfp.obj.getFace();
			fd.dist = dfp.dist;
			orderedList.add( fd );
		}

		return orderedList.toArray( new fd_pair[0] );
	}

	public double[][] get3dFeatureSpace()
	{
		double[][] features = new double[classifications.size() * 18 + 18][3];
		for( int i = 0; i < classifications.size(); i++ )
		{

			ArrayList<FeatureVector> rightClass = new ArrayList<FeatureVector>();
			for( int j = 0; j < featureSpace.size(); j++ )
			{
				if( featureSpace.get( j ).getClassification() == i ) rightClass
				        .add( featureSpace.get( j ) );
			}

			for( int j = 0; j < 18; j++ )
			{
				int pos = i * 18 + j;
				int tmp = j % rightClass.size();
				features[pos][0] = rightClass.get( tmp ).getFeatureVector()[0];
				features[pos][1] = rightClass.get( tmp ).getFeatureVector()[1];
				features[pos][2] = rightClass.get( tmp ).getFeatureVector()[2];
			}
		}

		// norlamise these values from 0-100
		double max0 = features[0][0], max1 = features[0][1], max2 = features[0][2];
		double min0 = features[0][0], min1 = features[0][1], min2 = features[0][2];
		for( int i = 1; i < features.length - 18; i++ )
		{ // get the max and min on each axis
			if( features[i][0] > max0 ) max0 = features[i][0];
			if( features[i][0] < min0 ) min0 = features[i][0];

			if( features[i][1] > max1 ) max1 = features[i][1];
			if( features[i][1] < min1 ) min1 = features[i][1];

			if( features[i][2] > max2 ) max2 = features[i][2];
			if( features[i][2] < min2 ) min2 = features[i][2];
		}

		double mult0 = (max0 - min0) / 100;
		double mult1 = (max1 - min1) / 100;
		double mult2 = (max2 - min2) / 100;

		for( int i = 0; i < features.length - 18; i++ )
		{ // perform the normalisation
			features[i][0] -= min0;
			features[i][0] /= mult0;

			features[i][1] -= min1;
			features[i][1] /= mult1;

			features[i][2] -= min2;
			features[i][2] /= mult2;

			// System.out.println("features[" + i + "] = [" +features[i][0] +
			// ", " + features[i][1] + ", " + features[i][2] + "]");
		}

		return features;
	}

	public double[][] get3dFeatureSpace( FeatureVector probe )
	{
		if( probe == null ) return get3dFeatureSpace();
		double[][] features = new double[classifications.size() * 18 + 36][3];
		for( int i = 0; i < classifications.size(); i++ )
		{

			ArrayList<FeatureVector> rightClass = new ArrayList<FeatureVector>();
			for( int j = 0; j < featureSpace.size(); j++ )
			{
				if( featureSpace.get( j ).getClassification() == i ) rightClass
				        .add( featureSpace.get( j ) );
			}

			for( int j = 0; j < 18; j++ )
			{
				int pos = i * 18 + j;
				int tmp = j % rightClass.size();
				features[pos][0] = rightClass.get( tmp ).getFeatureVector()[0];
				features[pos][1] = rightClass.get( tmp ).getFeatureVector()[1];
				features[pos][2] = rightClass.get( tmp ).getFeatureVector()[2];
			}
		}

		for( int j = 0; j < 18; j++ )
		{
			int pos = featureSpace.size() + j;
			features[pos][0] = probe.getFeatureVector()[0];
			features[pos][1] = probe.getFeatureVector()[1];
			features[pos][2] = probe.getFeatureVector()[2];
		}

		// norlamise these values from 0-100
		double max0 = features[0][0], max1 = features[0][1], max2 = features[0][2];
		double min0 = features[0][0], min1 = features[0][1], min2 = features[0][2];
		for( int i = 1; i < features.length - 18; i++ )
		{ // get the max and min on each axis
			if( features[i][0] > max0 ) max0 = features[i][0];
			if( features[i][0] < min0 ) min0 = features[i][0];

			if( features[i][1] > max1 ) max1 = features[i][1];
			if( features[i][1] < min1 ) min1 = features[i][1];

			if( features[i][2] > max2 ) max2 = features[i][2];
			if( features[i][2] < min2 ) min2 = features[i][2];
		}

		double mult0 = (max0 - min0) / 100;
		double mult1 = (max1 - min1) / 100;
		double mult2 = (max2 - min2) / 100;

		for( int i = 0; i < features.length - 18; i++ )
		{ // perform the normalisation
			features[i][0] -= min0;
			features[i][0] /= mult0;

			features[i][1] -= min1;
			features[i][1] /= mult1;

			features[i][2] -= min2;
			features[i][2] /= mult2;

			// System.out.println("features[" + i + "] = [" +features[i][0] +
			// ", " + features[i][1] + ", " + features[i][2] + "]");
		}

		return features;
	}

	public int getFeatureSpaceSize()
	{
		return featureSpace.size();
	}
}
