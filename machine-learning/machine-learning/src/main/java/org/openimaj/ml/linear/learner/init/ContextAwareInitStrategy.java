package org.openimaj.ml.linear.learner.init;

import org.openimaj.ml.linear.learner.OnlineLearner;


/**
 * A {@link ContextAwareInitStrategy} is told the learner it is initialising against
 * and the current INDEPENDANT and DEPENDANT variables at init time.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <INDEPENDANT> the Y
 * @param <DEPENDANT>  the X
 *
 */
public interface ContextAwareInitStrategy<INDEPENDANT,DEPENDANT> extends InitStrategy{
	/**
	 * The
	 * @param learner
	 */
	public void setLearner(OnlineLearner<INDEPENDANT,DEPENDANT> learner);

	/**
	 * The current INDEPENDANT and DEPENDANT values at the time of initialization
	 * @param x
	 * @param y
	 */
	public void setContext(INDEPENDANT x, DEPENDANT y);
}
