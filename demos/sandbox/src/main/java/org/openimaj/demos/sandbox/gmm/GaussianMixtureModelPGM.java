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
package org.openimaj.demos.sandbox.gmm;

/**
 * An attempt to code up the PGM for the GMM, namely:
 *
 * P(X,Z,pi,mu,sigmainv) = P(X | Z, mu, pi) P(Z | pi) P (pi) P(mu | sigmainv) P(sigmainv)
 *
 * If we want to express the distribtution of JUST the latent variables (X is the only observed variable)
 * we write:
 *
 * P(Z,pi,mu,sigmainv) = Q(Z) * Q(pi,mu,sigmainv)
 *
 * We can plug this directly into the free-form approximation of ln Q(_latent_) i.e.:
 *
 * ln Q_maximal (Z) = Expected_{mu, pi, sigmainv} [ ln P(X,Z,pi,mu,sigmainv ] + const
 *
 * if we call any factors of P not involving Z as part of the const, we can factorize this further into:
 *
 * ln Q_maximal (Z) = Expected_{pi}[P(Z | pi)] + Expected_{mu, sigmainv} [ P ( X | mu, Z, sigmainv) ] + const
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GaussianMixtureModelPGM {
	
}
