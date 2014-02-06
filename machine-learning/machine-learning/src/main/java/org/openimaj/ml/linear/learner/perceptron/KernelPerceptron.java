package org.openimaj.ml.linear.learner.perceptron;

import java.util.List;

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
	private int errors;
	
	/**
	 * 
	 */
	public KernelPerceptron() {
	}
	
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
			this.errors ++;
		}
	}

	/**
	 * When there is an error in prediction, update somehow
	 * @param xt
	 * @param yt
	 * @param yt_prime
	 */
	public abstract void update(INDEPENDANT xt, DEPENDANT yt, DEPENDANT yt_prime) ;
	
	/**
	 * @return the vectors that form the support
	 */
	public abstract List<INDEPENDANT> getSupports();
	/**
	 * @return the weights of the support vectors
	 */
	public abstract List<Double> getWeights();
	
	/**
	 * @return the bias
	 */
	public abstract double getBias();
	
	/**
	 * @return number of errors made
	 */
	public int getErrors(){
		return errors;
		
	}
	
}
