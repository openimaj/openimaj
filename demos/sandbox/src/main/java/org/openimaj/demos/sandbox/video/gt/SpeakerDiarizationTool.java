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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.openimaj.audio.AudioStream;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.demos.sandbox.video.gt.VideoGroundTruth.IdentifierProducer;
import org.openimaj.demos.sandbox.video.gt.VideoGroundTruth.StateProvider;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.processing.shotdetector.HistogramVideoShotDetector;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 * This tool provides a tool for ground-truthing video data based around people.
 * The tool implements a VideoPlayer which does face tracking and face
 * extraction. The detected faces are displayed in a window alongside a
 * classification chooser. When a classification is chosen it is remembered
 * along with the timecode at which the classification was chosen. This is shown
 * in a list which can be edited.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 10 Aug 2012
 * @version $Author$, $Revision$, $Date$
 */
public class SpeakerDiarizationTool extends JPanel implements
		StateProvider, IdentifierProducer
{
	/**
	 * A class that provides a display of the information that the tracker is
	 * tracking.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 17 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	protected class TrackerInfo extends JPanel
	{
		/** */
		private static final long serialVersionUID = 1L;

		/** The list of faces being tracked */
		private final JPanel faceList = new JPanel();

		/** Map */
		private final Map<TrackedFace, AbstractButton> map =
				new HashMap<TrackedFace, AbstractButton>();

		/** Only allow one face to be tracked */
		private final ButtonGroup faceGroup = new ButtonGroup();

		/**
		 * Default constructor
		 */
		public TrackerInfo() {
			super.setLayout(new GridBagLayout());
			super.setPreferredSize(new Dimension(600, 300));
			super.setSize(600, 300);
			this.init();
		}

		/**
		 * Initialises the widgets.
		 */
		private void init() {
			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = gbc.gridy = 1;
			gbc.weightx = gbc.weighty = 1;
			gbc.fill = GridBagConstraints.BOTH;

			// Add the list of faces
			this.faceList.setLayout(new GridLayout(-1, 1));
			this.faceList.setBackground(Color.black);
			this.add(this.faceList, gbc);

			// Add a button to force redetection
			final JButton b = new JButton("Force Redetection");
			b.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					SpeakerDiarizationTool.this.needsRedetect = true;
				}
			});
			gbc.gridy++;
			gbc.weighty = 0;
			this.add(b, gbc);
		}

		/**
		 * Set the list of faces being tracked.
		 * 
		 * @param faces
		 *            The face list
		 */
		public void setFaceList(final List<SortableTrackedFace> faces) {
			final ArrayList<TrackedFace> toRemove = new ArrayList<TrackedFace>();
			toRemove.addAll(this.map.keySet());

			// Add new faces
			for (final SortableTrackedFace face : faces) {
				if (!this.map.keySet().contains(face)) {
					// Add the face to the list as a toggle button
					final JToggleButton b = new JToggleButton(face.toString(),
							new ImageIcon(ImageUtilities.createBufferedImage(
									face.face.templateImage)));

					// Store the map from the face to the button
					this.map.put(face.face, b);

					// Add the button to the panel
					this.faceGroup.add(b);
					this.faceList.add(b);
					this.faceList.revalidate();
				}

				// Either the face is new or it's existing, so we
				// don't want to remove it - so we remove it from the
				// 'to remove' list
				toRemove.remove(face);
			}

			// Remove all the faces that have disappeared.
			for (final TrackedFace face : toRemove) {
				this.faceList.remove(this.map.get(face));
				this.faceGroup.remove(this.map.get(face));
				this.map.remove(face);
			}

			// If nothing's selected, select the first one.
			if (this.faceGroup.getSelected() == null && this.map.keySet().size() > 0)
				this.faceGroup.setSelected(this.map.values().iterator().next());
		}

		/**
		 * Returns the face that is selected.
		 * 
		 * @return The selected face
		 */
		public TrackedFace getSelectedFace() {
			final Iterator<TrackedFace> faces = this.map.keySet().iterator();
			TrackedFace f = null;
			while (faces.hasNext())
				if (this.map.get(f = faces.next()) == this.faceGroup.getSelected())
					return f;
			return null;
		}
	}

	/**
	 * Provides a comparable interface for tracked faces such that they can be
	 * sorted in a left-to-right order over the frame.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 28 Aug 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	private class SortableTrackedFace
			implements Comparable<SortableTrackedFace>, Identifiable
	{
		/** The tracked face */
		public TrackedFace face = null;

		/** The identifier of the face */
		public String identifier = "";

		/**
		 * Construct a sortable face
		 * 
		 * @param f
		 *            The face to wrap
		 */
		public SortableTrackedFace(final TrackedFace f)
		{
			this.face = f;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(final SortableTrackedFace o)
		{
			// (4,0) in the global matrix is the x-translation.
			return o.face.clm._pglobl.get(4, 0) < this.face.clm._pglobl.get(4, 0) ? 1 : 0;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.openimaj.data.identity.Identifiable#getID()
		 */
		@Override
		public String getID()
		{
			return this.identifier;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return this.identifier;
		}
	}

	/** */
	private static final long serialVersionUID = 1L;

	/** The ground truth tool */
	private VideoGroundTruth vgt = null;

	/** The face tracker we'll use in the video */
	private final CLMFaceTracker tracker = new CLMFaceTracker();

	/** The shot detector used to determine scenes within the video */
	private HistogramVideoShotDetector shotDetector = null;

	/** The frame in which the tool will be displayed */
	private JFrame frame = null;

	/** The scene counter - used in creating unique identifiers */
	private int scene = 0;

	/** The faces we're currently tracking */
	private ArrayList<SortableTrackedFace> sortedFaces = null;

	/** Whether the tracker needs to do a redetect before the next track */
	private boolean needsRedetect = false;

	private TrackerInfo trackerInfo;

	/**
	 * Initiate the speaker diarization tool.
	 * 
	 * @param video
	 *            The video to diarize
	 * @param audio
	 *            The audio to play (can be null)
	 */
	public SpeakerDiarizationTool(final Video<MBFImage> video, final AudioStream audio)
	{
		this.vgt = new VideoGroundTruth(video, audio, this, this);
		this.shotDetector = new HistogramVideoShotDetector(video);

		this.init();
	}

	/**
	 * Initialises the GUI widgets
	 */
	private void init()
	{
		this.setLayout(new GridBagLayout());

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 1;
		gbc.weightx = gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;

		this.trackerInfo = new TrackerInfo();
		this.add(this.trackerInfo, gbc);

		// Set up the video player.
		this.vgt.getVideoPlayer().setButtons(new String[] { "play", "pause" });
		this.vgt.getVideoPlayer().pause();
		this.vgt.getVideoPlayer().addVideoListener(new VideoDisplayListener<MBFImage>()
		{
			@Override
			public void beforeUpdate(final MBFImage frame)
			{
				SpeakerDiarizationTool.this.processFrame(frame);
			}

			@Override
			public void afterUpdate(final VideoDisplay<MBFImage> display)
			{
			}
		});

		// Show the video player
		final JFrame f = this.vgt.getVideoPlayer().showFrame();

		// Show the tool
		this.showFrame().setLocation(f.getLocation().x + f.getWidth(),
				f.getLocation().y);
	}

	/**
	 * This is the method that actually does most of the work.
	 * 
	 * @param frame
	 *            The frame to process
	 */
	private void processFrame(final MBFImage frame)
	{
		// Pass the frame to our shot detector to see if the shot has changed.
		this.shotDetector.processFrame(frame);

		// If we're in to a new scene, we update the scene counter
		if (this.shotDetector.wasLastFrameBoundary() || this.needsRedetect)
		{
			if (!this.needsRedetect)
				this.scene++;

			System.out.println("=========== Scene " + this.scene + " ===========");

			// Now try to find faces in the image
			this.tracker.reset();
			this.tracker.track(frame);

			// Get a list of the faces being tracked
			final List<TrackedFace> faces = this.tracker.getModelTracker().trackedFaces;

			// Created a sorted list of faces (left-to-right in the image)
			this.sortedFaces = new ArrayList<SortableTrackedFace>();
			for (int i = 0; i < faces.size(); i++)
				this.sortedFaces.add(new SortableTrackedFace(faces.get(i)));
			Collections.sort(this.sortedFaces);

			// Update the identifiers based on the position in the scene
			for (int i = 0; i < this.sortedFaces.size(); i++)
				this.sortedFaces.get(i).identifier = "Scene " + this.scene + " Face " + i;

			System.out.println(this.sortedFaces);

			this.trackerInfo.setFaceList(this.sortedFaces);
		}
		else
			// Continue to track the faces we already have.
			this.tracker.track(frame);

		// Draw the tracked model onto the frame
		this.tracker.drawModel(frame, true, true, true, true, true);
	}

	/**
	 * Shows the tool in a frame. If a frame already exists it will be made
	 * visible.
	 * 
	 * @return Returns the frame shown
	 */
	public JFrame showFrame()
	{
		if (this.frame == null)
		{
			this.frame = new JFrame();
			this.frame.add(this);
			this.frame.pack();
		}

		this.frame.setVisible(true);
		return this.frame;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.demos.sandbox.video.gt.VideoGroundTruth.IdentifierProducer#getIdentifiers()
	 */
	@Override
	public List<Identifiable> getIdentifiers()
	{
		final List<Identifiable> l = new ArrayList<Identifiable>();
		l.addAll(this.sortedFaces);
		return l;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.demos.sandbox.video.gt.VideoGroundTruth.StateProvider#getCurrentState(org.openimaj.data.identity.Identifiable)
	 */
	@Override
	public List<String> getCurrentState(final Identifiable id)
	{
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		String name = "heads1.mpeg";
		if (args.length > 0)
			name = args[0];

		final XuggleVideo xv = new XuggleVideo(new File(name));
		final XuggleAudio xa = new XuggleAudio(new File(name));
		new SpeakerDiarizationTool(xv, xa);
	}
}
