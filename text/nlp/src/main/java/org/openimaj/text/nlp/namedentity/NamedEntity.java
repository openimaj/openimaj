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
package org.openimaj.text.nlp.namedentity;

/**
 * Container Class for Named Entity values
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class NamedEntity {

	/**
	 * Type of Named Entity
	 */
	public enum Type {
		Organisation,
		Person,
		Location
	}

	/**
	 * Type of Named Entity
	 */
	public Type type;
	/**
	 * Unique root name of entity
	 */
	public String rootName;
	/**
	 * The string that resulted in a match
	 */
	public String stringMatched;
	/**
	 * Start token of the match
	 */
	public int startToken;
	/**
	 * Stop token of the match
	 */
	public int stopToken;
	/**
	 * Start char of the match
	 */
	public int startChar;
	/**
	 * Stop char of the match
	 */
	public int stopChar;

	@Override
	public String toString() {
		return "NamedEntity [type=" + type + ", rootName=" + rootName
				+ ", startToken=" + startToken + ", stopToken=" + stopToken
				+ "]";
	}

	public NamedEntity() {

	}

	public NamedEntity(String rootName, Type type) {
		this.rootName = rootName;
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((rootName == null) ? 0 : rootName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final NamedEntity other = (NamedEntity) obj;
		if (rootName == null) {
			if (other.rootName != null)
				return false;
		} else if (!rootName.equals(other.rootName))
			return false;
		return true;
	}
}
