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
package org.openimaj.ml.linear.learner;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class LearningParameters extends HashMap<String, Object> {
	protected class Placeholder {
		public Placeholder(String name) {
			this.name = name;
		}

		public String name;
	}

	private static final long serialVersionUID = 8150107681506488394L;

	Map<String, Object> defaults = new HashMap<String, Object>();

	public LearningParameters() {
	}

	public LearningParameters(Map<String, Object> defaults) {
		this.defaults.putAll(defaults);
	}

	@SuppressWarnings("unchecked")
	public <T> T getTyped(String s) {
		Object thisVal = this.get(s);
		if (thisVal == null) {
			thisVal = defaults.get(s);
		}
		if (thisVal != null && thisVal instanceof Placeholder) {
			return getTyped(((Placeholder) thisVal).name);
		}
		return (T) thisVal;
	}
}
