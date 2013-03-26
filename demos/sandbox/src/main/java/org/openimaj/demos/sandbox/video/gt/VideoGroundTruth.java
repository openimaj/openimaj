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
package org.openimaj.demos.sandbox.video.gt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileExistsException;
import org.openimaj.audio.AudioStream;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay.EndAction;
import org.openimaj.video.VideoFrame;
import org.openimaj.video.VideoPlayer;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;
import org.openimaj.video.timecode.VideoTimecode;

/**
 * A video player that allows the recording of timestamps for creating a
 * ground-truth. The class uses an {@link IdentifierProducer} to tag objects
 * within the scene. Each {@link Identifiable} that the producer produces has a
 * set of states (annotations) which are given by the {@link StateProvider}. The
 * tool outputs a list of annotations each linked to a list of frames (
 * {@link IdentifiableVideoFrame}s) each of which contains a list of
 * {@link Identifiable}s whose state changed on that frame. For example,
 * <p>
 * 
 * <pre>
 * <code>
 * 	00:00:01:23  Scene 1 - Face 1  SPEAKING
 * 	00:00:04:12  Scene 1 - Face 1  NOT_SPEAKING
 * 	00:00:06:05  Scene 1 - Face 2  SPEAKING
 * 	00:00:10:01  Scene 1 - Face 2  NOT_SPEAKING
 * 	00:00:11:15  Scene 2 - Face 1  SPEAKING
 * 	00:00:14:00  Scene 2 - Face 1  NOT_SPEAKING
 * </code>
 * </pre>
 * 
 * In this example there are two faces identified (within the first scene). Face
 * 1 speaks between 00:00:01:23 (1 second 23 frames) and 00:00:04:12. Face 2
 * speaks between 00:00:06:05 and 00:00:10:01. The first face in another scene
 * begins speaking again at 00:00:11:15 until 00:00:14:00. Note that Face 1 in
 * Scenes 1 and 2 may be different people (they may not too). The identifier
 * provider does all it can in the situation to provide unique identifiers. The
 * meaning of Face 1 and 2 depends on the implementation of the identifier
 * provider. SPEAKING and NOT_SPEAKING is given by the {@link StateProvider} and
 * the timecodes are given by the {@link IdentifiableVideoFrame} (created by
 * default).
 * <p>
 * Annotations can also include end times. So the above could be represented as:
 * <p>
 * 
 * <pre>
 * <code>
 * 	00:00:01:23-00:00:04:11  Scene 1 - Face 1  SPEAKING
 * 	00:00:04:12-00:00:06:04  Scene 1 - Face 1  NOT_SPEAKING
 * 	etc.
 * </code>
 * </pre>
 * <p>
 * The user interface provided is based on a {@link VideoPlayer}. The
 * {@link StateProvider} can provide its own user interface for selecting the
 * current state, or it can provide a key listener which will be automatically
 * added to the video player controls. The user should be in the state provider
 * loop.
 * <p>
 * The object for annotating is selected by the user of this class. Also, the
 * means for updating the state of any selected identifiable is also determined
 * by the user of this class. When the state of an identifiable changes, the
 * {@link #updateIdentifiable(Identifiable)} method should be called, which will
 * query the state provider for the current state of the identifiable. This will
 * then be added to the dataset for the current video timecode.
 * <p>
 * The class provides a {@link #writeDataset(File)} method for writing the
 * generated dataset to a text file. The file is formatted by the
 * {@link AnnotatedIdentifiable#toString()} method in the form:
 * <p>
 * 
 * <pre>
 * <code>
 * 	id@start>end:[comma-separated-tags]
 * </code>
 * </pre>
 * 
 * This implies that the identifier cannot include @ symbols and that tags may
 * not contain commas. The timecodes are {@link HrsMinSecFrameTimecode} objects
 * represented as strings (Hrs:Mins:Secs:Frames).
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 10 Aug 2012
 * @version $Author$, $Revision$, $Date$
 */
public class VideoGroundTruth
{
	/**
	 * An interface for objects which produce identifiers to stamp. So, if there
	 * are multiple objects within a single video frame, the identifier producer
	 * can produce identifiers that allow them to be tagged. For example, if
	 * there are people in a video, they may be identified by their URI, or just
	 * by a number. The task of the identifier is to ensure that the
	 * identifiable is somehow tracked between frames. For example, if there
	 * were faces in a shot and the faces moved around, moved off screen, etc.
	 * the identifier producer should produce the same ID for each face within a
	 * given series of frames, at least as far as possible.
	 * <p>
	 * The {@link #getIdentifiers()} method should only return identifiers for
	 * objects that are in the current frame (not for the whole video).
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 10 Aug 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public static interface IdentifierProducer
	{
		/**
		 * Returns the list of object identifiers that are being truthed in the
		 * current video frame.
		 * 
		 * @return The identifiers
		 */
		public List<Identifiable> getIdentifiers();
	}

	/**
	 * Provides states for identifiables.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 28 Aug 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public static interface StateProvider
	{
		/**
		 * Returns the current state for the given identifiable.
		 * 
		 * @param id
		 *            The identifiable
		 * @return The states
		 */
		public List<String> getCurrentState(Identifiable id);
	}

	/**
	 * An identifiable video frame that uses the timecode of the frame to
	 * identify the frame. The default implementation uses the timecode of the
	 * frame to identify the frame.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 10 Aug 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public static class IdentifiableVideoFrame extends VideoFrame<MBFImage>
			implements Identifiable
	{
		/**
		 * @param frame
		 *            The frame
		 * @param timecode
		 *            The timecode
		 */
		public IdentifiableVideoFrame(final MBFImage frame, final VideoTimecode timecode)
		{
			super(frame, timecode);
		}

		/**
		 * 
		 * @param frame
		 *            The frame
		 */
		public IdentifiableVideoFrame(final VideoFrame<MBFImage> frame)
		{
			super(frame.frame, frame.timecode);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.openimaj.data.identity.Identifiable#getID()
		 */
		@Override
		public String getID()
		{
			return "" + this.timecode.toString();
		}

		@Override
		public String toString()
		{
			return this.getID();
		}
	}

	/**
	 * An identifiable that has been annotated with states (tags) between
	 * specific times. If the end timestamp is null, then the start timestamp
	 * gives the only time at which the tags are valid.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 28 Aug 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public static class AnnotatedIdentifiable implements Identifiable
	{
		/** The timestamp at which the tags are valid for the identifiable */
		public VideoTimecode startTimestamp;

		/**
		 * The timestamp at which the tags are no longer valid for the
		 * identifiable
		 */
		public VideoTimecode endTimestamp;

		/** This identifiable */
		public Identifiable id;

		/** Tags associated with this identifiable */
		public List<String> tags;

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.openimaj.data.identity.Identifiable#getID()
		 */
		@Override
		public String getID()
		{
			return this.id.getID();
		}

		@Override
		public String toString()
		{
			return this.id + "@" + this.startTimestamp + ">" + this.endTimestamp + ":" + this.tags;
		}
	}

	/** The display */
	private VideoPlayer<MBFImage> display = null;

	/** List of all the events */
	private ListBackedDataset<AnnotatedIdentifiable> dataset = null;

	/** The video */
	private Video<MBFImage> video = null;

	/** The state provider */
	private final StateProvider stateProvider;

	/**
	 * Constructor that provides an identifier producer that returns the current
	 * frame of the video.
	 * 
	 * @param video
	 *            The video to ground truth
	 * @param sp
	 *            The state provider
	 */
	public VideoGroundTruth(final Video<MBFImage> video, final StateProvider sp)
	{
		this(video, null, sp);
	}

	/**
	 * Constructor that provides an identifier producer that returns the current
	 * frame of the video; that is the state provider tags frames of video
	 * rather than objects within frames of video.
	 * 
	 * @param video
	 *            The video to ground truth
	 * @param audio
	 *            The audio to play
	 * @param sp
	 *            The state provider
	 */
	public VideoGroundTruth(final Video<MBFImage> video, final AudioStream audio,
			final StateProvider sp)
	{
		// If no IdentifierProducer is given, we'll use one that
		// simply returns a SMTPE-like video timecode.
		this(video, audio, new IdentifierProducer()
		{
			@Override
			public List<Identifiable> getIdentifiers()
			{
				final List<Identifiable> r =
						new ArrayList<Identifiable>();
				r.add(new IdentifiableVideoFrame(video.getCurrentFrame(),
						new HrsMinSecFrameTimecode(video.getCurrentFrameIndex(),
								video.getFPS())));
				return r;
			}
		}, sp);
	}

	/**
	 * Constructor
	 * 
	 * @param video
	 *            The video to ground truth
	 * @param audio
	 *            The audio to play
	 * @param idProd
	 *            The identifier producer
	 * @param sp
	 *            The state provider
	 */
	public VideoGroundTruth(final Video<MBFImage> video, final AudioStream audio,
			final IdentifierProducer idProd, final StateProvider sp)
	{
		this.video = video;
		this.stateProvider = sp;

		// Create a video player (with navigation controls)
		this.display = VideoPlayer.createVideoPlayer(video, audio);
		this.display.setEndAction(EndAction.STOP_AT_END);
		this.display.showFrame();

		this.dataset = new ListBackedDataset<VideoGroundTruth.AnnotatedIdentifiable>();
	}

	/**
	 * Start the process.
	 */
	public void run()
	{
		this.display.run();
	}

	/**
	 * Returns the video player component being used to play the video.
	 * 
	 * @return the video player component.
	 */
	public VideoPlayer<MBFImage> getVideoPlayer()
	{
		return this.display;
	}

	/**
	 * Force the given identifiable to be updated in the dataset for the current
	 * time.
	 * 
	 * @param i
	 *            The identifiable
	 */
	public void updateIdentifiable(final Identifiable i)
	{
		final List<String> tags = this.stateProvider.getCurrentState(i);

		if (tags == null)
			return;

		final AnnotatedIdentifiable ai = new AnnotatedIdentifiable();
		ai.id = i;
		ai.startTimestamp = new HrsMinSecFrameTimecode(
				this.video.getCurrentFrameIndex(), this.video.getFPS());
		ai.tags = new ArrayList<String>(tags);

		this.addToDataset(ai);
	}

	/**
	 * Add an identifiable time region that will be annotated with the current
	 * state.
	 * 
	 * @param i
	 *            The identifiable
	 * @param start
	 *            The start timestamp
	 * @param end
	 *            The end timestamp
	 */
	public void updateIdentifiableRegion(final Identifiable i,
			final VideoTimecode start, final VideoTimecode end)
	{
		final List<String> tags = this.stateProvider.getCurrentState(i);

		if (tags == null)
			return;

		final AnnotatedIdentifiable ai = new AnnotatedIdentifiable();
		ai.id = i;
		ai.startTimestamp = start;
		ai.endTimestamp = end;
		ai.tags = new ArrayList<String>(tags);

		this.addToDataset(ai);
	}

	/**
	 * Add the given annotated identifiable to the dataset
	 * 
	 * @param ai
	 *            The annotated identifiable
	 */
	public void addToDataset(final AnnotatedIdentifiable ai)
	{
		this.dataset.add(ai);
		System.out.println(this.dataset);
	}

	/**
	 * Writes the created dataset to the given file
	 * 
	 * @param file
	 *            The file to write the dataset to.
	 * @throws IOException
	 *             If the file could not be written
	 */
	public void writeDataset(final File file) throws IOException
	{
		// Check if the file already exists
		if (file.exists())
			throw new FileExistsException(file);

		// Ensure that the directory exists for the file
		if (!file.getParentFile().mkdirs())
			throw new IOException("Cannot create directory " + file.getParent());

		// Write all the annotated identifiers
		final FileWriter fw = new FileWriter(file);
		for (final AnnotatedIdentifiable ai : this.dataset)
			fw.append(ai.toString() + "\n");
		fw.close();
	}

	/**
	 * Loads a dataset created using {@link #writeDataset(File)} into this
	 * class's dataset member so that annotation can be continued. Note that the
	 * identifiables that are created when reading back in are anonymous
	 * identifiable classes and are not necessarily of the same type as the
	 * identifiable which was saved. It will only include the identifier of the
	 * object retrievable with {@link Identifiable#getID()}.
	 * 
	 * @param file
	 *            The file to read from
	 * @throws IOException
	 *             If the file cannot be read
	 */
	public void loadDataset(final File file) throws IOException
	{
		if (!file.exists())
			throw new FileNotFoundException(file.getName());

		// Read the file, line-by-line
		final Scanner scanner = new Scanner(file);
		scanner.useDelimiter("\n");

		// Match each line against this pattern. This is the pattern
		// that will match the AnnotatedIdentifiable#toString() method's output
		final Pattern p = Pattern.compile("(.*)@(.*)>(.*):\\[(.*)\\]");

		// Read the file
		while (scanner.hasNext())
		{
			final String line = scanner.next();

			// Match the line against the pattern
			final Matcher m = p.matcher(line);
			if (m.find())
			{
				// Get the parts of the line
				final String id = m.group(0);
				final String start = m.group(1);
				final String end = m.group(2);
				final String[] tags = m.group(3).split(",");

				// Create a new annotated identifiable object
				final AnnotatedIdentifiable ai = new AnnotatedIdentifiable();
				ai.id = new Identifiable() {
					@Override
					public String getID() {
						return id;
					}
				};
				ai.tags = new ArrayList<String>(Arrays.asList(tags));
				ai.startTimestamp = HrsMinSecFrameTimecode.fromString(start);
				ai.endTimestamp = HrsMinSecFrameTimecode.fromString(end);

				// Add the annotated object to the dataset
				this.dataset.add(ai);
			}
		}
	}
}
