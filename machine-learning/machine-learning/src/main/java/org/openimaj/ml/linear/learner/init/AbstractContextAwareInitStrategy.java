package org.openimaj.ml.linear.learner.init;

import org.openimaj.ml.linear.learner.OnlineLearner;


/**
 * Holds on to the learner and the context variables
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <INDEPENDANT>
 * @param <DEPENDANT>
 *
 */
public abstract class AbstractContextAwareInitStrategy<INDEPENDANT, DEPENDANT> implements ContextAwareInitStrategy<INDEPENDANT, DEPENDANT>{


	protected INDEPENDANT x;
	protected DEPENDANT y;
	protected OnlineLearner<INDEPENDANT, DEPENDANT> learner;

	@Override
	public void setLearner(OnlineLearner<INDEPENDANT, DEPENDANT> learner) {
		this.learner = learner;
	}
	@Override
	public void setContext(INDEPENDANT x, DEPENDANT y) {
		this.x = x;
		this.y = y;
	}

}
