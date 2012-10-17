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
package org.openimaj.examples.citation;

import org.openimaj.OpenIMAJ;
import org.openimaj.aop.classloader.ClassLoaderTransform;
import org.openimaj.citation.ReferenceListener;
import org.openimaj.citation.ReferencesClassTransformer;
import org.openimaj.citation.annotation.output.StandardFormatters;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;

/**
 * Example showing how to use the byte-code translating classloader to augment a
 * program at runtime in order to extract references. See the <a
 * href="http://blog.openimaj.org/2012/08/28/adding-references-to-code/"
 * >blog-post</a> for more details.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ReferencesClassloaderExample {
	/**
	 * Main method
	 * 
	 * @param args
	 *            ignored
	 * @throws Throwable
	 *             if an error occurs
	 */
	public static void main(String[] args) throws Throwable {
		// the classloader must be initialised before any other classes are used
		if (ClassLoaderTransform.run(ReferencesClassloaderExample.class, args, new ReferencesClassTransformer()))
			return;

		// now we run some code that contains references
		final FImage image = ImageUtilities.readF(OpenIMAJ.getLogoAsStream());

		final DoGSIFTEngine engine = new DoGSIFTEngine();
		for (final Keypoint keypoint : engine.findFeatures(image)) {
			System.out.println(keypoint);
		}

		// and when we're done we print the collected citations
		System.out.println(StandardFormatters.STRING.format(ReferenceListener.getReferences()));
	}
}
