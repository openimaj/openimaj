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
package org.openimaj.image.analysis.watershed;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.watershed.event.ComponentStackMergeListener;
import org.openimaj.image.analysis.watershed.feature.ComponentFeature;
import org.openimaj.image.pixel.IntValuePixel;

/**
 * Maximally Stable Extremal Region watershed algorithm, implemented as
 * described in the Microsoft paper of Nister and Stewenius.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class WatershedProcessorAlgorithm
{
	/**
	 * A sorted heap of pixels. When {@link #pop()} is called the lowest value
	 * pixel is returned first.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @author Jonathon Hare (dpd@ecs.soton.ac.uk)
	 *
	 */
	private class BoundaryHeap
	{
		private BitSet availablePixels;
		private ArrayDeque<IntValuePixel>[] stacks;

		/**
		 * Construct a boundary heap object with a given number of levels (i.e.
		 * max value of a pixel).
		 *
		 * @param sz
		 *            number of levels.
		 */
		@SuppressWarnings("unchecked")
		public BoundaryHeap(int sz) {
			availablePixels = new BitSet(sz);
			stacks = new ArrayDeque[sz];

			for (int i = 0; i < sz; i++)
				stacks[i] = new ArrayDeque<IntValuePixel>();
		}

		/**
		 * Pushes the pixel onto the heap.
		 *
		 * @param p
		 *            The {@link IntValuePixel} to push onto the heap.
		 */
		public void push(IntValuePixel p)
		{
			final ArrayDeque<IntValuePixel> l = stacks[p.value];
			l.push(p);
			availablePixels.set(p.value);
		}

		/**
		 * Returns the lowest available pixel off the heap (removing it from the
		 * heap). Pixels are returned in sorted order (lowest value first). The
		 * method will return null if the heap is empty.
		 *
		 * @return The lowest value available pixel or NULL if no pixels are
		 *         available.
		 */
		public IntValuePixel pop()
		{
			final int l = availablePixels.nextSetBit(0);
			if (l == -1)
				return null; // Null means no available pixels (empty heap)

			final IntValuePixel xx = this.stacks[l].pop();
			if (this.stacks[l].size() == 0)
				availablePixels.set(l, false);
			return xx; // lowest and newest pixel
		}
	}

	/** The pixel where the pour will start */
	private IntValuePixel startPixel = null;

	/** The mask that shows which pixels have been visited */
	private BitSet accessibleMask = null;

	/** The current pixel being investigated */
	private IntValuePixel currentPixel = null;

	/** The stack of components during processing */
	private ArrayDeque<Component> componentStack = null;

	/** The boundary heap during processing */
	private BoundaryHeap boundaryHeap = null;

	/** The image being processed */
	private int[][] greyscaleImage = null;

	/**
	 * The listeners for this watershed process. They will be called as regions
	 * are detected
	 */
	private List<ComponentStackMergeListener> csmListeners = null;

	private Class<? extends ComponentFeature>[] featureClasses;

	/**
	 * Default constructor
	 *
	 * @param greyscaleImage
	 *            the image as a 2d array of integer values
	 * @param startPixel
	 *            The pixel to start the process at
	 * @param featureClasses
	 *            the features that should be created for each detected
	 *            component
	 */
	@SafeVarargs
	public WatershedProcessorAlgorithm(int[][] greyscaleImage, IntValuePixel startPixel,
			Class<? extends ComponentFeature>... featureClasses)
	{
		this.greyscaleImage = greyscaleImage;
		this.startPixel = startPixel;
		this.csmListeners = new ArrayList<ComponentStackMergeListener>();

		this.featureClasses = featureClasses;
	}

	/**
	 * Default constructor
	 *
	 * @param bGreyscaleImage
	 *            the image to apply the watershed transform too
	 * @param startPixel
	 *            The pixel to start the process at
	 * @param featureClasses
	 *            the features that should be created for each detected
	 *            component
	 */
	@SafeVarargs
	public WatershedProcessorAlgorithm(FImage bGreyscaleImage, IntValuePixel startPixel,
			Class<? extends ComponentFeature>... featureClasses)
	{
		this(new int[bGreyscaleImage.getHeight()][bGreyscaleImage.getWidth()], startPixel, featureClasses);

		for (int j = 0; j < bGreyscaleImage.getHeight(); j++) {
			for (int i = 0; i < bGreyscaleImage.getWidth(); i++) {
				greyscaleImage[j][i] = (int) (bGreyscaleImage.pixels[j][i] * 255);
			}
		}
	}

	/**
	 * Start the detection process by pouring on water at the pour point. (part
	 * 1 and 2)
	 *
	 */
	public void startPour()
	{
		// For each step on the downhill stream is created as
		// a component.
		this.currentPixel = startPixel;

		// Store the grey level of the current pixel
		this.currentPixel.value = greyscaleImage[this.startPixel.y][this.startPixel.x];

		// Create the mask the shows where the water has access to
		this.accessibleMask = new BitSet(this.greyscaleImage.length * this.greyscaleImage[0].length);

		// Create the stack of components
		this.componentStack = new ArrayDeque<Component>();

		// Create the heap of boundary pixels
		this.boundaryHeap = new BoundaryHeap(256);

		// Create a dummy component with a greylevel higher than
		// any allowed and push it onto the stack
		final Component dummyComponent = new Component(new IntValuePixel(-1, -1, Integer.MAX_VALUE), featureClasses);
		this.componentStack.push(dummyComponent);

		// Continue the processing at the first pixel
		this.processNeighbours();

		// System.err.println("Component Stack: "+componentStack );
	}

	/**
	 * Process the current pixel's neighbours (part 4, 5, 6 and 7).
	 */
	private void processNeighbours()
	{
		// Push an empty component with the current level
		// onto the component stack
		Component currentComponent = new Component(this.currentPixel, featureClasses);
		componentStack.push(currentComponent);

		// System.err.println( "Processing neighbours of "+currentPixel );

		final boolean processNeighbours = true;
		while (processNeighbours)
		{
			boolean toContinue = false;

			// Get all the neighbours of the current pixel
			final IntValuePixel[] neighbours = getNeighbourPixels_4(this.currentPixel);
			// System.err.println("Neighbours: "+outputArray( neighbours ) );

			// For each of the neighbours, check if the the neighbour
			// is already accessible.
			for (final IntValuePixel neighbour : neighbours)
			{
				if (neighbour == null)
					break; // neighbours array is packed, so nulls only occur at
				// the end

				final int idx = neighbour.x + neighbour.y * this.greyscaleImage[0].length;

				// If the neighbour is not accessible...
				if (!this.accessibleMask.get(idx))
				{
					// Mark it as accessible...
					this.accessibleMask.set(idx);
					// System.err.println("Making "+neighbour+" accessible" );

					// If its greylevel is not lower than the current one...
					if (neighbour.value >= currentPixel.value)
					{
						// Push it onto the heap of boundary pixels
						this.boundaryHeap.push(neighbour);
						// System.err.println("1. Push "+neighbour+", = "+boundaryHeap
						// );
					}
					// If, on the other hand, the greylevel is lower
					// than the current one, enter the current pixel
					// back into the queue of boundary pixels for later
					// processing (with the next edge number), consider
					// the new pixel and process it
					// (this is the water pouring into the local minimum)
					else
					{
						this.boundaryHeap.push(currentPixel);
						// System.err.println("2. Push "+currentPixel+", = "+boundaryHeap
						// );
						this.currentPixel = neighbour;
						currentComponent = new Component(this.currentPixel, featureClasses);
						componentStack.push(currentComponent);
						toContinue = true;
						break;
					}
				}
			}

			if (toContinue)
				continue;

			// Accumulate the current pixel to the component at the top of the
			// stack. (part 5)
			this.componentStack.peek().accumulate(this.currentPixel);
			// System.err.println("Added "+currentPixel+" to top component "+componentStack.peek()
			// );

			// Pop the heap of boundary pixels. (part 6)
			final IntValuePixel p = this.boundaryHeap.pop();
			// System.err.println("Popped "+p+", = "+boundaryHeap );

			// If the heap is empty, then we're done
			if (p == null)
				return;

			// If it's at the same grey-level we process its neighbours (part 6)
			if (p.value == currentPixel.value)
			{
				this.currentPixel = p;
			}
			// If it's at a higher grey-level we must process the components in
			// the stack (part 7)
			else
			{
				this.currentPixel = p;
				processComponentStack();
			}
		}
	}

	private void processComponentStack()
	{
		while (this.currentPixel.value > this.componentStack.peek().pivot.value)
		{
			// System.err.println( "Processing stack: "+componentStack );

			// If the second component on the stack has a greater
			// grey-level than the pixel, we set the component's grey-level
			// to that of the pixel and quit...
			final Component topOfStack = this.componentStack.pop();

			// System.err.println( "Top of stack gl: "+topOfStack.greyLevel );
			// System.err.println(
			// "Second stack gl: "+componentStack.peek().greyLevel );
			// System.err.println( "Pixel greylevel: "+currentPixel.value );

			if (this.currentPixel.value < this.componentStack.peek().pivot.value)
			{
				topOfStack.pivot = this.currentPixel;
				this.componentStack.push(topOfStack);

				fireComponentStackMergeListener(componentStack.peek());

				return;
			}

			fireComponentStackMergeListener(componentStack.peek(), topOfStack);

			// Otherwise...
			// Join the pixel lists
			this.componentStack.peek().merge(topOfStack);

			// TODO: histories of components
		}
	}

	/**
	 * Returns the neighbouring pixels for a given pixel with 4-connectedness.
	 * If the pixel lies outside of the image the result will be null.
	 *
	 * @param pixel
	 *            The pixel to find the neighbours of
	 * @return An array of pixels some of which may be null if they lie outside
	 *         of the image boundary.
	 */
	private IntValuePixel[] getNeighbourPixels_4(IntValuePixel pixel)
	{
		final IntValuePixel[] p = new IntValuePixel[4];
		final int x = pixel.x;
		final int y = pixel.y;

		final int height = this.greyscaleImage.length;
		final int width = this.greyscaleImage[0].length;

		// Find the pixels
		int c = 0;

		if (x < width - 1)
			p[c++] = new IntValuePixel(x + 1, y, greyscaleImage[y][x + 1]);

		if (x > 0)
			p[c++] = new IntValuePixel(x - 1, y, greyscaleImage[y][x - 1]);

		if (y < height - 1)
			p[c++] = new IntValuePixel(x, y + 1, greyscaleImage[y + 1][x]);

		if (y > 0)
			p[c++] = new IntValuePixel(x, y - 1, greyscaleImage[y - 1][x]);

		return p;
	}

	/**
	 * Add a component stack merge listener
	 *
	 * @param csml
	 *            The {@link ComponentStackMergeListener} to add
	 */
	public void addComponentStackMergeListener(ComponentStackMergeListener csml)
	{
		csmListeners.add(csml);
	}

	/**
	 * Removes the given {@link ComponentStackMergeListener} from the listeners
	 * list.
	 *
	 * @param csml
	 *            The {@link ComponentStackMergeListener} to remove
	 */
	public void removeComponentStackMergeListener(ComponentStackMergeListener csml)
	{
		csmListeners.remove(csml);
	}

	/**
	 * Fire the component stack merge listener event for the merging of two
	 * components.
	 *
	 * @param c1
	 *            The first component
	 * @param c2
	 *            The second component
	 */
	private void fireComponentStackMergeListener(Component c1, Component c2)
	{
		for (final ComponentStackMergeListener csm : csmListeners)
			csm.componentsMerged(c1, c2);
	}

	/**
	 * Fire the component stack merge listener event for the upward merge of a
	 * component.
	 *
	 * @param c1
	 *            The component that has been promoted to a higher intensity
	 *            level
	 */
	private void fireComponentStackMergeListener(Component c1)
	{
		for (final ComponentStackMergeListener csm : csmListeners)
			csm.componentPromoted(c1);
	}

	/**
	 * Helper function for debugging arrays
	 *
	 * @param o
	 * @return
	 */
	@SuppressWarnings("unused")
	private String outputArray(Object[] o)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		boolean first = true;
		for (final Object obj : o)
		{
			if (!first)
				sb.append(",");
			if (obj == null)
				sb.append("null");
			else
				sb.append(obj.toString());
			first = false;
		}
		sb.append("]");
		return sb.toString();
	}
}
