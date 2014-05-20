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
package org.openimaj.image.feature.astheticode;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.contour.Contour;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Predicate;

/**
 * Aestheticode detection algorithm. Only supports simple codes (one parent,
 * many children, many grandchildren with no children of their own).
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class AestheticodeDetector implements Function<Contour, List<Aestheticode>>, Predicate<Contour> {

	private static final int MAX_CHILDLESS_CHILDREN_DEFAULT = 0;
	private static final int MAX_CHILDREN_DEFAULT = 5;
	private static final int MIN_CHILDREN_DEFAULT = 5;
	private int minChildren;
	private int maxChildren;
	private int maxChildlessChildren;

	/**
	 * Constructs with the min and max children to the default (both are 5) and
	 * max childless children to 0
	 */
	public AestheticodeDetector() {
		this.minChildren = MIN_CHILDREN_DEFAULT;
		this.maxChildren = MAX_CHILDREN_DEFAULT;
		this.maxChildlessChildren = MAX_CHILDLESS_CHILDREN_DEFAULT;
	}

	/**
	 * Construct with the given parameters
	 * 
	 * @param minChildren
	 *            the minimum number of children allowed in a root
	 * @param maxChildren
	 *            the maximum number of children allowed in a root
	 * @param maxChildless
	 *            the maximum number of childless children
	 */
	public AestheticodeDetector(int minChildren, int maxChildren, int maxChildless) {
		this.minChildren = minChildren;
		this.maxChildren = maxChildren;
		this.maxChildlessChildren = maxChildless;
	}

	@Override
	public List<Aestheticode> apply(Contour in) {
		final List<Aestheticode> found = new ArrayList<Aestheticode>();
		detectCode(in, found);
		return found;
	}

	private void detectCode(Contour root, List<Aestheticode> found) {
		if (test(root)) {
			found.add(new Aestheticode(root));
		}
		else {
			for (final Contour child : root.children) {
				detectCode(child, found);
			}
		}
	}

	@Override
	public boolean test(Contour in) {
		// has at least one child
		if (in.children.size() < minChildren || in.children.size() > maxChildren) {
			return false;
		}

		int childlessChild = 0;
		// all grandchildren have no children
		for (final Contour child : in.children) {
			if (child.children.size() == 0)
				childlessChild++;

			if (childlessChild > maxChildlessChildren)
				return false;
			for (final Contour grandchildren : child.children) {
				if (grandchildren.children.size() != 0)
					return false;
			}
		}
		return true;
	}
}
