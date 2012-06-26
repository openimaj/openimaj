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
package org.openimaj.citation.annotation;

/**
 * Standard BibTeX types.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public enum ReferenceType {
	/**
	 * An article from a journal or magazine.
	 */
	Article,
	
	/**
	 * A book with an explicit publisher. 
	 */
	Book,
	
	/**
	 * A work that is printed and bound, but without a named publisher or sponsoring institution.
	 */
	Booklet,
	
	/**
	 * A part of a book, usually untitled. May be a chapter (or section or whatever) and/or a range of pages.
	 */
	Inbook,
	
	/**
	 * A part of a book having its own title. 
	 */
	Incollection,
	
	/**
	 * An article in a conference proceedings. 
	 */
	Inproceedings,
	
	/**
	 * Technical documentation.
	 */
	Manual,
	
	/**
	 * A Master's thesis.
	 */
	Mastersthesis,
	
	/**
	 * For use when nothing else fits.
	 */
	Misc,
	
	/**
	 * A Ph.D. thesis. 
	 */
	Phdthesis,
	
	/**
	 * The proceedings of a conference.
	 */
	Proceedings,
	
	/**
	 * A report published by a school or other institution, usually numbered within a series.
	 */
	Techreport,
	
	/**
	 * A document having an author and title, but not formally published.
	 */
	Unpublished
	;
	
	/**
	 * Get a {@link ReferenceType} from a string. 
	 * @param v the string
	 * @return the {@link ReferenceType}
	 */
	public static ReferenceType getReferenceType(String v) {
		for (ReferenceType rt : ReferenceType.values()) {
			if (rt.name().equalsIgnoreCase(v)) return rt;
		}
		return Misc;
	}
}
