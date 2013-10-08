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
package org.openimaj.util.api.auth;

/**
 * Interface describing a class capable of providing API tokens. See the
 * {@link DefaultTokenFactory} for an implementation that stores tokens in the
 * Java Preference system and can interactively query the user for the required
 * parameters if they don't have a saved token.
 * <p>
 * The tokens produced by a {@link TokenFactory} must be instances of a class
 * which is annotated with {@link Token}, and with fields annotated with
 * {@link Parameter}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface TokenFactory {
	/**
	 * Get the default token for the given class.
	 * <p>
	 * If a predefined token is unavailable, the token might be built by the
	 * underlying implementation; for example by interacting with the user.
	 * 
	 * @param tokenClass
	 *            the class of the token to build
	 * @return the token
	 */
	<T> T getToken(Class<T> tokenClass);

	/**
	 * Get the token for the given class, tagged with a specific name. This is
	 * useful if you have are multiple authentication tokens for the same API
	 * and want to refer to a specific one.
	 * <p>
	 * If a predefined token is unavailable, the token might be built by the
	 * underlying implementation; for example by interacting with the user.
	 * 
	 * @param tokenClass
	 *            the class of the token to build
	 * @param name
	 *            the name of the token
	 * @return the token
	 */
	<T> T getToken(Class<T> tokenClass, String name);
}
