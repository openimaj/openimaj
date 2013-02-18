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
package org.openimaj.image.objectdetection;

import Jama.Matrix;

/**
 * An object detection with an associated transform that maps the detection
 * shape to the image. Typically this is used by {@link ObjectDetector}s that
 * perform some form of pre-processing transform on the image (for example to
 * simulate rotations or affine warps in order to increase invariance). In these
 * cases, the transformation held by the {@link TransformedDetection} would be
 * the <b>inverse</b> of the pre-process transform.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <DETECTED_OBJECT>
 *            Type of detected object
 */
public class TransformedDetection<DETECTED_OBJECT> {
	/**
	 * The transform to be applied to the detected object to map it to the image
	 * in which the detection was made.
	 */
	public Matrix transform;

	/**
	 * The object that was detected
	 */
	public DETECTED_OBJECT detected;

	/**
	 * Construct a new {@link TransformedDetection} with the given detected
	 * object and transform.
	 * 
	 * @param detected
	 *            the detected object
	 * @param transform
	 *            the transform
	 */
	public TransformedDetection(DETECTED_OBJECT detected, Matrix transform) {
		this.detected = detected;
		this.transform = transform;
	}
}
