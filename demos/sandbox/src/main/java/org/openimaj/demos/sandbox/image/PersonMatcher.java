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
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.kohsuke.args4j.Option;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.dataset.BingImageDataset;
import org.openimaj.image.feature.global.SharpPixelProportion;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.io.IOUtils;
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
 * and attempts to classify the found faces. It will classify them in increasing
 * size order; so if more than one face is classified as a particular person,
 * then only the largest instance will remain (if the options to allow only one
 * instance is true). Annotations which are removed due to this constraint will
 * be relabelled, if possible. There is also an option to ignore faces which are
 * significantly blurred ("significantly" can be defined).
 * <p>
 * Note that, when run for the first time, the system will ask you to go and get
 * an APPID for the Bing Search, and it will give you the URL to go get it.
 * <p>
 * The main method is just a test method - it will delete the recogniser after
 * it's done.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 5 Feb 2013
 * @version $Author$, $Revision$, $Date$
 */
public class PersonMatcher
{
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

	/** Whether to save the recogniser or not */
	private boolean saveRecogniser = false;

	/**
	 * The threshold to apply to the annotator above which no match will be
	 * considered
	 */
	@Option(name = "--threshold", aliases = "-t",
			usage = "The matching threshold (default: 8)")
	private float matchingThreshold = 8f;

	/** The recognition strategy to use */
	@Option(name = "--strategy", aliases = "-s",
			usage = "The recognition strategy to use (default: CLMFeature_KNN)")
	private final RecognitionStrategy strategy = RecognitionStrategy.CLMFeature_KNN;

	@Option(name = "--onlyOne", aliases = "-o",
			usage = "Allow only one instance of each person (default: true)")
	/** If true, only one instance of each person will be allowed in a photo */
	private final boolean allowOnlyOneInstance = true;

	@Option(name = "--ignoreBlurred", aliases = "-b",
			usage = "Ignore faces which are considerably blurred (default: true)")
	/** If true, will ignore faces which are blurred */
	private final boolean ignoreBlurredFaces = true;

	@Option(name = "--blurThreshold", aliases = "-bt",
			usage = "The threshold to use for blur detection (default: 0.2)")
	/** Only used if ignoreBlurredFaces is true */
	private final float blurThreshold = 0.2f;

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
	public PersonMatcher(final List<String> queries, final File recogniserFile,
			final boolean addCounterExamples)
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
	 *            The file to save the recogniser into (NULL for no saving)
	 * @param addCounterExamples
	 *            Whether to add counter examples
	 * @throws Exception
	 *             If the face recognition engine could not be initialised
	 */
	public PersonMatcher(final String[] queries, final File recogniserFile,
			final boolean addCounterExamples)
			throws Exception
	{
		this(recogniserFile);

		if (recogniserFile != null)
			this.saveRecogniser = true;

		// Train using the given queries
		this.train(queries);

		// Add a set of images that are not of the query person.
		if (addCounterExamples)
			this.addCounterExamples();

		// Save the recogniser for later
		if (this.saveRecogniser)
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
		final List<? extends IndependentPair<? extends DetectedFace, ScoredAnnotation<String>>> recognisedFaces = this.faceRecogniser
				.recogniseBest(fi);

		for (final IndependentPair<? extends DetectedFace, ScoredAnnotation<String>> p : recognisedFaces) {
			if (p.secondObject() == null)
				p.setSecondObject(new ScoredAnnotation<String>("Unknown", 1.0f));
		}

		// If we are to ignore blurred faces, we'll remove them here
		// by using the SharpPixelProportion analyser to detect whether
		// the image within the face region is blurred or not
		if (this.ignoreBlurredFaces)
		{
			// We'll use the SharpPixelProportion analyser to work out how much
			// is blurred
			final SharpPixelProportion spp = new SharpPixelProportion();

			// Iterate over the detected faces
			final Iterator<? extends IndependentPair<? extends DetectedFace, ScoredAnnotation<String>>> it = recognisedFaces
					.iterator();
			while (it.hasNext())
			{
				final IndependentPair<? extends DetectedFace, ScoredAnnotation<String>> facePair = it.next();

				// Analyse the face patch...
				facePair.firstObject().getFacePatch().analyseWith(spp);

				// If the pixels are mostly blurred, remove the face from the
				// list.
				final double pp = spp.getBlurredPixelProportion();
				if (pp < this.blurThreshold)
					it.remove();
			}
		}

		// Sort on the size of the face
		Collections.sort(recognisedFaces,
				new Comparator<IndependentPair<? extends DetectedFace, ScoredAnnotation<String>>>()
				{
					@Override
					public int compare(
							final IndependentPair<? extends DetectedFace, ScoredAnnotation<String>> o1,
							final IndependentPair<? extends DetectedFace, ScoredAnnotation<String>> o2)
					{
						return (int) (o2.firstObject().getShape().calculateArea()
						- o1.firstObject().getShape().calculateArea());
					}
				});

		System.out.println("Recognised " + recognisedFaces.size() + " faces.");
		System.out.println(recognisedFaces);

		// If we're only allowing a single instance of a face within an image,
		// we need
		// to check whether the recognised faces have been assigned to the same
		// person
		// more than once. If so, we'll check the faces in size order and remove
		// any
		// existing names.
		if (this.allowOnlyOneInstance)
		{
			final HashSet<String> seenPeople = new HashSet<String>();
			final Iterator<? extends IndependentPair<? extends DetectedFace, ScoredAnnotation<String>>> it = recognisedFaces
					.iterator();
			while (it.hasNext())
			{
				final IndependentPair<? extends DetectedFace, ScoredAnnotation<String>> facePair = it.next();

				// If we've already seen the person that this face might be, we
				// remove it from the list. We'll try again at recognising them.
				if (seenPeople.contains(facePair.secondObject().annotation))
				{
					// it.remove();
					facePair.secondObject().annotation = "Removed " + facePair.secondObject().annotation;

					// Try to annotate this face again but within some
					// constraints.
					// The constraints will be all the possible annotations
					// minus those
					// that we've already seen.
					final HashSet<String> constraints = new HashSet<String>();
					constraints.addAll(this.faceRecogniser.getRecogniser().getAnnotations());
					constraints.removeAll(seenPeople);

					// Recognise the best from the people we've not already seen
					final List<? extends IndependentPair<? extends DetectedFace, ScoredAnnotation<String>>> r = this.faceRecogniser
							.recogniseBest(
									facePair.firstObject().getFacePatch(), constraints);

					// If we have a new person, then update the pair and add it
					// to
					// the seen people
					if (r != null && r.size() > 0 && r.get(0).getSecondObject() != null)
					{
						// Update the annotation for this face.
						facePair.getSecondObject().annotation = r.get(0).getSecondObject().annotation;

						// Remember that this person has been seen
						seenPeople.add(facePair.getSecondObject().annotation);
					}
				}

				// Note that we've seen this person
				seenPeople.add(facePair.secondObject().annotation);
			}
		}

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
			final File recogniserFile) throws IOException
	{
		// If we have a pre-trained file to load, load it in.
		if (recogniserFile != null && recogniserFile.exists())
		{
			System.out.println("Loading existing recogniser from " + recogniserFile + " to update...");

			final FaceRecognitionEngine<DetectedFace, String> fre = FaceRecognitionEngine
					.load(recogniserFile);
			return fre;
		}

		// No pre-trained file? Then just create a new, clean, fresh and sparkly
		// new engine.
		try
		{
			// We look for a field called "threshold" in the strategy and set
			// the threshold
			// to the value in the options. If the field doesn't exist, we'll
			// ignore it.
			final Field f = this.strategy.getClass().getDeclaredField("threshold");
			f.setAccessible(true);
			f.setFloat(this.strategy, this.matchingThreshold);
			System.out.println("Field: " + f);
		} catch (final NoSuchFieldException e)
		{
			System.out.println("WARNING: No threshold field to set in " + this.strategy + ".");
		} catch (final SecurityException e)
		{
			System.out.println("WARNING: No threshold field to set in " + this.strategy + ".");
		} catch (final IllegalArgumentException e)
		{
			e.printStackTrace();
		} catch (final IllegalAccessException e)
		{
			e.printStackTrace();
		}
		final RecognitionEngineProvider<?> o = this.strategy.getOptions();
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
			for (final File cachedImage : f.listFiles(new FilenameFilter()
			{
				@Override
				public boolean accept(final File file, final String filename)
				{
					return filename.endsWith(".png");
				}
			}))
			{
				try
				{
					this.processImageURL(ImageUtilities.readMBF(cachedImage), label);
				} catch (final MalformedURLException m)
				{
					m.printStackTrace();
				} catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
			return;
		}

		final BingAPIToken apiToken = DefaultTokenFactory.get(BingAPIToken.class);
		final BingImageDataset<MBFImage> results = BingImageDataset.create(
				ImageUtilities.MBFIMAGE_READER, apiToken, query, 10);

		System.out.println("    - Got " + results.getImages().size() + " results");

		// Loop over all the results and process each one
		for (final MBFImage result : results)
			this.processImageURL(result, label);
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
		final UUID uuid = UUID.nameUUIDFromBytes(result.toByteImage());
		final String cacheFilename = PersonMatcher.CACHE_DIR + "/" + label + "/" + uuid + ".png";
		if (this.cacheImages)
		{
			try
			{
				final File f = new File(cacheFilename);
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
		List<? extends DetectedFace> detectedFaces = null;
		if (this.cacheImages && new File(cacheFilename + ".detectedFaces").exists())
		{
			System.out.println("    - Reading from file " + cacheFilename + ".detectedFaces...");
			try {
				detectedFaces = IOUtils.readFromFile(new File(cacheFilename + ".detectedFaces"));
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
		}
		// No cache? Let's run the face detector.
		else
		{
			detectedFaces = this.faceDetector.detectFaces(img);

			// If we're caching, let's also cache the face detection results
			if (this.cacheImages)
			{
				try
				{
					final File f = new File(cacheFilename + ".detectedFaces");
					IOUtils.writeToFile(detectedFaces, f);
				} catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}

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
	 * Get the current matching threhsold of this person matcher.
	 * 
	 * @return The matching threshold
	 */
	public float getMatchingThreshold()
	{
		return this.matchingThreshold;
	}

	/**
	 * Set the matching threshold of this person matcher. Note that this must be
	 * called prior to processing a frame; calling afterwards will have no
	 * effect.
	 * 
	 * @param matchingThreshold
	 *            The matching threshold to set
	 */
	public void setMatchingThreshold(final float matchingThreshold)
	{
		this.matchingThreshold = matchingThreshold;
	}

	/**
	 * 
	 * @param resource
	 * @return The displayed image
	 * @throws Exception
	 */
	public static MBFImage displayQueryResults(final URL resource) throws Exception
	{
		System.out.println("----------- QUERYING ----------- ");
		final FImage fi = ImageUtilities.readF(resource);
		final PersonMatcher pm = new PersonMatcher(new File(PersonMatcher.RECOGNISER_FILE));
		final List<? extends IndependentPair<? extends DetectedFace, ScoredAnnotation<String>>> l = pm.query(fi);

		final MBFImage m = new MBFImage(fi.getWidth(), fi.getHeight(), 3);
		m.addInplace(fi);
		int count = 1;
		for (final IndependentPair<? extends DetectedFace, ScoredAnnotation<String>> i : l)
		{
			final Rectangle b = i.firstObject().getBounds();
			m.drawShape(b, RGBColour.RED);
			final String name = count + " : " +
					(i.secondObject() == null ? "Unknown" : i.secondObject().annotation);
			m.drawText(name, (int) b.x, (int) b.y,
					HersheyFont.TIMES_MEDIUM, 12, RGBColour.GREEN);
			count++;
		}
		DisplayUtilities.display(m);
		return m;
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
					new File(PersonMatcher.RECOGNISER_FILE),
					true);
		} catch (final Exception e)
		{
			e.printStackTrace();
		}

		// Load back in the recogniser and try querying using the given image
		try
		{
			// PersonMatcher.displayQueryResults(
			// PersonMatcher.class.getResource(
			// "/org/openimaj/demos/sandbox/BarackObama1.jpg"));
			//
			// PersonMatcher.displayQueryResults(
			// PersonMatcher.class.getResource(
			// "/org/openimaj/demos/sandbox/BarackObama2.jpg"));
			//
			// PersonMatcher.displayQueryResults(
			// PersonMatcher.class.getResource(
			// "/org/openimaj/demos/sandbox/BarackObama5.jpg"));
			//
			// PersonMatcher.displayQueryResults(
			// PersonMatcher.class.getResource(
			// "/org/openimaj/demos/sandbox/ArnoldSchwarzenegger1.jpg"));

			PersonMatcher
					.displayQueryResults(new URL(
							"http://www2.pictures.gi.zimbio.com/Barack%2BObama%2BArnold%2BSchwarzenegger%2BBloomberg%2BO6kM6r0LSK-l.jpg"));

			PersonMatcher.displayQueryResults(new URL(
					"http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2008/08/02/Arnie-460x276.jpg"));

			PersonMatcher.displayQueryResults(new URL(
					"http://images.politico.com/global/2012/09/120930_arnold_maria_reu.jpg"));

			PersonMatcher
					.displayQueryResults(new URL(
							"http://assets-s3.usmagazine.com/uploads/assets/articles/56812-what-do-you-want-to-ask-president-barack-obama/1350336415_barack-obama-467.jpg"));

			// Remove the recogniser (for testing)
			new File(PersonMatcher.RECOGNISER_FILE).delete();
		} catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
}
