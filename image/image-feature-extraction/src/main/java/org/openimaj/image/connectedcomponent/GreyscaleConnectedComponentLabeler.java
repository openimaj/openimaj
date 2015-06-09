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
package org.openimaj.image.connectedcomponent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;

/**
 * A Connected Component Labeler for grey-level images. This class can be
 * used to transform an image that represents a map of labeled objects
 * into a list of {@link ConnectedComponent}s.  
 * <p>
 * Internally we use a flood-fill approach to finding the {@link ConnectedComponent}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class GreyscaleConnectedComponentLabeler implements ImageAnalyser<FImage> {
	private static final boolean D = true; //Debug?
	private static final String TAG = ConnectedComponentLabeler.class.getSimpleName();

	List<ConnectedComponent> components;

	private static int addedToQueueCount = 0;
	
	/**
	 * Syntactic sugar for calling {@link #analyseImage(FImage)} followed by 
	 * {@link #getComponents()};
	 *  
	 * @param image the image to extract components from
	 * @return the extracted components.
	 */
	public List<ConnectedComponent> findComponents(FImage image) {
		analyseImage(image);
		return components;
	}
	
	/**
	 * Efficiently Creates a ConnectedComponent. The previous algorithm would add excessive numbers of pixels when
	 * there were horizontal lines of targetColour. 
	 * @param image The image to extract the ConnectedComponent from
	 * @param start The pixel to start from. Pixels adjacent to this pixel (using the Connect 4 rule) of the same 
	 * color are added to the connected component. this process repeats with each pixel added until no additional 
	 * pixels are found.
	 * @param output Array representation of this component. Does not appear to be used.
	 * @param color the color to assign to the output array.
	 * @return The pixels that make up the ConnectedComponent.
	 */
	protected ConnectedComponent floodFill(FImage image, Pixel start, int[][] output, int color) {
		ConnectedComponent cc = new ConnectedComponent();
//		Flood-fill (node, target-color, replacement-color):
//			 1. Set Q to the empty queue.
		//Queue<Pixel> queue = new LinkedList<Pixel>();
		LinkedHashSet<Pixel> queue = new LinkedHashSet<Pixel>();
		
//			 2. If the color of node is not equal to target-color, return.
		float targetColour = image.pixels[start.y][start.x];
		
//		// 3. Add node to Q.
		queue.add(start);
		if(D) addedToQueueCount++;

		// 4. For each element n of Q:
		while (queue.size() > 0) {
			// Pixel n = queue.poll();
			Pixel n = queue.iterator().next();
			queue.remove(n);

			// 5. Set the color of node to replacement-color.
			output[n.y][n.x] = color;
			cc.addPixel(n.x, n.y);
			
			int west = n.x - 1;
			int east = n.x + 1;

			// 6. If the color of the node to the west matches target-color add
			// it to the queue
			if (n.x > 0 && image.pixels[n.y][west] == targetColour && output[n.y][west] != color) {
				queue.add(new Pixel(west, n.y));
				if(D) addedToQueueCount++;
			}

			// 7. If the color of the node to the east matches target-color add
			// it to the queue
			if (n.x < image.width - 1 && image.pixels[n.y][east] == targetColour && output[n.y][east] != color) {
				queue.add(new Pixel(east, n.y));
				if(D) addedToQueueCount++;
			}

			// 8. Check above and below - 4 connected model
			int north = n.y - 1;
			int south = n.y + 1;

			// 9. If the color of the node to the north of n is
			// target-color, add that node to Q.
			if (north >= 0 && image.pixels[north][n.x] == targetColour && output[north][n.x] != color) {
				queue.add(new Pixel(n.x, north));
				if(D) addedToQueueCount++;
			}
			// If the color of the node to the south of n is
			// target-color, add that node to Q.
			if (south < image.height && image.pixels[south][n.x] == targetColour && output[south][n.x] != color) {
				queue.add(new Pixel(n.x, south));
				if(D) addedToQueueCount++;
			}

		}
		// 10. Return.
		return cc;
	}
	
	@Override
	public void analyseImage(FImage image) {
		components = new ArrayList<ConnectedComponent>();
		
		int [][] labels = new int[image.height][image.width];
		int nextColor=1;
		
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				if (labels[y][x] == 0) {
					components.add(floodFill(image, new Pixel(x, y), labels, nextColor));
					nextColor++;
				}
			}
		}
		
		if(D) System.out.println(TAG + ".analyseImage - Tested " + addedToQueueCount + " pixels.");
	}
	
	/**
	 * @return the list of components found in the last call to {@link #analyseImage(FImage)}.
	 */
	public List<ConnectedComponent> getComponents() {
		return components;
	}
}
