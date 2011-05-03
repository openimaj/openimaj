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
package org.openimaj.image.processing.face;

import java.util.List;

/**
 *	An interface for objects that can recognise instances of something
 *	from a given representation. The interface provides methods for
 *	the training of the recogniser. The class is generic can the template
 *	variable provides the type of object which this object recogniser will
 *	recognise.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 */
public interface ObjectRecogniser<T>
{
	/**
	 * 	Add the given representation as a depiction
	 * 	of the given object.
	 * 
	 *	@param o The object depicted in the representation
	 *	@param r The Representation that is a depiction of the person.
	 */
	public void addRepresentation( T o, Representation r );
	
	/**
	 * 	Verify that the given representation is a depiction
	 * 	of the object o.
	 * 
	 *	@param o The object to check against
	 *	@param r The {@link Representation} to verify
	 *	@return
	 */
	public boolean verify( T o, Representation r );
	
	/**
	 * 	Finds the nearest object to the given representation.
	 * 
	 *	@param r The {@link Representation} to check against 
	 *	@return The nearest object, T
	 */
	public T findNearest( Representation r );
	
	/**
	 * 	Returns a list of up to <code>limit</code> length
	 * 	that contains an ordered set of <code>T</code> object
	 * 	that are nearest to the given representation.
	 * 
	 *	@param r The representation to find
	 *	@param limit The maximum number of results to return
	 *	@return
	 */
	public List<RankedObject<T>> find( Representation r, int limit );

}
