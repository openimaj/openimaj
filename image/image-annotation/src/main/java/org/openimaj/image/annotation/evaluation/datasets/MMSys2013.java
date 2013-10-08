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
package org.openimaj.image.annotation.evaluation.datasets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.annotations.DatasetDescription;
import org.openimaj.experiment.evaluation.agreement.CohensKappaInterraterAgreement;
import org.openimaj.experiment.evaluation.agreement.MajorityVoting;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.iterator.TextLineIterable;
import org.openimaj.util.pair.ObjectFloatPair;
import org.openimaj.web.flickr.FlickrImage;

/**
 * A wrapper dataset for the MMSys2013 Fashion-Focussed Creative Commons social
 * dataset (Loni, et.al).
 * 
 * TODO: Need to add the citation here. From
 * http://dl.acm.org/citation.cfm?id=2483984
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 12 Aug 2013
 * @version $Author$, $Revision$, $Date$
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Loni, Babak", "Menendez, Maria", "Georgescu, Mihai", "Galli, Luca", "Massari, Claudio", "Altingovde, Ismail Sengor", "Martinenghi, Davide", "Melenhorst, Mark", "Vliegendhart, Raynor", "Larson, Martha" },
		title = "Fashion-focused creative commons social dataset",
		year = "2013",
		booktitle = "Proceedings of the 4th ACM Multimedia Systems Conference",
		pages = { "72", "", "77" },
		url = "http://doi.acm.org/10.1145/2483977.2483984",
		publisher = "ACM",
		series = "MMSys '13",
		customData = {
				"isbn", "978-1-4503-1894-5",
				"location", "Oslo, Norway",
				"numpages", "6",
				"doi", "10.1145/2483977.2483984",
				"acmid", "2483984",
				"address", "New York, NY, USA",
				"keywords", "crowdsourcing, dataset, fashion, multimedia content analysis"
		})
@DatasetDescription(
		name = "Fashion-Focused Creative Commons Social Dataset",
		description = "a fashion-focused Creative Commons dataset, which is "
				+ "designed to contain a mix of general images as well as a large "
				+ "component of images that are focused on fashion (i.e., relevant "
				+ "to particular clothing items or fashion accessories)",
		creator = "Babak Loni, Maria Menendez, Mihai Georgescu, Luca Galli, "
				+ "Claudio Massari, Ismail Sengor Altingovde, Davide Martinenghi, "
				+ "Mark Melenhorst, Raynor Vliegendhart, Martha Larson",
		downloadUrls = {
				"http://skuld.cs.umass.edu/traces/mmsys/2013/fashion/Fashion Dataset.zip" })
public class MMSys2013
{
	/**
	 * Allowable types of answer for each question.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 12 Aug 2013
	 * @version $Author$, $Revision$, $Date$
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
	 * A response to a HIT
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 12 Aug 2013
	 * @version $Author$, $Revision$, $Date$
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
		 * Constructor
		 * 
		 * @param r1
		 *            contains category depiction
		 * @param r2
		 *            is in correct category
		 * @param familiarity
		 *            familiarity with subject
		 */
		public Response(final QuestionResponse r1, final QuestionResponse r2, final int familiarity)
		{
			this.containsCategoryDepiction = r1;
			this.isInCorrectCategory = r2;
			this.familiarityWithCategory = familiarity;
		}

		@Override
		public String toString()
		{
			return "{" + this.containsCategoryDepiction + "," +
					this.isInCorrectCategory + "," + this.familiarityWithCategory + "}";
		}
	}

	/**
	 * A record in the Fashion 10,000 dataset.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 12 Aug 2013
	 * @version $Author$, $Revision$, $Date$
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
			return this.image.getId() + ":" + this.category + "[" +
					Arrays.toString(this.annotations) + "]";
		}
	}

	protected String baseLocation =
			"/data/degas/mediaeval/mediaeval-crowdsourcing/MMSys2013/";

	protected String expertDataFile =
			"Annotations/Annotation_PerImage_Trusted.csv";

	protected String nonExpertDataFile =
			"Annotations/Annotation_PerImage_NonExperts.csv";

	protected String groundTruthFile =
			"Annotations/GroundTruth.csv";

	protected String queriesFile =
			"Metadata/queries.csv";

	/**
	 * Returns the ground truth set.
	 * 
	 * @return The grouped dataset
	 */
	public GroupedDataset<String, GroupedDataset<String, ListDataset<Response>, Response>, Response>
			getGroundTruth()
	{
		final GroupedDataset<String, GroupedDataset<String, ListDataset<Response>, Response>, Response> results = new MapBackedDataset<String, GroupedDataset<
				String, ListDataset<Response>, Response>, MMSys2013.Response>();

		// The ground truth dataset doesn't contain categories, sadly - just
		// the filename and the results. So we need to go and get the categories
		// for each of the images first. We'll do that from the queries file.
		final HashMap<Long, String> categoryCache = new HashMap<Long, String>();
		boolean firstLine = true;
		for (final String line : new TextLineIterable(new File(this.baseLocation, this.queriesFile)))
		{
			if (!firstLine)
			{
				final String[] parts = line.split(",", -1);

				// The substrings remove the quotes either side of the value
				categoryCache.put(
						Long.parseLong(parts[3].substring(1).substring(0, parts[3].length() - 2)),
						parts[0].substring(1).substring(0, parts[0].length() - 2));
			}

			firstLine = false;
		}

		firstLine = true;
		for (final String line : new TextLineIterable(new File(this.baseLocation, this.groundTruthFile)))
		{
			if (!firstLine)
			{
				try
				{
					final String[] parts = line.split(",", -1);

					// Get the category for the given image.
					final String url = parts[0];
					final FlickrImage fi = FlickrImage.create(new URL(url));
					final String cat = categoryCache.get(fi.getId());

					// Get the category list
					GroupedDataset<String, ListDataset<Response>, Response> gds = results.get(cat);

					// Check whether we already have a dataset for
					// the image in this category
					if (gds == null)
					{
						// Create a new dataset for images in this category
						gds = new MapBackedDataset<String,
								ListDataset<Response>, Response>();
						results.put(cat, gds);
					}

					// See if we have any responses for this image already
					ListDataset<Response> ids = gds.get(url);

					// If not, create the dataset for this image
					if (ids == null)
					{
						ids = new ListBackedDataset<Response>();
						gds.put(url, ids);
					}

					// Get the response for this image and add it
					final Response rr = new Response(
							this.parseQR(parts[1]),
							this.parseQR(parts[2]), 1);
					ids.add(rr);
				} catch (final MalformedURLException e)
				{
					e.printStackTrace();
				}
			}

			firstLine = false;
		}

		return results;
	}

	/**
	 * Returns the results from the non-expert turkers.
	 * 
	 * @return The grouped dataset
	 */
	public GroupedDataset<String, GroupedDataset<String,
			ListDataset<Response>, Response>, Response> getNonExpertData()
	{
		return this.parseMetadata(new File(this.baseLocation, this.nonExpertDataFile));
	}

	/**
	 * Returns the results from the expert turkers.
	 * 
	 * @return The grouped dataset
	 */
	public GroupedDataset<String, GroupedDataset<String,
			ListDataset<Response>, Response>, Response> getExpertData()
	{
		return this.parseMetadata(new File(this.baseLocation, this.expertDataFile));
	}

	/**
	 * @param metadataFile
	 * @return A grouped dataset
	 */
	public GroupedDataset<String, GroupedDataset<String,
			ListDataset<Response>, Response>, Response> parseMetadata(
					final File metadataFile)
	{
		final GroupedDataset<String, GroupedDataset<String, ListDataset<Response>, Response>, Response> results = new MapBackedDataset<String, GroupedDataset<
				String, ListDataset<Response>, Response>, MMSys2013.Response>();

		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(metadataFile));
			String line;
			boolean firstLine = true;
			int count = 1;
			while ((line = br.readLine()) != null)
			{
				if (!firstLine)
				{
					try
					{
						final String[] parts = line.split(",", -1);

						final Response[] r = new Response[3];
						r[0] = new Response(this.parseQR(parts[3]),
								this.parseQR(parts[6]), this.parseF(parts[9]));
						r[1] = new Response(this.parseQR(parts[4]),
								this.parseQR(parts[7]), this.parseF(parts[10]));
						r[2] = new Response(this.parseQR(parts[5]),
								this.parseQR(parts[8]), parts.length > 11 ?
										this.parseF(parts[11]) : -1);

						GroupedDataset<String, ListDataset<Response>, Response> gds = results.get(parts[2]);

						// Check whether we already have a dataset for
						// the image in this category
						if (gds == null)
						{
							// Create a new dataset for images in this category
							gds = new MapBackedDataset<String,
									ListDataset<Response>, Response>();
							results.put(parts[2], gds);
						}

						// See if we have any responses for this image already
						ListDataset<Response> ids = gds.get(parts[1]);

						// If not, create the dataset for this image
						if (ids == null)
						{
							ids = new ListBackedDataset<Response>();
							gds.put(parts[1], ids);
						}

						// Add the each response for this image
						for (final Response rr : r)
							ids.add(rr);
					} catch (final Exception e)
					{
						System.err.println("Error on line " + count);
						e.printStackTrace();
					}
				}
				firstLine = false;
				count++;
			}
			br.close();
		} catch (final FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (final IOException e)
		{
			e.printStackTrace();
		} finally
		{
			if (br != null)
				try
				{
					br.close();
				} catch (final IOException e)
				{
					e.printStackTrace();
				}
		}

		return results;
	}

	/**
	 * Given a string returns a question response.
	 * 
	 * @param qr
	 *            The string
	 * @return A {@link QuestionResponse}
	 */
	protected QuestionResponse parseQR(final String qr)
	{
		if (qr.toLowerCase().equals("yes"))
			return QuestionResponse.YES;
		if (qr.toLowerCase().equals("no"))
			return QuestionResponse.NO;
		if (qr.toLowerCase().equals("notsure"))
			return QuestionResponse.NOT_SURE;
		return QuestionResponse.UNANSWERED;
	}

	protected int parseF(final String f)
	{
		try
		{
			return Integer.parseInt(f);
		} catch (final NumberFormatException e)
		{
			return -1;
		}
	}

	/**
	 * For a given {@link GroupedDataset} that represents the results from a
	 * single category, returns a list of scored annotations for each group, for
	 * question 1 (contains depication of category).
	 * 
	 * @param data
	 *            The data
	 * @return a list of {@link ScoredAnnotation} linked to image URL
	 */
	public static Map<String, List<ScoredAnnotation<QuestionResponse>>>
			getAnnotationsQ1(
					final GroupedDataset<String, ListDataset<Response>, Response> data)
	{
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> r =
				new HashMap<String, List<ScoredAnnotation<QuestionResponse>>>();

		// Loop through the images in this dataset
		for (final String imgUrl : data.getGroups())
		{
			final ListDataset<Response> l = data.get(imgUrl);

			final List<ScoredAnnotation<QuestionResponse>> l2 =
					new ArrayList<ScoredAnnotation<QuestionResponse>>();
			r.put(imgUrl, l2);

			// Loop through the responses for this image
			for (final Response rr : l)
				l2.add(new ScoredAnnotation<QuestionResponse>(
						rr.containsCategoryDepiction, rr.familiarityWithCategory));
		}

		return r;
	}

	/**
	 * For a given {@link GroupedDataset} that represents the results from a
	 * single category, returns a list of scored annotations for each group, for
	 * question 2 (is in category).
	 * 
	 * @param data
	 *            The group name to retrieve
	 * @return a list of {@link ScoredAnnotation} linked to image URL
	 */
	public static Map<String, List<ScoredAnnotation<QuestionResponse>>>
			getAnnotationsQ2(
					final GroupedDataset<String, ListDataset<Response>, Response> data)
	{
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> r =
				new HashMap<String, List<ScoredAnnotation<QuestionResponse>>>();

		// Loop through the images in this dataset
		for (final String imgUrl : data.getGroups())
		{
			final ListDataset<Response> l = data.get(imgUrl);

			final List<ScoredAnnotation<QuestionResponse>> l2 =
					new ArrayList<ScoredAnnotation<QuestionResponse>>();
			r.put(imgUrl, l2);

			// Loop through the responses for this image
			for (final Response rr : l)
				l2.add(new ScoredAnnotation<QuestionResponse>(
						rr.isInCorrectCategory, rr.familiarityWithCategory));
		}

		return r;
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		System.out.println();

		// Expert annotations for Q1 and Q2
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> q1r1 =
				MMSys2013.getAnnotationsQ1(new MMSys2013().getExpertData().get("Cowboy hat"));
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> q2r1 =
				MMSys2013.getAnnotationsQ2(new MMSys2013().getExpertData().get("Cowboy hat"));

		// Non expert annotations for Q1 and Q2
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> q1r2 =
				MMSys2013.getAnnotationsQ1(new MMSys2013().getNonExpertData().get("Cowboy hat"));
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> q2r2 =
				MMSys2013.getAnnotationsQ2(new MMSys2013().getNonExpertData().get("Cowboy hat"));

		// Ground truth data for Q1 and Q2
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> q1gt =
				MMSys2013.getAnnotationsQ1(new MMSys2013().getGroundTruth().get("Cowboy hat"));
		final Map<String, List<ScoredAnnotation<QuestionResponse>>> q2gt =
				MMSys2013.getAnnotationsQ2(new MMSys2013().getGroundTruth().get("Cowboy hat"));

		// Majority voting on the data sets
		final Map<String, ObjectFloatPair<ScoredAnnotation<QuestionResponse>>> q1r1mv =
				MajorityVoting.calculateBasicMajorityVote(q1r1);
		final Map<String, ObjectFloatPair<ScoredAnnotation<QuestionResponse>>> q2r1mv =
				MajorityVoting.calculateBasicMajorityVote(q2r1);
		final Map<String, ObjectFloatPair<ScoredAnnotation<QuestionResponse>>> q1r2mv =
				MajorityVoting.calculateBasicMajorityVote(q1r2);
		final Map<String, ObjectFloatPair<ScoredAnnotation<QuestionResponse>>> q2r2mv =
				MajorityVoting.calculateBasicMajorityVote(q2r2);
		final Map<String, ObjectFloatPair<ScoredAnnotation<QuestionResponse>>> q1gtmv =
				MajorityVoting.calculateBasicMajorityVote(q1gt);
		final Map<String, ObjectFloatPair<ScoredAnnotation<QuestionResponse>>> q2gtmv =
				MajorityVoting.calculateBasicMajorityVote(q2gt);

		// Agreement output
		System.out.println("Question 1 agreement between raters 1 and 2: " +
				CohensKappaInterraterAgreement.calculate(q1r1mv, q1r2mv));
		System.out.println("Question 1 agreement between rater 1 and GT: " +
				CohensKappaInterraterAgreement.calculate(q1r1mv, q1gtmv));
		System.out.println("Question 1 agreement between rater 2 and GT: " +
				CohensKappaInterraterAgreement.calculate(q1r2mv, q1gtmv));

		System.out.println("Question 2 agreement between raters 1 and 2: " +
				CohensKappaInterraterAgreement.calculate(q2r1mv, q2r2mv));
		System.out.println("Question 2 agreement between rater 1 and GT: " +
				CohensKappaInterraterAgreement.calculate(q2r1mv, q2gtmv));
		System.out.println("Question 2 agreement between rater 2 and GT: " +
				CohensKappaInterraterAgreement.calculate(q2r2mv, q2gtmv));
	}
}
