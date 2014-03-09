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
package org.openimaj.image.segmentation;

import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler.Algorithm;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.processor.Processor;

/**
 * Simple wrapper to make thresholding algorithms into {@link Segmenter}s by
 * applying the thresholding operation and then applying connected component
 * labeling. This class will produce components for both the foreground and
 * background elements of thresholded input image.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ConnectedThresholdSegmenter extends ThresholdSegmenter {
	private ConnectMode mode;
	private Algorithm algorithm;

	/**
	 * Construct with the given thresholding algorithm implementation and
	 * connection mode.
	 * 
	 * @param thresholder
	 *            the thresholding algorithm
	 * @param mode
	 *            the connection mode
	 */
	public ConnectedThresholdSegmenter(Processor<FImage> thresholder, ConnectMode mode) {
		this(thresholder, ConnectedComponentLabeler.Algorithm.TWO_PASS, mode);
	}

	/**
	 * Construct with the given thresholding algorithm implementation.
	 * 
	 * @param thresholder
	 *            the thresholding algorithm
	 * @param algorithm
	 *            the connected component labeling algorithm to use
	 * @param mode
	 *            the connection mode
	 */
	public ConnectedThresholdSegmenter(Processor<FImage> thresholder, Algorithm algorithm, ConnectMode mode) {
		super(thresholder);
		this.mode = mode;
		this.algorithm = algorithm;
	}

	@Override
	public List<ConnectedComponent> segment(FImage image) {
		final FImage timg = image.process(thresholder);

		return algorithm.findComponents(timg, 0, mode);
	}
}
