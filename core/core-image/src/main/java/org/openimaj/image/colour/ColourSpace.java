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
package org.openimaj.image.colour;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

/**
 * 	Different colour space types with conversion methods.
 * 
 * 	@author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public enum ColourSpace {
	/**
	 * RGB colour space
	 */
	RGB {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return input;
		}

		@Override
		public int getNumBands() {
			return 3;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			return input;
		}
	},
	/**
	 * HSV colour space
	 */
	HSV {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.RGB_TO_HSV(input);
		}

		@Override
		public int getNumBands() {
			return 3;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			return Transforms.HSV_TO_RGB(input);
		}
	},
	/**
	 * HSI colour space
	 */
	HSI {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.RGB_TO_HSI(input);
		}

		@Override
		public int getNumBands() {
			return 3;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			throw new UnsupportedOperationException("colour transform not implemented");
		}
	},
	/**
	 * H2SV colour space
	 * @see Transforms#RGB_TO_H2SV
	 */
	H2SV {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.RGB_TO_H2SV(input);
		}

		@Override
		public int getNumBands() {
			return 4;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			return Transforms.HSV_TO_RGB(Transforms.H2SV_TO_HSV_Simple(input));
		}
	},
	/**
	 * H2SV_2 colour space
	 * @see Transforms#RGB_TO_H2SV_2
	 */
	H2SV_2 {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.RGB_TO_H2SV_2(input);
		}

		@Override
		public int getNumBands() {
			return 4;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			return Transforms.HSV_TO_RGB(Transforms.H2SV2_TO_HSV_Simple(input));
		}
	},
	/**
	 * H2S colour space
	 * @see Transforms#RGB_TO_H2S
	 */
	H2S {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.RGB_TO_H2S(input);
		}

		@Override
		public int getNumBands() {
			return 3;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			throw new UnsupportedOperationException("colour transform not implemented");
		}
	},
	/**
	 * H2S_2 colour space
	 * @see Transforms#RGB_TO_H2S_2
	 */
	H2S_2 {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.RGB_TO_H2S_2(input);
		}

		@Override
		public int getNumBands() {
			return 3;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			throw new UnsupportedOperationException("colour transform not implemented");
		}
	},
	/**
	 * LUMINANCE colour space from averaging RGB
	 */
	LUMINANCE_AVG {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return new MBFImage(this, Transforms.calculateIntensity(input));
		}

		@Override
		public int getNumBands() {
			return 1;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			return new MBFImage(input.bands.get(0).clone(), input.bands.get(0).clone(), input.bands.get(0).clone());
		}
	},
	/**
	 * LUMINANCE colour space using NTSC perceptual weightings
	 */
	LUMINANCE_NTSC {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return new MBFImage(this, Transforms.calculateIntensityNTSC(input));
		}

		@Override
		public int getNumBands() {
			return 1;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			return new MBFImage(input.bands.get(0).clone(), input.bands.get(0).clone(), input.bands.get(0).clone());
		}
	},
	/**
	 * Hue colour space
	 */
	HUE {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return new MBFImage(this, Transforms.calculateHue(input));
		}

		@Override
		public int getNumBands() {
			return 1;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			return new MBFImage(input.bands.get(0).clone(), input.bands.get(0).clone(), input.bands.get(0).clone());
		}
	},
	/**
	 * Saturation colour space
	 */
	SATURATION {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return new MBFImage(this, Transforms.calculateSaturation(input));
		}

		@Override
		public int getNumBands() {
			return 1;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			return new MBFImage(input.bands.get(0).clone(), input.bands.get(0).clone(), input.bands.get(0).clone());
		}
	},
	/**
	 * Intensity normalised RGB colour space using normalisation
	 */
	RGB_INTENSITY_NORMALISED {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.RGB_TO_RGB_NORMALISED(input);
		}

		@Override
		public int getNumBands() {
			return 3;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			return input;
		}
	}, 
	/**
	 * A custom (unknown) colour space
	 */
	CUSTOM {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			throw new UnsupportedOperationException("Cannot convert to the custom color-space");
		}

		@Override
		public int getNumBands() {
			return 1;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			throw new UnsupportedOperationException("colour transform not implemented");
		}
	}, 
	/**
	 * RGB with alpha colour space
	 */
	RGBA {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return new MBFImage(input.bands.get(0), input.bands.get(1), input.bands.get(2), new FImage(input.bands.get(0).width, input.bands.get(0).height));
		}

		@Override
		public int getNumBands() {
			return 4;
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			return new MBFImage(input.bands.get(0).clone(), input.bands.get(1).clone(), input.bands.get(2).clone());
		}
	}, 
	/**
	 * HSL colour space 
	 */
	HSL {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.RGB_TO_HSL(input);
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			throw new UnsupportedOperationException("colour transform not implemented");
		}

		@Override
		public int getNumBands() {
			return 3;
		}
	}, 
	/**
	 * HSY colour space 
	 */
	HSY {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.RGB_TO_HSY(input);
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			throw new UnsupportedOperationException("colour transform not implemented");
		}

		@Override
		public int getNumBands() {
			return 3;
		}
	}, 
	/**
	 * HS colour space 
	 */
	HS {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.RGB_TO_HS(input);
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			throw new UnsupportedOperationException("colour transform not implemented");
		}

		@Override
		public int getNumBands() {
			return 2;
		}
	},
	/**
	 * HS_2 colour space 
	 */
	HS_2 {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.RGB_TO_HS_2(input);
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			throw new UnsupportedOperationException("colour transform not implemented");
		}

		@Override
		public int getNumBands() {
			return 2;
		}
	}, 
	/**
	 * H1H2 colour space (two component hue)
	 * @see Transforms#H_TO_H1H2 
	 */
	H1H2 {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.H_TO_H1H2(Transforms.calculateHue(input));
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			throw new UnsupportedOperationException("colour transform not implemented");
		}

		@Override
		public int getNumBands() {
			return 2;
		}
	},
	/**
	 * H1H2_2 colour space (two component hue)
	 * @see Transforms#H_TO_H1H2_2
	 */
	H1H2_2 {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.H_TO_H1H2_2(Transforms.calculateHue(input));
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			throw new UnsupportedOperationException("colour transform not implemented");
		}

		@Override
		public int getNumBands() {
			return 2;
		}
	},
	/**
	 * CIE_XYZ color space, using the same transform as in OpenCV,
	 * which in turn came from:
	 * http://www.cica.indiana.edu/cica/faq/color_spaces/color.spaces.html
	 */
	CIE_XYZ {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.RGB_TO_CIEXYZ(input);
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			return Transforms.CIEXYZ_TO_RGB(input);
		}

		@Override
		public int getNumBands() {
			return 3;
		}
	},
	/**
	 * CIE_Lab color space, using the same transform as in OpenCV,
	 * which in turn came from:
	 * http://www.cica.indiana.edu/cica/faq/color_spaces/color.spaces.html
	 */
	CIE_Lab {
		@Override
		public MBFImage convertFromRGB(MBFImage input) {
			return Transforms.RGB_TO_CIELab(input);
		}

		@Override
		public MBFImage convertToRGB(MBFImage input) {
			return Transforms.CIELab_TO_RGB(input);
		}

		@Override
		public int getNumBands() {
			return 3;
		}
	}
	;
	
	/**
	 * Convert the given RGB image to the current colour space
	 * @param input RGB image
	 * @return image in the current colour space
	 */
	public abstract MBFImage convertFromRGB(MBFImage input);
	
	/**
	 * Convert the image in this color space to RGB
	 * @param input image in this colour space
	 * @return RGB image
	 */
	public abstract MBFImage convertToRGB(MBFImage input);
	
	/**
	 * Convert the image to this colour space 
	 * @param input an image
	 * @return image in this colour space
	 */
	public MBFImage convert(MBFImage input) {
		return convertFromRGB(input.getColourSpace().convertToRGB(input));
	}
	
	/**
	 * Convert the image to the given colour space
	 * @param image the image
	 * @param cs the target colour space
	 * @return the converted image
	 */
	public static MBFImage convert(MBFImage image, ColourSpace cs) {
		return cs.convertFromRGB(image.colourSpace.convertToRGB(image));
	}
	
	/**
	 * Get the number of bands required by this colour space
	 * @return the number of bands 
	 */
	public abstract int getNumBands();
}
