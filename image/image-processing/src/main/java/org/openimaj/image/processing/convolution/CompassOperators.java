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
package org.openimaj.image.processing.convolution;

import org.openimaj.image.FImage;

/**
 *	Compass operators that are used by the Liu and Samarabandu
 *	text extraction.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 9 Aug 2011
 *	
 */
public class CompassOperators
{
	/**
	 * The Compass0 operator
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 */
	static public class Compass0 extends FConvolution
	{
		/**
		 * Default constructor
		 */
		public Compass0()
		{
			super( new FImage( new float[][]{
					{-1,-1,-1},
					{ 2, 2, 2},
					{-1,-1,-1}
			}));
		}
	}

	/**
	 * The Compass45 operator
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 */
	static public class Compass45 extends FConvolution
	{
		/**
		 * Default constructor
		 */
		public Compass45()
		{
			super( new FImage( new float[][]{
					{-1,-1, 2},
					{-1, 2,-1},
					{ 2,-1,-1}
			}));
		}
	}

	/**
	 * The Compass90 operator
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 */
	static public class Compass90 extends FConvolution
	{
		/**
		 * Default constructor
		 */
		public Compass90()
		{
			super( new FImage( new float[][]{
					{-1, 2,-1},
					{-1, 2,-1},
					{-1, 2,-1}
			}));
		}
	}

	/**
	 * The Compass135 operator
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 */
	static public class Compass135 extends FConvolution
	{
		/**
		 * Default constructor
		 */
		public Compass135()
		{
			super( new FImage( new float[][]{
					{ 2,-1,-1},
					{-1, 2,-1},
					{-1,-1, 2}
			}));
		}
	}
}
