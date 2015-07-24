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

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.pixel.Pixel;

/**
 * A connected component labeler.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ConnectedComponentLabeler implements ImageAnalyser<FImage> {
	/**
	 * Different algorithms for finding {@link ConnectedComponent}s.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public enum Algorithm {
		/**
		 * A single-pass algorithm
		 *
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 *
		 */
		SINGLE_PASS {
			@Override
			public List<ConnectedComponent> findComponents(FImage image, float bgThreshold, ConnectMode mode) {
				final List<ConnectedComponent> components = new ArrayList<ConnectedComponent>();

				// Single pass method inspired by the wikipedia two-pass
				// technique
				// http://en.wikipedia.org/wiki/Connected_component_labeling
				for (int y = 0; y < image.height; y++) {
					for (int x = 0; x < image.width; x++) {
						final float element = image.pixels[y][x];

						if (element > bgThreshold) {
							final List<Pixel> neighbours = mode.getNeighbours(image, x, y, bgThreshold);

							ConnectedComponent currentComponent = null;
							for (final Pixel p : neighbours) {
								final ConnectedComponent cc = searchPixel(p, components);
								if (cc != null) {
									if (currentComponent == null) {
										currentComponent = cc;
									} else if (currentComponent != cc) {
										currentComponent.merge(cc);
										components.remove(cc);
									}
								}
							}

							if (currentComponent == null) {
								currentComponent = new ConnectedComponent();
								components.add(currentComponent);
							}

							currentComponent.addPixel(x, y);
						}
					}
				}

				return components;
			}

			private ConnectedComponent searchPixel(Pixel p, List<ConnectedComponent> components) {
				for (final ConnectedComponent c : components) {
					if (c.find(p))
						return c;
				}
				return null;
			}
		},
		/**
		 * The standard two-pass algorithm.
		 *
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		TWO_PASS {
			@Override
			public List<ConnectedComponent> findComponents(FImage image, float bgThreshold, ConnectMode mode) {
				final List<ConnectedComponent> components = new ArrayList<ConnectedComponent>();
				final TIntIntHashMap linked = new TIntIntHashMap();
				final int[][] labels = new int[image.height][image.width];
				int nextLabel = 1;

				// first pass
				for (int y = 0; y < image.height; y++) {
					for (int x = 0; x < image.width; x++) {
						final float element = image.pixels[y][x];

						if (element > bgThreshold) {
							final List<Pixel> neighbours = mode.getNeighbours(image, x, y, bgThreshold);
							final List<Integer> L = new ArrayList<Integer>();

							for (final Pixel p : neighbours)
								if (labels[p.y][p.x] != 0)
									L.add(labels[p.y][p.x]);

							if (L.size() == 0) {
								linked.put(nextLabel, nextLabel);
								labels[y][x] = nextLabel;
								nextLabel++;
							} else {
								int min = Integer.MAX_VALUE;
								for (final int i : L)
									if (i < min)
										min = i;
								labels[y][x] = min;

								for (final int i : L) {
									merge(linked, i, min);
								}
							}
						}
					}
				}

				// second pass
				final Map<Integer, ConnectedComponent> comp = new HashMap<Integer, ConnectedComponent>();

				for (int i = 1; i <= linked.size(); i++) {
					int min = linked.get(i);

					while (true) {
						final int m = linked.get(min);

						if (m == min)
							break;
						else
							min = m;
					}
					linked.put(i, min);
				}

				for (int y = 0; y < image.height; y++) {
					for (int x = 0; x < image.width; x++) {
						if (labels[y][x] != 0) {
							final int min = linked.get(labels[y][x]);
							// labels[r][c] = min; //not needed

							if (comp.containsKey(min)) {
								comp.get(min).addPixel(x, y);
							} else {
								final ConnectedComponent cc = new ConnectedComponent();
								cc.addPixel(x, y);
								comp.put(min, cc);
							}
						}
					}
				}
				components.addAll(comp.values());

				return components;
			}

			private void merge(TIntIntHashMap linked, int start, int target) {
				if (start == target)
					return;

				final int old = linked.get(start);

				if (old > target) {
					linked.put(start, target);
					merge(linked, old, target);
				} else {
					merge(linked, target, old);
				}
			}
		},
		/**
		 * The flood-fill algorithm
		 *
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		FLOOD_FILL {
			@Override
			public List<ConnectedComponent> findComponents(FImage image, float bgThreshold, ConnectMode mode) {
				final List<ConnectedComponent> components = new ArrayList<ConnectedComponent>();
				final int[][] labels = new int[image.height][image.width];
				int nextColor = 1;

				for (int y = 0; y < image.height; y++) {
					for (int x = 0; x < image.width; x++) {
						if (image.pixels[y][x] != 0 && labels[y][x] == 0) {
							components.add(floodFill(image, new Pixel(x, y), labels, nextColor));
							nextColor++;
						}
					}
				}

				return components;
			}

			protected ConnectedComponent floodFill(FImage image, Pixel start, int[][] output, int color) {
				final ConnectedComponent cc = new ConnectedComponent();
				// Flood-fill (node, target-color, replacement-color):
				// 1. Set Q to the empty queue.
				// Queue<Pixel> queue = new LinkedList<Pixel>();
				final LinkedHashSet<Pixel> queue = new LinkedHashSet<Pixel>();

				// 2. If the color of node is not equal to target-color, return.
				if (image.pixels[start.y][start.x] == 0)
					return cc;

				// 3. Add node to Q.
				queue.add(start);

				// 4. For each element n of Q:
				while (queue.size() > 0) {
					// Pixel n = queue.poll();
					final Pixel n = queue.iterator().next();
					queue.remove(n);

					// 5. If the color of n is equal to target-color:
					if (image.pixels[n.y][n.x] != 0 && output[n.y][n.x] != color) {
						// 6. Set w and e equal to n.
						int e = n.x, w = n.x;
						// 7. Move w to the west until the color of the node to
						// the west of w no longer matches target-color.
						while (w > 0 && image.pixels[n.y][w - 1] != 0)
							w--;

						// 8. Move e to the east until the color of the node to
						// the east of e no longer matches target-color.
						while (e < image.width - 1 && image.pixels[n.y][e + 1] != 0)
							e++;

						// 9. Set the color of nodes between w and e to
						// replacement-color.
						for (int i = w; i <= e; i++) {
							output[n.y][i] = color;
							cc.addPixel(i, n.y);

							// 10. For each node n between w and e:
							final int north = n.y - 1;
							final int south = n.y + 1;
							// 11. If the color of the node to the north of n is
							// target-color, add that node to Q.
							if (north >= 0 && image.pixels[north][i] != 0 && output[north][i] != color)
								queue.add(new Pixel(i, north));
							// If the color of the node to the south of n is
							// target-color, add that node to Q.
							if (south < image.height && image.pixels[south][i] != 0 && output[south][i] != color)
								queue.add(new Pixel(i, south));
						}
						// 12. Continue looping until Q is exhausted.
					}
				}
				// 13. Return.
				return cc;
			}
		};

		/**
		 * Find the connected components in an image.
		 *
		 * @param image
		 *            the image
		 * @param bgThreshold
		 *            the threshold below which pixels should be considered to
		 *            be background
		 * @param mode
		 *            the {@link ConnectMode}.
		 * @return the connected components
		 */
		public abstract List<ConnectedComponent> findComponents(FImage image, float bgThreshold, ConnectMode mode);
	}

	protected float bgThreshold = 0;
	protected Algorithm algorithm = Algorithm.TWO_PASS;
	protected ConnectMode mode;
	protected List<ConnectedComponent> components;

	/**
	 * Construct using the default (two-pass) algorithm, background pixels
	 * having a value of 0 or less, and the given {@link ConnectMode}.
	 *
	 * @param mode
	 *            the connection mode.
	 */
	public ConnectedComponentLabeler(ConnectMode mode) {
		this.mode = mode;
	}

	/**
	 * Construct using the given algorithm, background pixels having a value of
	 * 0 or less, and the given {@link ConnectMode}.
	 *
	 * @param algorithm
	 *            the algorithm to use
	 * @param mode
	 *            the connection mode.
	 */
	public ConnectedComponentLabeler(Algorithm algorithm, ConnectMode mode) {
		this.algorithm = algorithm;
		this.mode = mode;
	}

	/**
	 * Construct using the given algorithm, background pixel threshold, and the
	 * given {@link ConnectMode}.
	 *
	 * @param algorithm
	 *            the algorithm to use
	 * @param bgThreshold
	 *            threshold at which pixels with lower values are considered to
	 *            be the background
	 * @param mode
	 *            the connection mode.
	 */
	public ConnectedComponentLabeler(Algorithm algorithm, float bgThreshold, ConnectMode mode) {
		this.algorithm = algorithm;
		this.bgThreshold = bgThreshold;
		this.mode = mode;
	}

	/**
	 * Syntactic sugar for calling {@link #analyseImage(FImage)} followed by
	 * {@link #getComponents()};
	 *
	 * @param image
	 *            the image to extract components from
	 * @return the extracted components.
	 */
	public List<ConnectedComponent> findComponents(FImage image) {
		analyseImage(image);
		return components;
	}

	@Override
	public void analyseImage(FImage image) {
		components = algorithm.findComponents(image, bgThreshold, mode);
	}

	/**
	 * @return the list of components found in the last call to
	 *         {@link #analyseImage(FImage)}.
	 */
	public List<ConnectedComponent> getComponents() {
		return components;
	}
}
