package org.openimaj.ml.linear.learner;

public interface OnlineLearner<INDEPENDANT,DEPENDANT> {
	
	public void process(INDEPENDANT x, DEPENDANT y);
	public DEPENDANT predict(INDEPENDANT x);
}
