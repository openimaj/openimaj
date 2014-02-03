package org.openimaj.ml.linear.learner.perceptron;

import org.openimaj.ml.linear.kernel.Kernel;
import org.openimaj.ml.linear.learner.OnlineLearner;

/**
 *
 * @param <INDEPENDANT>
 * @param <DEPENDANT>
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public abstract class KernelPerceptron<INDEPENDANT, DEPENDANT> implements OnlineLearner<INDEPENDANT, DEPENDANT>{

	
	Kernel<INDEPENDANT> kernel;

	/**
	 * @param kernel
	 */
	public KernelPerceptron(Kernel<INDEPENDANT> kernel) {
		this.kernel = kernel;
	}
	
	@Override
	public void process(INDEPENDANT xt, DEPENDANT yt) {
		DEPENDANT yt_prime = predict(xt);
		if(!yt_prime.equals(yt)){
			update(xt,yt,yt_prime);
		}
	}

	/**
	 * When there is an error in prediction, update somehow
	 * @param xt
	 * @param yt
	 * @param yt_prime
	 */
	public abstract void update(INDEPENDANT xt, DEPENDANT yt, DEPENDANT yt_prime) ;

}
