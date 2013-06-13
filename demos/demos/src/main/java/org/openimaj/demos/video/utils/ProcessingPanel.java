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
package org.openimaj.demos.video.utils;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.openimaj.demos.faces.Mustache;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

/**
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 
 * @created 28 Sep 2011
 */
public class ProcessingPanel extends JPanel
		implements VideoDisplayListener<MBFImage>
{
	/** */
	private static final long serialVersionUID = 1L;

	private boolean edgeDetect = false;
	private boolean faceDetect = false;
	private boolean faceKPDetect = false;
	private boolean moustache = false;

	private final FKEFaceDetector fkp;

	private final HaarCascadeDetector d;

	/**
	 * 
	 */
	public ProcessingPanel()
	{
		this.d = new HaarCascadeDetector(100);
		this.fkp = new FKEFaceDetector(
				HaarCascadeDetector.BuiltInCascade.frontalface_alt.load());
		this.init();
	}

	/**
	 * 
	 */
	private void init()
	{
		this.setLayout(new GridBagLayout());
		this.setBorder(BorderFactory.createTitledBorder("Processing"));

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.gridwidth = 1;

		// -----------------------------------------------------
		final JCheckBox edgeDetectButton = new JCheckBox("Edge Detect", this.edgeDetect);
		edgeDetectButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				ProcessingPanel.this.edgeDetect = edgeDetectButton.isSelected();
			}
		});
		gbc.gridy++;
		this.add(edgeDetectButton, gbc);

		// -----------------------------------------------------
		final JCheckBox faceDetectorButton = new JCheckBox("Face Detection", this.faceDetect);
		faceDetectorButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				ProcessingPanel.this.faceDetect = faceDetectorButton.isSelected();
			}
		});
		gbc.gridy++;
		this.add(faceDetectorButton, gbc);

		// -----------------------------------------------------
		final JCheckBox faceKPDetectorButton = new JCheckBox("Facial Keypoint Detection", this.faceKPDetect);
		faceKPDetectorButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				ProcessingPanel.this.faceKPDetect = faceKPDetectorButton.isSelected();
			}
		});
		gbc.gridy++;
		this.add(faceKPDetectorButton, gbc);

		// -----------------------------------------------------
		final JCheckBox moustacheButton = new JCheckBox("Add Moustaches", this.moustache);
		moustacheButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				ProcessingPanel.this.moustache = moustacheButton.isSelected();
			}
		});
		gbc.gridy++;
		this.add(moustacheButton, gbc);

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.video.VideoDisplayListener#afterUpdate(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void afterUpdate(final VideoDisplay<MBFImage> display)
	{
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.video.VideoDisplayListener#beforeUpdate(org.openimaj.image.Image)
	 */
	@Override
	public void beforeUpdate(final MBFImage frame)
	{
		if (this.edgeDetect)
			frame.processInplace(new CannyEdgeDetector());

		if (this.faceDetect)
		{
			final List<DetectedFace> faces = this.d.detectFaces(
					Transforms.calculateIntensityNTSC(frame));

			for (final DetectedFace face : faces)
			{
				final Shape transBounds = face.getBounds();
				final MBFImageRenderer renderer = frame.createRenderer();
				renderer.drawPolygon(transBounds.asPolygon(), RGBColour.RED);
			}
		}

		if (this.faceKPDetect)
		{
			final List<KEDetectedFace> faces = this.fkp.detectFaces(
					Transforms.calculateIntensityNTSC(frame));

			for (final KEDetectedFace face : faces)
			{
				final Shape transBounds = face.getBounds();
				final MBFImageRenderer renderer = frame.createRenderer();
				renderer.drawPolygon(transBounds.asPolygon(), RGBColour.RED);

				for (final FacialKeypoint kp : face.getKeypoints())
				{
					final Point2d pt = kp.position.clone();
					pt.translate((float) transBounds.minX(), (float) transBounds.minY());
					renderer.drawPoint(pt, RGBColour.GREEN, 3);
				}
			}
		}

		if (this.moustache)
			try {
				frame.internalAssign(new Mustache().addMustaches(frame));
			} catch (final IOException e) {
				e.printStackTrace();
			}
	}
}
