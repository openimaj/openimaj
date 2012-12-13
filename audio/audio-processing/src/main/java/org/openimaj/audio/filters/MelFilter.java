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
package org.openimaj.audio.filters;

import org.openimaj.audio.util.AudioUtils;

/**
 *	A Mel triangular filter for frequency spectrum. The class is constructed
 *	using linear frequencies (Hz) to give the start and end points of the filter.
 *	These are converted into Mel frequencies (non-linear) so that the triangle
 *	which is Isosceles in Mel frequency is non-linear in the linear frequency
 *	(stretched towards the higher frequencies).
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 25 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class MelFilter extends TriangularFilter
{
	/**
	 * 	The Mel Filter default constructor.
	 *
	 *	@param startFreq The start frequency of the filter
	 *	@param endFreq The end frequency of the filter
	 */
	public MelFilter( final double startFreq, final double endFreq )
	{
		// We need to work out the centre frequency in Mel terms. We work out the
		// centre frequency in the Mel scale then convert back to linear frequency.
		super( startFreq, AudioUtils.melFrequencyToFrequency(
			(AudioUtils.frequencyToMelFrequency( endFreq ) +
			 AudioUtils.frequencyToMelFrequency( startFreq )) /2d), endFreq );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "mf{"+this.lowFrequency+"->"+this.centreFrequency+"->"+this.highFrequency+"}";
	}
}
