package org.openimaj.demos.sandbox.gmm;

/**
 * Start with a liklihood on some data that looks like:
 * 
 * P(Data | mean, variance) = Normal(Data | mean, variance)
 * 
 * The prior for this liklihood looks like:
 * 
 * P(mean | variance ) = Normal(mean | mean_0, variance)
 * P(variance = Gamma(variance | a_0, b_0)
 * 
 * The goal is to calculate the posterior
 * 
 * P ( mean, variance | Data ) ~= P(Data | mean, variance) * P(mean | variance) * P(Variance)
 * 
 * But instead of doing so directly lets assume there is some function:
 * 
 * Q(mean, variance) = Q(mean) * Q(variance)
 * 
 * s.t.
 * 
 * ln Q*(mean,variance) = a lower bound for P(mean,variance | Data)
 * or the optimal value for ln Q*(mean,variance) is "the best" approximate estimate
 * of P using Q. 
 * 
 * We can also say that:
 * ln Q(mean) = E_{variance} [ ln P(mean,variance | Data)]
 * and
 * ln Q(variance) = E_{mean} [ ln P(mean,variance | Data)]
 * 
 * Starting from here, and figuring out the distribution for Q(mean) and Q(variance)
 * we end up with:
 * 
 * Q(mean) = Normal(mean | mean_N, variance_N)
 * s.t.
 * 
 * mean_N = { lambda_0 * mean_0 + N * mean(Data) } / { lambda_0 + N }
 * variance_N = ( lambda_0 + N ) * E [ variance ]
 * 
 * 
 * and
 * 
 * Q(variance) = Gamma(variance | a_N, b_N)
 * s.t.
 * a_N = a_0 + N/2
 * b_N = b_0 + 1/2 * E_{mean} [ Sum[N:n=1] { (x_n - mean)^2 + lambda_0(mean - mean_0)^2}]
 * 
 * Note that Q(variance) requires estimates of the mean and Q(mean) requires estimates of the variance!
 * 
 * We must the variance to something, use it to calculate the the mean for the optimal 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class UnivariateGaussianVariationalInference {
	
}
