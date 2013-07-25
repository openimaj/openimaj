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
package org.openimaj.tools.localfeature.options;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.Transforms;

/**
 * Colour modes
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
enum ColourMode implements CmdLineOptionsProvider {
	/**
	 * Standard greylevel intensity
	 */
	INTENSITY {
		@Override
		public ColourModeOp getOptions() {
			return new Intensity();
		}
	},
	/**
	 * Find interest using intensity image, but extract features in colour
	 * 
	 */
	INTENSITY_COLOUR {
		@Override
		public ColourModeOp getOptions() {
			return new IntensityColour();
		}
	},
	/**
	 * Find & extract features in a single colour band
	 */
	SINGLE_COLOUR {
		@Override
		public ColourModeOp getOptions() {
			return new SingleColour();
		}
	};

	/**
	 * Ways of reading an image, and/or converting colours
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static abstract class ColourModeOp {
		/**
		 * Read the image from the byte array
		 * 
		 * @param img
		 *            the image bytes
		 * @return the image
		 * @throws IOException
		 */
		public abstract Image<?, ?> process(byte[] img) throws IOException;

		/**
		 * Convert the image appropriately
		 * 
		 * @param img
		 *            the image
		 * @return the image
		 */
		public abstract Image<?, ?> process(MBFImage img);
	}

	private static class Intensity extends ColourModeOp {
		@Override
		public FImage process(byte[] img) throws IOException {
			return ImageUtilities.readF(new ByteArrayInputStream(img));
		}

		@Override
		public Image<?, ?> process(MBFImage img) {
			return Transforms.calculateIntensityNTSC_LUT(img);
		}
	}

	private static class IntensityColour extends ColourModeOp {
		@Option(
				name = "--colour-conversion",
				aliases = "-cc",
				required = false,
				usage = "Optionally specify a colour space conversion")
		private ColourSpace ct = null;

		@Override
		public MBFImage process(byte[] img) throws IOException {
			MBFImage toRet = ImageUtilities.readMBF(new ByteArrayInputStream(img));
			if (ct != null)
				toRet = ct.convert(toRet);
			return toRet;
		}

		@Override
		public Image<?, ?> process(MBFImage img) {
			if (ct != null)
				img = ct.convert(img);
			return img;
		}
	}

	private static class SingleColour extends ColourModeOp {
		@Option(
				name = "--colour-conversion",
				aliases = "-cc",
				required = false,
				usage = "Optionally specify a colour space conversion")
		private ColourSpace ct = null;

		@Option(
				name = "--isolated-colour",
				aliases = "-ic",
				required = false,
				usage = "Specify the image band you wish extracted, defaults to 0")
		private int band = 0;

		@Override
		public FImage process(byte[] img) throws IOException {
			MBFImage toRet = ImageUtilities.readMBF(new ByteArrayInputStream(img));
			if (ct != null)
				toRet = ct.convert(toRet);
			return toRet.getBand(band);
		}

		@Override
		public Image<?, ?> process(MBFImage img) {
			if (ct != null)
				img = ct.convert(img);
			return img.getBand(band);
		}
	}
}
