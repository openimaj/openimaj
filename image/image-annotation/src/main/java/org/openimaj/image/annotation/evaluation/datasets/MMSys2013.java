/**
 *
 */
package org.openimaj.image.annotation.evaluation.datasets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.annotations.DatasetDescription;
import org.openimaj.image.annotation.evaluation.agreement.CohensKappaInterraterAgreement;
import org.openimaj.image.annotation.evaluation.agreement.MajorityVoting;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.web.flickr.FlickrImage;

/**
 *	A wrapper dataset for the MMSys2013 Fashion-Focussed Creative Commons
 *	social dataset (Loni, et.al).
 *
 *	TODO: Need to add the citation here.
 *	From http://dl.acm.org/citation.cfm?id=2483984
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 12 Aug 2013
 *	@version $Author$, $Revision$, $Date$
 */
@DatasetDescription(
	name = "Fashion-Focused Creative Commons Social Dataset",
	description = "a fashion-focused Creative Commons dataset, which is "
			+ "designed to contain a mix of general images as well as a large "
			+ "component of images that are focused on fashion (i.e., relevant "
			+ "to particular clothing items or fashion accessories)",
	creator = "Babak Loni, Maria Menendez, Mihai Georgescu, Luca Galli, "
			+ "Claudio Massari, Ismail Sengor Altingovde, Davide Martinenghi, "
			+ "Mark Melenhorst, Raynor Vliegendhart, Martha Larson",
	downloadUrls={
		"http://skuld.cs.umass.edu/traces/mmsys/2013/fashion/Fashion Dataset.zip"}
)
public class MMSys2013
{
	/**
	 * 	Allowable types of answer for each question.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 12 Aug 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static enum QuestionResponse
	{
		/** No */
		NO,
		/** Yes */
		YES,
		/** Not sure */
		NOT_SURE,
		/** Question was unanswered */
		UNANSWERED;
	}

	/**
	 *	A response to a HIT
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 12 Aug 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class Response
	{
		/** Whether the image contains a depicition of the category subject */
		public QuestionResponse containsCategoryDepiction;

		/** Whether the image is in the correct category */
		public QuestionResponse isInCorrectCategory;

		/** How familiar is the responder with the category */
		public int familiarityWithCategory;

		/**
		 * 	Constructor
		 *	@param r1 contains category depiction
		 *	@param r2 is in correct category
		 *	@param familiarity familiarity with subject
		 */
		public Response( final QuestionResponse r1, final QuestionResponse r2, final int familiarity )
		{
			this.containsCategoryDepiction = r1;
			this.isInCorrectCategory = r2;
			this.familiarityWithCategory = familiarity;
		}

		@Override
		public String toString()
		{
			return "{"+this.containsCategoryDepiction+","+
					this.isInCorrectCategory+","+this.familiarityWithCategory+"}";
		}
	}

	/**
	 * 	A record in the Fashion 10,000 dataset.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 12 Aug 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	protected static class Record
	{
		/** The Flickr Photo */
		public FlickrImage image;

		/** The category in which the image was found */
		public String category;

		/** A set of responses for this image */
		public Response[] annotations;

		@Override
		public String toString()
		{
			return this.image.getId()+":"+this.category+"["+
					Arrays.toString(this.annotations)+"]";
		}
	}

	protected String baseLocation =
			"/data/degas/mediaeval/mediaeval-crowdsourcing/MMSys2013/";

	protected String expertDataFile =
			"Annotations/Annotation_PerImage_Trusted.csv";

	protected String nonExpertDataFile =
			"Annotations/Annotation_PerImage_NonExperts.csv";

	/**
	 *	@return The grouped dataset
	 */
	public GroupedDataset<String,GroupedDataset<String,
		ListDataset<Response>,Response>,Response> getNonExpertData()
	{
		return this.parseMetadata( new File( this.baseLocation, this.nonExpertDataFile ) );
	}

	/**
	 *	@return The grouped dataset
	 */
	public GroupedDataset<String, GroupedDataset<String,
		ListDataset<Response>, Response>, Response> getExpertData()
	{
		return this.parseMetadata( new File( this.baseLocation, this.expertDataFile ) );
	}

	/**
	 *	@param metadataFile
	 *	@return A grouped dataset
	 */
	public GroupedDataset<String,GroupedDataset<String,
		ListDataset<Response>,Response>,Response> parseMetadata(
			final File metadataFile )
	{
		final GroupedDataset<String,
			GroupedDataset<String, ListDataset<Response>, Response>,Response>
		results = new MapBackedDataset<String, GroupedDataset<
			String,ListDataset<Response>,Response>, MMSys2013.Response>();

		BufferedReader br = null;
		try
		{
			br = new BufferedReader( new FileReader( metadataFile ) );
			String line;
			boolean firstLine = true;
			int count = 1;
			while( (line = br.readLine()) != null )
			{
				if( !firstLine )
				{
					try
					{
						final String[] parts = line.split( ",", -1 );

						final Response[] r = new Response[3];
						r[0] = new Response( this.parseQR(parts[3]),
								this.parseQR(parts[6]), this.parseF(parts[9]) );
						r[1] = new Response( this.parseQR(parts[4]),
								this.parseQR(parts[7]), this.parseF(parts[10]) );
						r[2] = new Response( this.parseQR(parts[5]),
								this.parseQR(parts[8]), parts.length>11?
									this.parseF(parts[11]):-1 );

						GroupedDataset<String, ListDataset<Response>, Response>
							gds = results.get( parts[2] );

						// Check whether we already have a dataset for
						// the image in this category
						if( gds == null )
						{
							// Create a new dataset for images in this category
							gds = new MapBackedDataset<String,
									ListDataset<Response>, Response>();
							results.put( parts[2], gds );
						}

						// See if we have any responses for this image already
						ListDataset<Response> ids = gds.get( parts[1] );

						// If not, create the dataset for this image
						if( ids == null )
						{
							ids = new ListBackedDataset<Response>();
							gds.put( parts[1], ids );
						}

						// Add the each response for this image
						for( final Response rr : r )
							ids.add( rr );
					}
					catch( final Exception e )
					{
						System.err.println( "Error on line "+count );
						e.printStackTrace();
					}
				}
				firstLine = false;
				count++;
			}
			br.close();
		}
		catch( final FileNotFoundException e )
		{
			e.printStackTrace();
		}
		catch( final IOException e )
		{
			e.printStackTrace();
		}
		finally
		{
			if( br != null ) try
			{
				br.close();
			}
			catch( final IOException e )
			{
				e.printStackTrace();
			}
		}

		return results;
	}

	/**
	 * 	Given a string returns a question response.
	 *	@param qr The string
	 *	@return A {@link QuestionResponse}
	 */
	protected QuestionResponse parseQR( final String qr )
	{
		if( qr.toLowerCase().equals( "yes" ) )
			return QuestionResponse.YES;
		if( qr.toLowerCase().equals( "no" ) )
			return QuestionResponse.NO;
		if( qr.toLowerCase().equals( "notsure" ) )
			return QuestionResponse.NOT_SURE;
		return QuestionResponse.UNANSWERED;
	}

	protected int parseF( final String f )
	{
		try
		{
			return Integer.parseInt( f );
		}
		catch( final NumberFormatException e )
		{
			return -1;
		}
	}

	/**
	 * 	For a given {@link GroupedDataset} that represents the results
	 * 	from a single category, returns a list of scored annotations for
	 * 	each group, for question 1 (contains depication of category).
	 *
	 *	@param data The data
	 *	@return a list of {@link ScoredAnnotation} linked to image URL
	 */
	public static Map<String,List<ScoredAnnotation<QuestionResponse>>>
				getAnnotationsQ1(
					final GroupedDataset<String,ListDataset<Response>,Response> data )
	{
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> r =
				new HashMap<String, List<ScoredAnnotation<QuestionResponse>>>();

		// Loop through the images in this dataset
		for( final String imgUrl : data.getGroups() )
		{
			final ListDataset<Response> l = data.get( imgUrl );

			final List<ScoredAnnotation<QuestionResponse>> l2 =
					new ArrayList<ScoredAnnotation<QuestionResponse>>();
			r.put( imgUrl, l2 );

			// Loop through the responses for this image
			for( final Response rr : l )
				l2.add( new ScoredAnnotation<QuestionResponse>(
					rr.containsCategoryDepiction, rr.familiarityWithCategory ) );
		}

		return r ;
	}

	/**
	 * 	For a given {@link GroupedDataset} that represents the results
	 * 	from a single category, returns a list of scored annotations for
	 * 	each group, for question 2 (is in category).
	 *
	 *	@param data The group name to retrieve
	 *	@return a list of {@link ScoredAnnotation} linked to image URL
	 */
	public static Map<String,List<ScoredAnnotation<QuestionResponse>>>
				getAnnotationsQ2(
					final GroupedDataset<String,ListDataset<Response>,Response> data )
	{
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> r =
				new HashMap<String, List<ScoredAnnotation<QuestionResponse>>>();

		// Loop through the images in this dataset
		for( final String imgUrl : data.getGroups() )
		{
			final ListDataset<Response> l = data.get( imgUrl );

			final List<ScoredAnnotation<QuestionResponse>> l2 =
					new ArrayList<ScoredAnnotation<QuestionResponse>>();
			r.put( imgUrl, l2 );

			// Loop through the responses for this image
			for( final Response rr : l )
				l2.add( new ScoredAnnotation<QuestionResponse>(
					rr.isInCorrectCategory, rr.familiarityWithCategory ) );
		}

		return r ;
	}

	/**
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		// Expert annotations for Q1 and Q2
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> q1r1 =
				MMSys2013.getAnnotationsQ1( new MMSys2013().getExpertData().get( "Cowboy hat" ) );
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> q2r1 =
				MMSys2013.getAnnotationsQ2( new MMSys2013().getExpertData().get( "Cowboy hat" ) );

		// Non expert annotations for Q1 and Q2
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> q1r2 =
				MMSys2013.getAnnotationsQ1( new MMSys2013().getNonExpertData().get( "Cowboy hat" ) );
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> q2r2 =
				MMSys2013.getAnnotationsQ2( new MMSys2013().getNonExpertData().get( "Cowboy hat" ) );


		final Map<String, ScoredAnnotation<QuestionResponse>> q1r1mv =
				MajorityVoting.calculateBasicMajorityVote( q1r1 );
		final Map<String, ScoredAnnotation<QuestionResponse>> q2r1mv =
				MajorityVoting.calculateBasicMajorityVote( q2r1 );
		final Map<String, ScoredAnnotation<QuestionResponse>> q1r2mv =
				MajorityVoting.calculateBasicMajorityVote( q1r2 );
		final Map<String, ScoredAnnotation<QuestionResponse>> q2r2mv =
				MajorityVoting.calculateBasicMajorityVote( q2r2 );

		System.out.println( "Question 1 agreement: "+CohensKappaInterraterAgreement.calculate( q1r1mv, q1r2mv ) );
		System.out.println( "Question 2 agreement: "+CohensKappaInterraterAgreement.calculate( q2r1mv, q2r2mv ) );
	}
}
