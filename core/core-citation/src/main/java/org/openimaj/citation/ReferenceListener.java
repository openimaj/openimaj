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
package org.openimaj.citation;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.References;

/**
 * Listener that registers instances of {@link Reference} annotations.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ReferenceListener {
	private static Set<Reference> references = new LinkedHashSet<Reference>();

	static {
		addOpenIMAJReference();
	}

	private static synchronized void addOpenIMAJReference() {
		try {
			final Class<?> clz = ReferenceListener.class.getClassLoader().loadClass("org.openimaj.OpenIMAJ");
			addReference(clz);
		} catch (final ClassNotFoundException e) {
			// assume that core-citation is being used outside OpenIMAJ
			// and that thus you don't want/need an OI reference.
		}
	}

	/**
	 * Register the given {@link Reference}
	 * 
	 * @param r
	 *            the {@link Reference}
	 */
	public static synchronized void addReference(Reference r) {
		references.add(r);
	}

	/**
	 * Register the any {@link Reference} or {@link References} from the given
	 * class.
	 * 
	 * @param clz
	 *            the class
	 */
	public static void addReference(Class<?> clz) {
		final Reference ann = clz.getAnnotation(Reference.class);

		if (ann != null)
			addReference(ann);

		final References ann2 = clz.getAnnotation(References.class);
		if (ann2 != null)
			for (final Reference r : ann2.references())
				addReference(r);

		processPackage(clz);
	}

	private static void processPackage(Class<?> clz) {
		Package base = clz.getPackage();

		while (base != null) {
			if (base.isAnnotationPresent(Reference.class))
				addReference(base.getAnnotation(Reference.class));

			if (base.isAnnotationPresent(References.class))
				for (final Reference r : base.getAnnotation(References.class).references())
					addReference(r);

			final String name = base.getName();
			final int dot = name.lastIndexOf(".");

			if (dot < 0)
				break;

			base = Package.getPackage(name.substring(0, dot));
		}
	}

	/**
	 * Register the any {@link Reference} or {@link References} from the given
	 * method.
	 * 
	 * @param clz
	 *            the class
	 * @param methodName
	 * @param signature
	 */
	public static void addReference(Class<?> clz, String methodName, String signature) {
		for (final Method m : clz.getDeclaredMethods()) {
			if (m.getName().equals(methodName) && m.toString().endsWith(signature)) {
				final Reference ann = m.getAnnotation(Reference.class);

				if (ann != null)
					addReference(ann);

				final References ann2 = m.getAnnotation(References.class);
				if (ann2 != null)
					for (final Reference r : ann2.references())
						addReference(r);
			}
		}

		processPackage(clz);
	}

	/**
	 * Reset the references held by the listener, returning the current set of
	 * references.
	 * 
	 * @return the current set of references.
	 */
	public static synchronized Set<Reference> reset() {
		final Set<Reference> oldRefs = references;
		references = new LinkedHashSet<Reference>();
		addOpenIMAJReference();
		return oldRefs;
	}

	/**
	 * Get a copy of the references collected by the listener
	 * 
	 * @return the references.
	 */
	public static synchronized Set<Reference> getReferences() {
		return new LinkedHashSet<Reference>(references);
	}

	/**
	 * Register the given {@link Reference}s
	 * 
	 * @param refs
	 *            the {@link Reference}s
	 */
	public static void addReferences(Collection<Reference> refs) {
		references.addAll(refs);
	}
}
