/**
 *
 */
package org.openimaj.demos.sandbox.video;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.CLMDetectedFace;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.tools.faces.recognition.options.RecognitionEngineProvider;
import org.openimaj.tools.faces.recognition.options.RecognitionStrategy;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.Video;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.openimaj.video.processing.shotdetector.HistogramVideoShotDetector;
import org.openimaj.video.processing.shotdetector.VideoShotDetector;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 * Person linker takes a video and makes links between the various depictions of
 * the same person by using a face recognition engine for face verification.
 * <p>
 * There are various challenges associated with doing this. For example, how
 * much training is needed?
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 12 Mar 2013
 */
public class PersonLinker
{
	/**
	 * A tracked person is a tracked face with a person identifier.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 18 Mar 2013
	 */
	protected static class TrackedPerson
	{
		/** The face */
		public CLMDetectedFace face;

		/** An identifier for the person */
		public String personIdentifier;
	}

	/**
	 * Options for the person matcher.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk) 8 * @created 12 Mar 2013
	 */
	protected static class PersonLinkerOptions
	{
		@Argument(metaVar = "FILE",
				usage = "Video file to process", required = false)
		/** The input file to process */
		public File inputFile = null;

		@Option(name = "--recogniser", aliases = "-r",
				usage = "Pre-trained recogniser to use (default: none)")
		/** The recogniser file to load */
		public File recogniserFile = null;

		@Option(name = "--display", aliases = "-d",
				usage = "Display video during processing (default: false)")
		/** Whether to display the video during processing */
		public boolean display = false;

		@Option(name = "--threshold", aliases = "-t",
				usage = "The confidence threshold for face matching (default: 0.75)")
		/** The confidence threshold determining when a face is matched or a new face is found */
		public float threshold = 0.75f;

		@Option(name = "--maxTrainingFrames", aliases = "-m",
				usage = "The maximum number of frames to train a single person on (default: 20)")
		/** The maximum number of frames to train a single person on. */
		public int maxTrainingFrames = 20;

		@Option(name = "--help", aliases = "-h",
				usage = "Display this help")
		/** Whether to display help information */
		public boolean displayHelp = false;
	}

	/** The options in use for this linker */
	private PersonLinkerOptions options = null;

	/** The face recognition engine used to match people */
	private FaceRecognitionEngine<DetectedFace, String> faceRecogniser;

	/** The shot detector we'll use to know when to reset the tracker */
	private final VideoShotDetector<MBFImage> shotDetector = new HistogramVideoShotDetector();

	/** The face tracker we'll use to track faces across frames */
	private final CLMFaceTracker tracker = new CLMFaceTracker();

	/** The map from faces to people to avoid having recognise all the time */
	private final Map<CLMDetectedFace, String> trackedFacesMap =
			new HashMap<CLMDetectedFace, String>();

	/** If we're training the annotator on a new face, we store it in here */
	private final Set<IndependentPair<DetectedFace, String>> trainingFaces =
			new HashSet<IndependentPair<DetectedFace, String>>();

	/** The number of training examples encountered for any particular person */
	private final Map<String, Integer> trainingExamplesCount =
			new HashMap<String, Integer>();

	/** Cache for conversion for robustly converting tracked faces */
	private final Map<TrackedFace, CLMDetectedFace> conversionCache =
			new HashMap<TrackedFace, CLMDetectedFace>();

	/** Inverse index for the conversion cache */
	private final Map<CLMDetectedFace, TrackedFace> inverseConversionCache =
			new HashMap<CLMDetectedFace, MultiTracker.TrackedFace>();

	/**
	 * Default constructor that takes the options object.
	 * 
	 * @param options
	 *            The options for the new PersonLinker
	 */
	public PersonLinker(final PersonLinkerOptions options)
	{
		this.options = options;

		try
		{
			// Set the face tracker to redetect faces regularly.
			this.tracker.fpd = 10;

			// Instantiate the face recognition engine.
			this.faceRecogniser = this.getFaceRecogniserEngine(this.options.recogniserFile);
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Process the video
	 * 
	 * @param v
	 *            The video to process
	 */
	public void processVideo(final Video<MBFImage> v)
	{
		for (final MBFImage frame : v)
			this.processFrame(frame);
	}

	/**
	 * Process the video frame given
	 * 
	 * @param frame
	 *            The frame to process
	 */
	public void processFrame(final MBFImage frame)
	{
		// Use the shot detector to find out if this frame is a shot boundary.
		this.shotDetector.processFrame(frame);

		// If a shot boundary was detected, reset the tracker.
		if (this.shotDetector.wasLastFrameBoundary())
			this.tracker.reset();

		// Track the faces in the frame (if there are any)
		this.tracker.track(frame);

		// Get the list of faces being tracked.
		final List<CLMDetectedFace> faces = this.cachedConvert(
				this.tracker.getTrackedFaces(), frame.flatten());

		// If we have at least one face, we can do something.
		if (faces.size() > 0)
		{
			this.trainingFaces.clear();

			// If there are no people annotated in our recogniser yet,
			// then we simply train with the current faces.
			if (this.faceRecogniser.getRecogniser().getAnnotations().size() == 0)
			{
				final int nPersons = this.faceRecogniser.getRecogniser().getAnnotations().size();
				System.out.println("Annotator empty. Adding " + faces.size() + " faces to training list...");
				for (int i = 0; i < faces.size(); i++)
				{
					final String personName = "Person " + (nPersons + i);
					this.trainingFaces.add(new IndependentPair<DetectedFace, String>(
							faces.get(i), personName));
					this.trainingExamplesCount.put(personName, 1);
				}
			}
			// Otherwise, we check to see if any of the faces are recognised
			// with the current annotator. If the confidence of a match
			// is very small, then we'll train instead.
			else
			{
				// This is how many faces we already have trained on
				// We need this to "name" each of the new people we find
				int nPersons = this.faceRecogniser.getRecogniser().getAnnotations().size();

				// Check each of the faces for a match.
				for (int i = 0; i < faces.size(); i++)
				{
					// Get the face
					final CLMDetectedFace face = faces.get(i);

					// Check if we're tracking it already
					if (this.trackedFacesMap.get(face) != null)
					{
						// We already know who it is... but we can carry
						// on training for them based on this image
						this.trainingFaces.add(new IndependentPair<DetectedFace, String>(
								face, this.trackedFacesMap.get(face)));
					}
					// It's not a face we're already tracking...
					else
					{
						// We've started tracking a new face, but we
						// already know of some other faces. So, we need
						// to first check if it's a face we already recognise.
						final ScoredAnnotation<String> x =
								this.faceRecogniser.getRecogniser().annotateBest(face);

						// It's possible there'll be an error getting the face
						// from
						// the face patch, in which case we better ignore this.
						if (x != null)
						{
							// Check if the confidence is over a certain
							// threshold. If it is,
							// we'll assume that we have matched the person in
							// question,
							// in which case we'll simply update the tracking
							// map and keep tracking.
							if (x.confidence > this.options.threshold)
							{
								this.trackedFacesMap.put(face, x.annotation);
								System.out.println("Recognised " + x.annotation + " with confidence " + x.confidence);
							}

							// Otherwise, confidence is low... which means
							// we don't recognise this new face, so we need to
							// train
							// the annotator on this new person instead.
							// The person gets a number for a name.
							else
							{
								final String name = "Person " + nPersons;
								System.out.println("   - Adding " + name);
								this.trainingFaces.add(
										new IndependentPair<DetectedFace, String>(
												face, name));
								nPersons++;
							}
						}
						else
						{
							System.out.println("Warning: unable to extract face from image for " + face);
						}
					}
				}
			}

			// Loop through the list of faces we should be training for, and
			// train, train, train! Woo Woo!
			final Iterator<IndependentPair<DetectedFace, String>> it = this.trainingFaces.iterator();
			while (it.hasNext())
			{
				final IndependentPair<DetectedFace, String> facePair = it.next();
				final String person = facePair.getSecondObject();
				final Integer nExamplesSoFar = this.trainingExamplesCount.get(person);
				final DetectedFace face = facePair.getFirstObject();
				if (nExamplesSoFar < this.options.maxTrainingFrames)
				{
					if (this.options.display)
						DisplayUtilities.displayName(face.getFacePatch(), "Face Patch");

					// Train the recogniser with this face
					this.faceRecogniser.train(face, person);

					// Only train if we're still under the maximum number of
					// training
					// frames set within the options. If we are, then we also
					// increase
					// that counter so we keep track of how many examples we've
					// used.
					this.trainingExamplesCount.put(person, nExamplesSoFar + 1);
				}
				else
				{
					// If we've done enough training on this face, we remove it
					// from the training faces list
					it.remove();
				}
			}

			// If we're to display the video while we're processing it, then
			// we'll do that here. We'll add on the overlays too.
			if (this.options.display)
			{
				final MBFImage f = frame.clone();

				// Draw all the tracked faces
				for (final CLMDetectedFace face : this.trackedFacesMap.keySet())
				{
					// If the face is one being trained on, then it will be
					// coloured
					// red, otherwise coloured green.
					Float[] colour = RGBColour.GREEN;
					for (final IndependentPair<DetectedFace, String> x : this.trainingFaces)
						if (x.firstObject() == face)
							colour = RGBColour.RED;

					// Draw the face model to the frame
					final TrackedFace trackedFace = this.inverseConversionCache.get(face);
					if (trackedFace != null)
					{
						// Draw the face model
						CLMFaceTracker.drawFaceModel(f, trackedFace,
								true, true, true, true, true,
								this.tracker.triangles, this.tracker.connections,
								1f, colour, RGBColour.WHITE, RGBColour.WHITE, RGBColour.RED);

						// Draw the name of the person
						final String person = this.trackedFacesMap.get(face);
						f.drawText(person, (int) trackedFace.lastMatchBounds.x,
								(int) trackedFace.lastMatchBounds.y,
								HersheyFont.TIMES_BOLD, 10, colour);
					}
				}

				DisplayUtilities.displayName(f, "video processing");
			}
		}
	}

	/**
	 * Provides a cached conversion of {@link TrackedFace}s to
	 * {@link CLMDetectedFace}s. That is, if the same {@link TrackedFace} is
	 * passed in to the method, the same {@link CLMDetectedFace} will be
	 * returned from the method. This method will attempt to tidy up the cache
	 * as it goes - that is, if a {@link TrackedFace} does not exist in the list
	 * passed in that does exist in the cache, it will be removed from the
	 * cache.
	 * 
	 * @param list
	 *            The list of {@link TrackedFace}s to convert
	 * @param img
	 *            The image from which they were tracked
	 * @return A list of {@link CLMDetectedFace}s.
	 */
	private List<CLMDetectedFace> cachedConvert(final List<TrackedFace> list, final FImage img)
	{
		final List<CLMDetectedFace> cvt = new ArrayList<CLMDetectedFace>();

		// Clear the inverse cache, we'll reinstate it below
		this.inverseConversionCache.clear();

		// Clean up the cache.
		final Iterator<TrackedFace> it = this.conversionCache.keySet().iterator();
		while (it.hasNext())
			if (!list.contains(it.next()))
				it.remove();

		// Convert the passed in information
		for (final TrackedFace f : list)
		{
			CLMDetectedFace m = null;
			if ((m = this.conversionCache.get(f)) == null)
			{
				m = new CLMDetectedFace(f, img);
				this.conversionCache.put(f, m);
			}

			cvt.add(m);
			this.inverseConversionCache.put(m, f);
		}

		return cvt;
	}

	/**
	 * Returns the shot detector in use.
	 * 
	 * @return The shot detector being used.
	 */
	public VideoShotDetector<MBFImage> getShotDetector()
	{
		return this.shotDetector;
	}

	// ======================================================================
	/**
	 * Returns a face recogniser by using the FaceRecogniserTools.
	 * 
	 * @param recogniserFile
	 * @return The face recogniser engine
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private FaceRecognitionEngine<DetectedFace, String> getFaceRecogniserEngine(final File recogniserFile)
			throws IOException
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
		final RecognitionEngineProvider<?> o = RecognitionStrategy.CLMFeature_KNN.getOptions();
		return (FaceRecognitionEngine<DetectedFace, String>) o.createRecognitionEngine();
	}

	/**
	 * Parses the command line arguments to create an options object.
	 * 
	 * @param args
	 *            The arguments from the command-line
	 * @return The options that were parsed from the command-line
	 */
	public static PersonLinkerOptions parseArgs(final String args[])
	{
		final PersonLinkerOptions o = new PersonLinkerOptions();
		final CmdLineParser p = new CmdLineParser(o);
		try
		{
			p.parseArgument(args);

			if (o.displayHelp)
				throw new CmdLineException(p, "");

			if (o.inputFile != null && !o.inputFile.exists())
				throw new CmdLineException(p, "File " + o.inputFile + " does not exist.");
		} catch (final CmdLineException e)
		{
			System.err.println(e.getMessage());
			System.err.println("java PersonLinker [OPTIONS] [INPUT-FILE]");
			System.err.println("If no input file is provided, the webcam will be used.");
			p.printUsage(System.err);
			System.exit(1);
		}

		return o;
	}

	/**
	 * @param args
	 *            Command-line args
	 * @throws VideoCaptureException
	 */
	public static void main(final String[] args) throws VideoCaptureException
	{
		final PersonLinkerOptions o = PersonLinker.parseArgs(args);
		final PersonLinker pm = new PersonLinker(o);

		Video<MBFImage> video = null;
		if (o.inputFile != null)
			video = new XuggleVideo(o.inputFile);
		else
			video = new VideoCapture(320, 240);

		System.out.println("Processing video from " + (o.inputFile == null ? "webcam" : o.inputFile));
		pm.getShotDetector().setFPS(video.getFPS());
		pm.processVideo(video);
	}
}
