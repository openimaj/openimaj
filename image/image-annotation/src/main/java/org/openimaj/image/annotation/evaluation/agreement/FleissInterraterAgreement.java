/**
 * 
 */
package org.openimaj.image.annotation.evaluation.agreement;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.ml.annotation.ScoredAnnotation;

/**
 *	Calculates Fleiss inter-rater agreement - a version of Cohen's kappa
 *	that works with multiple raters. Note that it is technially not a kappa
 *	as it does not reduce to Cohen's kappa when used with only 2 raters.	
 *
 *	@see "http://en.wikipedia.org/wiki/Fleiss'_kappa"
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 16 Aug 2013
 */
public class FleissInterraterAgreement
{
	/**
	 * 	Calculate Fleiss interrater agreement
	 * 
	 * 	@see "http://en.wikipedia.org/wiki/Fleiss'_kappa"
	 *	@param data The rater's annotations
	 *	@return The Fleiss Kappa
	 */
	public static <K,A> double calculate( 
			List<Map<K,ScoredAnnotation<A>>> data )
	{
		// Map from subject to category count (the table)
		Map<K,TObjectIntHashMap<A>> table = new HashMap<K,TObjectIntHashMap<A>>();
		
		// Total assignments to each category, Pj*Nn
		TObjectDoubleHashMap<A> cats = new TObjectDoubleHashMap<A>();
		
		// Total ratings made
		int totalCount = 0;
		
		// Generate the counts table
		// Loop through all the raters
		for( Map<K, ScoredAnnotation<A>> ratings : data )
		{
			// Loop through the subjects that this rater rated
			for( K subject : ratings.keySet() )
			{
				ScoredAnnotation<A> annotation = ratings.get( subject );
				if( annotation != null )
				{
					TObjectIntHashMap<A> count = table.get( subject );
					if( count == null )
					{
						count = new TObjectIntHashMap<A>();
						table.put( subject, count );
					}

					count.putIfAbsent( annotation.annotation, 0 );
					count.increment( annotation.annotation );
					
					cats.putIfAbsent( annotation.annotation, 0 );
					cats.increment( annotation.annotation );
					
					totalCount++;
				}
			}
		}
		
		// Normalise Pj by N*n (totalCount) and square (part of working out PeBar)
		TDoubleArrayList Pj = new TDoubleArrayList();
		for( A x : cats.keySet() )
			Pj.add( (cats.get(x) / (double)totalCount) * 
					(cats.get(x) / (double)totalCount) );
		
		int n = data.size();

		TDoubleArrayList Pis = new TDoubleArrayList();
		for( K subject : table.keySet() )
		{
			double Pi = 0;
			for( A cat : table.get( subject ).keySet() )
			{
				int nij = table.get(subject).get(cat); 
				Pi += nij*(nij-1);
			}
			Pi /= (n*(n-1));
			Pis.add( Pi );
		}
		
		double Pbar = Pis.sum() / Pis.size();
		double PeBar = Pj.sum(); 
		
		double kappa = (Pbar - PeBar) / (1 - PeBar); 		
		return kappa;
	}
}