package org.openimaj.ml.linear.learner;

public interface OnlineLearner<DEPENDANT, INDEPENDANT> {
	
	public void process(DEPENDANT y, INDEPENDANT x);
}
