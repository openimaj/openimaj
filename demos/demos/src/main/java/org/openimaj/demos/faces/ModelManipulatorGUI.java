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
package org.openimaj.demos.faces;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.demos.Demo;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.Mode;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.processing.shotdetector.HistogramVideoShotDetector;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 * Provides a user interface for interacting with the parameters of the trained
 * CLM Model.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 9 Jul 2012
 * @version $Author$, $Revision$, $Date$
 */
@Demo(
		author = "David Dupplaw",
		description = "Interface for playing with the parameters of a " +
				"trained Constrained Local Model (CLM) based facial expression tracker",
		keywords = { "Constrained Local Model", "Point Distribution Model", "face", "webcam" },
		title = "CLM Face Model Playground")
public class ModelManipulatorGUI extends JPanel {
	private static final long serialVersionUID = -3302684225273947693L;

	/**
	 * Provides a panel which draws a particular model to itself.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 9 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public class ModelView extends JPanel {
		/** */
		private static final long serialVersionUID = 1L;

		/** The image of the model */
		private final MBFImage vis = new MBFImage(600, 600, 3);

		/** The face being drawn */
		private TrackedFace face = null;

		/** Reference triangles */
		private final int[][] triangles;

		/** Reference connections */
		private final int[][] connections;

		/** Colour to draw the connections */
		private final Float[] connectionColour = RGBColour.WHITE;

		/** Colour to draw the points */
		private final Float[] pointColour = RGBColour.RED;

		/** Colour to draw the mesh */
		private final Float[] meshColour = new Float[] { 0.3f, 0.3f, 0.3f };

		/** Colour to draw the bounding box */
		private final Float[] boundingBoxColour = RGBColour.RED;

		/** Whether to show mesh */
		private final boolean showMesh = true;

		/** Whether to show connections */
		private final boolean showConnections = true;

		/**
		 * Constructor
		 */
		public ModelView() {
			this.setPreferredSize(new Dimension(600, 600));

			final CLMFaceTracker t = new CLMFaceTracker();
			this.triangles = t.getReferenceTriangles();
			this.connections = t.getReferenceConnections();
			this.face = new TrackedFace(
					new Rectangle(50, -50, 500, 500),
					t.getInitialVars());
			t.initialiseFaceModel(face);

			// Centre the face in the view
			setGlobalParam(0, 10);
			setGlobalParam(4, 300);
			setGlobalParam(5, 300);

			setBackground(new Color(60, 60, 60));
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		@Override
		public void paint(Graphics g) {
			super.paint(g);

			vis.zero();

			// Draw the model to the image.
			CLMFaceTracker.drawFaceModel(vis, face, showMesh, showConnections,
					true, true, true, triangles, connections, 1, boundingBoxColour,
					meshColour, connectionColour, pointColour);

			// Draw the image to the panel
			g.drawImage(ImageUtilities.createBufferedImage(vis), 0, 0, null);
		}

		/**
		 * Number of global parameters
		 * 
		 * @return The number of global parameters
		 */
		public int getNumGlobalParams() {
			return face.clm._pglobl.getRowDimension();
		}

		/**
		 * Get the value of the given global parameter.
		 * 
		 * @param indx
		 *            The index to get
		 * @return The value
		 */
		public double getGlobalParam(int indx) {
			return face.clm._pglobl.get(indx, 0);
		}

		/**
		 * Set the given index to the value in the global params and update the
		 * face model.
		 * 
		 * @param indx
		 *            The index
		 * @param val
		 *            The new value
		 */
		public void setGlobalParam(int indx, double val) {
			// Set the parameter
			face.clm._pglobl.set(indx, 0, val);

			// Recalculate the shape.
			face.clm._pdm.calcShape2D(face.shape,
					face.clm._plocal, face.clm._pglobl);

			repaint();
		}

		/**
		 * Number of local parameters
		 * 
		 * @return The number of local parameters
		 */
		public int getNumLocalParams() {
			return face.clm._plocal.getRowDimension();
		}

		/**
		 * Get the value of the given local parameter.
		 * 
		 * @param indx
		 *            The index to get
		 * @return The value
		 */
		public double getLocalParam(int indx) {
			return face.clm._plocal.get(indx, 0);
		}

		/**
		 * Set the given index to the value in the local params and update the
		 * face model.
		 * 
		 * @param indx
		 *            The index
		 * @param val
		 *            The new value
		 */
		public void setLocalParam(int indx, double val) {
			// Set the parameter
			face.clm._plocal.set(indx, 0, val);

			// Recalculate the shape.
			face.clm._pdm.calcShape2D(face.shape,
					face.clm._plocal, face.clm._pglobl);

			repaint();
		}
	}

	/**
	 * A class that provides a display of the information that the tracker is
	 * tracking.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 17 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	protected class TrackerInfo extends JPanel {
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
			init();
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
			faceList.setLayout(new GridLayout(-1, 1));
			faceList.setBackground(Color.black);
			this.add(faceList, gbc);

			// Add a button to force redetection
			final JButton b = new JButton("Force Redetection");
			b.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					needRedetect = true;
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
		public void setFaceList(List<TrackedFace> faces) {
			final ArrayList<TrackedFace> toRemove = new ArrayList<TrackedFace>();
			toRemove.addAll(map.keySet());

			// Add new faces
			for (final TrackedFace face : faces) {
				if (!map.keySet().contains(face)) {
					// Add the face to the list as a toggle button
					final JToggleButton b = new JToggleButton(face.toString(),
							new ImageIcon(ImageUtilities.createBufferedImage(
									face.templateImage)));

					// Store the map from the face to the button
					map.put(face, b);

					// Add the button to the panel
					faceGroup.add(b);
					faceList.add(b);
					faceList.revalidate();
				}

				// Either the face is new or it's existing, so we
				// don't want to remove it - so we remove it from the
				// 'to remove' list
				toRemove.remove(face);
			}

			// Remove all the faces that have disappeared.
			for (final TrackedFace face : toRemove) {
				faceList.remove(map.get(face));
				faceGroup.remove(map.get(face));
				map.remove(face);
			}

			// If nothing's selected, select the first one.
			if (faceGroup.getSelected() == null && map.keySet().size() > 0)
				faceGroup.setSelected(map.values().iterator().next());
		}

		/**
		 * Returns the face that is selected.
		 * 
		 * @return The selected face
		 */
		public TrackedFace getSelectedFace() {
			final Iterator<TrackedFace> faces = map.keySet().iterator();
			TrackedFace f = null;
			while (faces.hasNext())
				if (map.get(f = faces.next()) == faceGroup.getSelected())
					return f;
			return null;
		}
	}

	/**
	 * A replacement for the AWT ButtonGroup class.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 17 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	protected class ButtonGroup {
		/** The buttons */
		private final List<AbstractButton> buttons = new ArrayList<AbstractButton>();

		/**
		 * Add a button
		 * 
		 * @param b
		 */
		public void add(final AbstractButton b) {
			this.buttons.add(b);
			b.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					updateButtons(b);
				}
			});
		}

		/**
		 * Remove the given button from the group.
		 * 
		 * @param b
		 *            The button to remove
		 */
		public void remove(final AbstractButton b) {
			this.buttons.remove(b);
		}

		/**
		 * Make sure only the given button is selected.
		 * 
		 * @param b
		 *            The button to select.
		 */
		private void updateButtons(AbstractButton b) {
			for (final AbstractButton button : buttons)
				button.setSelected(button == b);
		}

		/**
		 * Returns the selected button in the group.
		 * 
		 * @return The selected button in the group or null if no buttons are
		 *         selected.
		 */
		public AbstractButton getSelected() {
			for (final AbstractButton button : buttons)
				if (button.isSelected())
					return button;
			return null;
		}

		/**
		 * Sets all buttons in the group to unselected.
		 */
		public void selectNone() {
			for (final AbstractButton button : buttons)
				button.setSelected(false);
		}

		/**
		 * Set the selected button to the given one. Note that this method will
		 * select the button whether or not the button is in the button group.
		 * 
		 * @param b
		 *            The button to select
		 */
		public void setSelected(AbstractButton b) {
			b.setSelected(true);
			updateButtons(b);
		}
	}

	/** The view of the model */
	private ModelView modelView = null;

	/** The video view */
	private JFrame videoFrame = null;

	/** The tracker info */
	private TrackerInfo trackerInfo = null;

	/** The video displayer */
	private VideoDisplay<MBFImage> videoDisplay = null;

	/** The tracker used to track faces in the videos */
	private final CLMFaceTracker tracker = new CLMFaceTracker();

	/** Shot detector used to force redetects on shot changes */
	// Note that the fps isn't used, so we just give 25 as anything will do
	private final HistogramVideoShotDetector shotDetector = new HistogramVideoShotDetector(25);

	/** The global sliders */
	private final List<JSlider> globalSliders = new ArrayList<JSlider>();

	/** The local sliders */
	private final List<JSlider> localSliders = new ArrayList<JSlider>();

	// -------------------------------------------------------
	// Note that all the slider values need to be 1,000 times
	// the size of the actual value as the sliders only work
	// in integer, so we divide the slider value by 1000 to get
	// the actual value.
	// -------------------------------------------------------

	/** Tool tip label text for each of the global sliders */
	private final String[] globalLabels = new String[] {
			"Scale", "X Rotation", "Y Rotation", "Z Rotation",
			"Translate X", "Translate Y"
	};

	/** Tool tip label text for each of the local sliders */
	private final String[] localLabels = new String[] {
	};

	/** Maximum values for each of the global sliders */
	private final int[] globalMaxs = new int[] {
			20000, (int) (Math.PI * 2000), (int) (Math.PI * 2000),
			(int) (Math.PI * 2000), 1000000, 1000000
	};

	/** Minimum values for each of the global sliders */
	private final int[] globalMins = new int[] {
			0, -(int) (Math.PI * 2000), -(int) (Math.PI * 2000),
			-(int) (Math.PI * 2000), 0, 0
	};

	/** Maximum values for each of the local sliders */
	private final int[] localMaxs = new int[] {
	};

	/** Minimum values for each of the local sliders */
	private final int[] localMins = new int[] {
	};

	/** Whether to force redetect on next track */
	private boolean needRedetect = false;

	/**
	 * Default constructor
	 */
	public ModelManipulatorGUI() {
		init();
	}

	/**
	 * Display (or hide) the video frame, creating it if necessary.
	 * 
	 * @param showNotHide
	 *            TRUE for show, FALSE for hide
	 */
	private void displayVideoFrame(Video<MBFImage> video, boolean showNotHide) {
		System.out.println("displayVideoFrame( " + video + ", " + showNotHide + " )");

		// If the button was selected...
		if (showNotHide) {
			// ..and we don't yet have a video frame
			if (videoFrame == null) {
				videoFrame = new JFrame();
				videoDisplay = VideoDisplay.createVideoDisplay(video, videoFrame);
				videoDisplay.addVideoListener(new VideoDisplayListener<MBFImage>()
				{
					@Override
					public void beforeUpdate(MBFImage frame)
					{
						// Reset the tracker if the last frame was a boundary
						shotDetector.processFrame(frame);
						if (shotDetector.wasLastFrameBoundary() || needRedetect)
						{
							tracker.reset();
							needRedetect = false;
						}

						// Track the faces
						tracker.track(frame);
						final List<TrackedFace> t = tracker.getModelTracker().trackedFaces;
						if (t != null && t.size() > 0 && trackerInfo != null)
						{
							final int indx = t.indexOf(trackerInfo.getSelectedFace());
							if (indx != -1)
								trackFace(t.get(indx));
						}

						// Draw the faces onto the frame
						tracker.drawModel(frame, true, true, true, true, true);

						// Update the track info screen
						if (trackerInfo != null)
							trackerInfo.setFaceList(t);
					}

					@Override
					public void afterUpdate(VideoDisplay<MBFImage> display)
					{
					}
				});
				videoFrame.setLocation(getLocation().x, getLocation().y + getHeight());
				videoFrame.setVisible(true);

				// Create information about the face tracking
				final JFrame trackerFrame = new JFrame();
				trackerInfo = new TrackerInfo();
				trackerFrame.getContentPane().add(trackerInfo);
				trackerFrame.setLocation(videoFrame.getLocation().x + videoFrame.getWidth(),
						getLocation().y + getHeight());
				trackerFrame.pack();
				trackerFrame.setVisible(true);
			} else {
				if (videoDisplay.getVideo() != video)
					videoDisplay.changeVideo(video);
				videoFrame.setVisible(true);
			}
		} else {
			// Not selected.
			if (videoDisplay != null) {
				videoDisplay.getVideo().close();
				videoDisplay.setMode(Mode.STOP);
				videoDisplay.close();
				videoDisplay = null;
			}

			videoFrame.setVisible(false);
		}
	}

	/**
	 * Initialise the widgets
	 */
	private void init() {
		super.setLayout(new GridBagLayout());

		modelView = new ModelView();

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = gbc.weighty = 1;
		gbc.gridx = gbc.gridy = 1;

		this.add(modelView, gbc); // 1,1

		// Add a panel to put the sliders on.
		final JPanel slidersPanel = new JPanel(new GridBagLayout());

		// Add a panel that allows us to select the source of the model.
		gbc.gridx = gbc.gridy = 1;
		gbc.insets = new Insets(2, 2, 2, 2);
		final JPanel sourcePanel = new JPanel(new GridBagLayout());
		final ButtonGroup sourceGroup = new ButtonGroup();

		// Button that sets the source to model only
		final JToggleButton modelOnlyButton = new JToggleButton("Model");
		sourceGroup.add(modelOnlyButton);
		sourcePanel.add(modelOnlyButton, gbc);
		sourceGroup.setSelected(modelOnlyButton);
		modelOnlyButton.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (modelOnlyButton.isSelected())
					displayVideoFrame(null, false);
			}
		});

		// Button that sets the source to webcam
		gbc.gridy++;
		final JToggleButton webcamButton = new JToggleButton("Webcam");
		sourceGroup.add(webcamButton);
		sourcePanel.add(webcamButton, gbc);
		webcamButton.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (webcamButton.isSelected())
				{
					if (videoDisplay == null ||
							!(videoDisplay.getVideo() instanceof VideoCapture))
					{
						try
						{
							final VideoCapture vc = new VideoCapture(640, 480);
							displayVideoFrame(vc, true);
						}
						catch (final IOException e1)
						{
							JOptionPane.showMessageDialog(ModelManipulatorGUI.this,
									"Unable to instantiate the webcam");
							e1.printStackTrace();
						}
					}
				}
			}
		});

		// Button that set the source to a video file
		gbc.gridy++;
		final JToggleButton videoFileButton = new JToggleButton("Video File");
		sourceGroup.add(videoFileButton);
		sourcePanel.add(videoFileButton, gbc);
		videoFileButton.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (videoFileButton.isSelected())
				{
					final JFileChooser jfc = new JFileChooser();
					final int returnVal = jfc.showOpenDialog(ModelManipulatorGUI.this);

					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						final XuggleVideo xv = new XuggleVideo(jfc.getSelectedFile());
						displayVideoFrame(xv, true);
					}
				}
			}
		});

		// Add the source panel to the main panel
		gbc.gridx = gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		slidersPanel.add(sourcePanel, gbc);

		// Add the global settings.
		gbc.gridx = gbc.gridy = 1;
		final JPanel pGlobalSliders = new JPanel(new GridBagLayout());
		pGlobalSliders.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createTitledBorder("Pose")));
		for (int i = 0; i < modelView.getNumGlobalParams(); i++) {
			final int j = i;
			int min = 0, max = 20000;
			final int val = (int) (modelView.getGlobalParam(i) * 1000d);

			if (j < globalMins.length)
				min = globalMins[j];
			if (j < globalMaxs.length)
				max = globalMaxs[j];

			final JSlider s = new JSlider(min, max, val);
			globalSliders.add(s);
			s.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent e)
				{
					modelView.setGlobalParam(j, s.getValue() / 1000d);
				}
			});

			// Add a tooltip if we have one
			if (i < globalLabels.length && globalLabels[i] != null)
				s.setToolTipText(globalLabels[i]);

			pGlobalSliders.add(s, gbc);
			gbc.gridy++;
		}

		gbc.gridy = 2;
		slidersPanel.add(pGlobalSliders, gbc);

		// Add the local sliders
		gbc.gridy = 1;
		final JPanel pLocalSliders = new JPanel(new GridBagLayout());
		pLocalSliders.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createTitledBorder("Local")));
		for (int i = 0; i < modelView.getNumLocalParams(); i++) {
			final int j = i;
			int min = -20000, max = 20000;
			final int val = (int) (modelView.getLocalParam(i) * 1000d);

			if (j < localMins.length)
				min = localMins[j];
			if (j < localMaxs.length)
				max = localMaxs[j];

			final JSlider s = new JSlider(min, max, val);
			localSliders.add(s);
			s.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent e)
				{
					modelView.setLocalParam(j, s.getValue() / 1000d);
				}
			});

			// Add a tooltip if we have one
			if (i < localLabels.length && localLabels[i] != null)
				s.setToolTipText(localLabels[i]);

			pLocalSliders.add(s, gbc);
			gbc.gridy++;
		}

		gbc.gridy = 3;
		gbc.gridx = 1;
		slidersPanel.add(pLocalSliders, gbc);

		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.weightx = 0.5;
		this.add(slidersPanel, gbc); // 2,1
	}

	/**
	 * Makes the model track the face
	 */
	private void trackFace(TrackedFace face) {
		for (int i = 0; i < modelView.getNumGlobalParams(); i++)
			globalSliders.get(i).setValue((int) (face.clm._pglobl.get(i, 0) * 1000));

		for (int i = 0; i < modelView.getNumLocalParams(); i++)
			localSliders.get(i).setValue((int) (face.clm._plocal.get(i, 0) * 1000));
	}

	/**
	 * Main
	 * 
	 * @param args
	 *            Command-line arguments
	 */
	public static void main(String[] args) {
		final JFrame f = new JFrame("CLM Model Manipulator");
		final ModelManipulatorGUI gui = new ModelManipulatorGUI();
		f.getContentPane().add(gui);
		f.setSize(1000, gui.getPreferredSize().height);
		f.setVisible(true);
	}
}
