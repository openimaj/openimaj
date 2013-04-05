package org.openimaj.ml.linear.learner.loss;

import gov.sandia.cognition.math.matrix.Matrix;

/**
 * With a held Y and X, return gradient and evaluations of
 * a loss function of some parameters s.t.
 * 
 * J(W) = F(Y,X,W)
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class LossFunction {
	protected Matrix X;
	protected Matrix Y;
	protected Matrix bias;
	public void setX(Matrix X){
		this.X = X;
	}
	public void setY(Matrix Y){
		this.Y = Y;
	}
	public abstract Matrix gradient(Matrix W);
	public abstract double eval(Matrix W);
	public void setBias(Matrix bias) {
		this.bias = bias;
	}
}
