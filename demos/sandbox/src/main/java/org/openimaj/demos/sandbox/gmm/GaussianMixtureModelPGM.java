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
