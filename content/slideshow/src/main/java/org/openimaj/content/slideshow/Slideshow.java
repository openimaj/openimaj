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
package org.openimaj.content.slideshow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;

/**
 * Implementation of a slideshow made up of {@link Slide}s. Binds the left and
 * right arrow keys to forward/backward, 'q' to quit and 'f' to toggle
 * fullscreen mode. If the current slide being displayed is also a
 * {@link KeyListener} then keypresses other than these will be passed to the
 * slide.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public abstract class Slideshow implements KeyListener {
	protected RootPaneContainer container;

	protected List<Slide> slides;
	protected int currentSlideIndex = -1;
	protected Component currentSlideComp;

	protected int slideWidth;
	protected int slideHeight;

	protected Slide currentSlide;

	private JPanel contentPanel;

	/**
	 * Default constructor.
	 *
	 * @param container
	 *            the root window
	 * @param slides
	 *            the slides
	 * @param slideWidth
	 *            the width to display the slides
	 * @param slideHeight
	 *            the height to display the slides
	 * @param background
	 *            a background image to display behind the slides (the slides
	 *            need to be transparent!)
	 * @throws IOException
	 *             if the first slide can't be loaded
	 */
	public Slideshow(RootPaneContainer container, List<Slide> slides, final int slideWidth, final int slideHeight,
			BufferedImage background) throws IOException
	{
		this.container = container;

		this.slideWidth = slideWidth;
		this.slideHeight = slideHeight;

		final BufferedImage bg;
		if (background == null) {
			bg = new BufferedImage(slideWidth, slideHeight, BufferedImage.TYPE_3BYTE_BGR);
			final Graphics2D g = bg.createGraphics();
			g.setColor(UIManager.getColor("Panel.background"));
			g.fillRect(0, 0, bg.getWidth(), bg.getHeight());
		} else {
			bg = background;
		}

		contentPanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				g.drawImage(bg, 0, 0, slideWidth, slideHeight, null);
			};
		};
		contentPanel.setOpaque(false);
		contentPanel.setSize(slideWidth, slideHeight);
		contentPanel.setPreferredSize(new Dimension(slideWidth, slideHeight));
		contentPanel.setLayout(new GridBagLayout());

		final JPanel scrollContent = new JPanel();
		scrollContent.setLayout(new GridBagLayout());
		scrollContent.setSize(contentPanel.getSize());
		scrollContent.setPreferredSize(contentPanel.getSize());
		scrollContent.add(contentPanel);
		scrollContent.setBackground(Color.BLACK);

		container.getContentPane().setBackground(Color.BLACK);

		final JScrollPane scroller = new JScrollPane(scrollContent);
		scroller.setBackground(Color.BLACK);
		scroller.setBorder(null);
		container.getContentPane().add(scroller, BorderLayout.CENTER);

		((Component) container).addKeyListener(this);

		this.slides = slides;

		displayNextSlide();
		pack();

		((Component) container).setVisible(true);
	}

	protected abstract void pack();

	/**
	 * Display the next slide
	 *
	 * @throws IOException
	 */
	public void displayNextSlide() throws IOException {
		if (currentSlideIndex < slides.size() - 1) {
			currentSlideIndex++;
			displaySlide(slides.get(currentSlideIndex));
		}
	}

	/**
	 * Display the previous slide
	 *
	 * @throws IOException
	 */
	public void displayPrevSlide() throws IOException {
		if (currentSlideIndex > 0) {
			currentSlideIndex--;
			displaySlide(slides.get(currentSlideIndex));
		}
	}

	protected void displaySlide(Slide slide) throws IOException {
		if (currentSlideComp != null) {
			contentPanel.remove(currentSlideComp);
			currentSlide.close();
		}

		currentSlide = slide;
		currentSlideComp = currentSlide.getComponent(slideWidth, slideHeight);
		currentSlideComp.setPreferredSize(new Dimension(slideWidth, slideHeight));
		currentSlideComp.setMaximumSize(new Dimension(slideWidth, slideHeight));

		contentPanel.add(currentSlideComp, new GridBagConstraints());

		currentSlideComp.setFocusable(true);
		currentSlideComp.requestFocus();
		currentSlideComp.addKeyListener(this);
		currentSlideComp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				currentSlideComp.requestFocus();
			}
		});

		contentPanel.validate();
		((Component) container).repaint();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (currentSlide instanceof KeyListener) {
			((KeyListener) currentSlide).keyTyped(e);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		try {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				displayPrevSlide();
				break;
			case KeyEvent.VK_RIGHT:
				displayNextSlide();
				break;
			case KeyEvent.VK_F:
				toggleFullscreen();
				break;
			case KeyEvent.VK_ESCAPE:
				setFullscreen(false);
				break;
			case KeyEvent.VK_Q:
				System.exit(0);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		if (currentSlide instanceof KeyListener) {
			((KeyListener) currentSlide).keyPressed(e);
		}
	}

	private void toggleFullscreen() {
		setFullscreen(!isFullscreen());
	}

	protected abstract boolean isFullscreen();

	/**
	 * Method allows changing whether this window is displayed in fullscreen or
	 * windowed mode.
	 *
	 * @param fullscreen
	 *            true = change to fullscreen, false = change to windowed
	 */
	public abstract void setFullscreen(boolean fullscreen);

	@Override
	public void keyReleased(KeyEvent e) {
		if (currentSlide instanceof KeyListener) {
			((KeyListener) currentSlide).keyReleased(e);
		}
	}
}
