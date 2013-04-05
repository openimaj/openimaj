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
package org.openimaj.demos.sandbox.image;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.UUID;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.dataset.BingImageDataset;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.tools.faces.recognition.options.RecognitionEngineProvider;
import org.openimaj.tools.faces.recognition.options.RecognitionStrategy;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.BingAPIToken;
import org.openimaj.util.pair.IndependentPair;

/**
 * Class for providing verification of unseen people in images. It does this by
 * limiting the search space to be a verification problem rather than a
 * recognition problem - that is, the possible number of people that could
 * possibly be in an image is limited. The tool does a web search (currently
 * using Bing) to retrieve images of the people in question and trains a face
 * recognition engine using these. It then looks in the query image for faces
 * and attempts to classify the found faces.
 * <p>
 * Note that, when run for the first time, the system will ask you to go and
 * get an APPID for the Bing Search, and it will give you the URL to go get it.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 5 Feb 2013
 * @version $Author$, $Revision$, $Date$
 */
public class PersonMatcher
{
	/** Bing App Id */
	private static final String APPID = "S27Lmrra8fEPf4mvSN9iPsWKZTUayXVTdaMIbP5uRiQ=";

	/** The file the recogniser will be saved into */
	private static final String RECOGNISER_FILE = "recogniser.rec";

	/** Where to cache images */
	private static final String CACHE_DIR = "cache";

	/** The face detector to use to detect faces in all images */
	private final FaceDetector<?, FImage> faceDetector;

	/** The face recognition engine we'll use */
	private final FaceRecognitionEngine<? extends DetectedFace, String> faceRecogniser;

	/** Whether to cache search results */
	private final boolean cacheImages = true;

	/**
	 * Create a person matcher
	 *
	 * @throws Exception
	 */
	public PersonMatcher() throws Exception
	{
		this(null);
	}

	/**
	 * Create a person matcher with the given file
	 *
	 * @param recogniserFile
	 *            The recogniser file to load
	 * @throws Exception
	 */
	public PersonMatcher(final File recogniserFile) throws Exception
	{
		// Setup a new face recognition engine
		this.faceRecogniser = this.getFaceRecogniserEngine(recogniserFile);

		if (this.faceRecogniser == null)
			throw new Exception("Face recogniser not initialised");

		// Get the face detector for this strategy
		this.faceDetector = this.faceRecogniser.getDetector();
	}

	/**
	 * Create a recogniser for the given person into the given file.
	 *
	 * @param person
	 *            The person to create a recogniser for
	 * @param recogniserFile
	 *            The recogniser file to save
	 * @throws Exception
	 *             If the face recognition engine could not be initialised
	 */
	public PersonMatcher(final String person, final File recogniserFile) throws Exception
	{
		this(new String[] { person }, recogniserFile, true);
	}

	/**
	 * Constructor that takes a query string.
	 *
	 * @param queries
	 *            The query strings to use
	 * @param recogniserFile
	 *            The file to save the recogniser into
	 * @param addCounterExamples
	 *            Whether to add counter examples
	 * @throws Exception
	 *             If the face recognition engine could not be initialised
	 */
	public PersonMatcher(final List<String> queries, final File recogniserFile, final boolean addCounterExamples)
			throws Exception
	{
		this(queries.toArray(new String[0]), recogniserFile, addCounterExamples);
	}

	/**
	 * Constructor that takes a set of queries to search for
	 *
	 * @param queries
	 *            The query strings to use
	 * @param recogniserFile
	 *            The file to save the recogniser into
	 * @param addCounterExamples
	 *            Whether to add counter examples
	 * @throws Exception
	 *             If the face recognition engine could not be initialised
	 */
	public PersonMatcher(final String[] queries, final File recogniserFile, final boolean addCounterExamples)
			throws Exception
	{
		this(recogniserFile);

		// Train using the given queries
		this.train(queries);

		// Add a set of images that are not of the query person.
		if (addCounterExamples)
			this.addCounterExamples();

		// Save the recogniser for later
		this.saveRecogniser(recogniserFile);
	}

	/**
	 * After training, you might want to save the recogniser
	 *
	 * @param recogniserFile
	 *            The recogniser file to save to
	 * @throws IOException
	 */
	public void saveRecogniser(final File recogniserFile) throws IOException
	{
		System.out.println("Saving recogniser to " + recogniserFile);

		// Save the recogniser
		this.faceRecogniser.save(recogniserFile);
	}

	/**
	 * Train the recogniser with examples retrieved from searching with the
	 * given queries.
	 *
	 * @param queries
	 *            The query strings
	 */
	public void train(final String[] queries)
	{
		// Now go and retrieve the images for the query
		for (final String query : queries)
			this.searchForExamples(query, query, false);
	}

	/**
	 * @param fi
	 *            The image to find the query person within
	 * @return The matching results
	 */
	public List<? extends IndependentPair<? extends DetectedFace, ScoredAnnotation<String>>>
			query(final FImage fi)
	{
		System.out.println("Querying with image");

		// Recognise the unknown faces in the image.
		final List<? extends IndependentPair<? extends DetectedFace, ScoredAnnotation<String>>>
			recognisedFaces = this.faceRecogniser.recogniseBest(fi);

		System.out.println("Recognised " + recognisedFaces.size() + " faces.");
		System.out.println(recognisedFaces);

		return recognisedFaces;
	}

	/**
	 * Returns a face recogniser by using the FaceRecogniserTools.
	 *
	 * @param recogniserFile
	 * @return The face recogniser engine
	 * @throws IOException
	 */
	private FaceRecognitionEngine<? extends DetectedFace, String> getFaceRecogniserEngine(
			final File recogniserFile )	throws IOException
	{
		if( recogniserFile.exists() )
		{
			System.out.println("Loading existing recogniser from " + recogniserFile + " to update...");

			final FaceRecognitionEngine<DetectedFace, String> fre =
					FaceRecognitionEngine.load(recogniserFile);
			return fre;
		}

		final RecognitionEngineProvider<? extends DetectedFace> o =
				RecognitionStrategy.CLMFeature_KNN.getOptions();
		return o.createRecognitionEngine();
	}

	/**
	 * Adds a set of counter examples to the recogniser by searching the web for
	 * the generic string "face" and adding them as an unknown person.
	 */
	public void addCounterExamples()
	{
		this.searchForExamples("face", "unknown", true);
	}

	/**
	 * Retrieves a set of images from Bing that match the query then for each
	 * one calls the face recogniser to train it.
	 */
	private void searchForExamples(final String query, final String label, final boolean facesOnly)
	{
		System.out.println("Searching for '" + query + "'");

		File f = null;
		if (this.cacheImages && (f = new File(PersonMatcher.CACHE_DIR + "/" + label + "/")).exists())
		{
			System.out.println("Using cached images: ");
			for (final File cachedImage : f.listFiles())
			{
				try
				{
					this.processImageURL(ImageUtilities.readMBF(cachedImage), label);
				}
				catch (final MalformedURLException m)
				{
					m.printStackTrace();
				}
				catch( final IOException e )
				{
					e.printStackTrace();
				}
			}
			return;
		}

		final BingAPIToken apiToken = DefaultTokenFactory.get( BingAPIToken.class );
		final BingImageDataset<MBFImage> results = BingImageDataset.create(
				ImageUtilities.MBFIMAGE_READER, apiToken, query, 10 );

		System.out.println("    - Got " + results.getImages().size() + " results");

		// Loop over all the results and process each one
		for (final MBFImage result : results )
			this.processImageURL( result, label );
	}

	/**
	 * For each URL (that is an image representation of the query), load it in
	 * and train the face recogniser.
	 *
	 * @param result
	 *            The URL of an image that is a representation of the query
	 * @param label
	 *            The classification of the URL
	 */
	private void processImageURL(final MBFImage result, final String label)
	{
		if (this.cacheImages)
		{
			try
			{
				final UUID uuid = UUID.nameUUIDFromBytes( result.toByteImage() );
				final File f = new File(PersonMatcher.CACHE_DIR + "/" + label + "/" + uuid + ".png");
				f.getParentFile().mkdirs();
				if (!f.exists())
					ImageUtilities.write(result, f);
			} catch (final IOException e)
			{
				e.printStackTrace();
			}
		}

		System.out.println("Reading " + result);

		// Read in the result image
		final FImage img = result.flatten();

		// Get the detected faces from the given image
		final List<? extends DetectedFace> detectedFaces =
				this.faceDetector.detectFaces(img);

		System.out.println("    - Found " + detectedFaces.size() + " faces ");

		// If there is more than one person in the image (or none),
		// then we can't sensibly say which one was the query.. so
		// we must ignore. If there is only one detected face, we
		// assume that it's the face of the person in the query
		if (detectedFaces.size() == 1)
			this.faceRecogniser.train(label, img);
		else
			System.out.println("    - Ignoring this image.");
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException
	{
		// Use the constructor that takes the queries and automatically creates
		// a recogniser for the given person and then saves it
		try
		{
			System.out.println("----------- TRAINING ---------- ");
			new PersonMatcher(
					new String[] { "Barack Obama", "Arnold Schwarzenegger" },
					new File(PersonMatcher.RECOGNISER_FILE), false);
		} catch (final Exception e)
		{
			e.printStackTrace();
		}

		// Load back in the recogniser and try querying using the given image
		try
		{
			System.out.println("----------- QUERYING ----------- ");
			final FImage fi = ImageUtilities.readF(PersonMatcher.class.getResource(
					"/org/openimaj/demos/sandbox/BarackObama1.jpg"));
			final PersonMatcher pm = new PersonMatcher(new File(PersonMatcher.RECOGNISER_FILE));
			final List<? extends IndependentPair<? extends DetectedFace, ScoredAnnotation<String>>>
				l = pm.query(fi);

			final MBFImage m = new MBFImage(fi, fi, fi);
			for (final IndependentPair<? extends DetectedFace, ScoredAnnotation<String>> i : l)
			{
				final Rectangle b = i.firstObject().getBounds();
				m.drawShape(b, RGBColour.RED);
				m.drawText(i.secondObject().annotation, (int) b.x, (int) b.y,
						HersheyFont.TIMES_MEDIUM, 12, RGBColour.GREEN);
			}
			DisplayUtilities.display(m);
			ImageUtilities.write(m, new File("output.png"));
		} catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
}
