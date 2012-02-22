package org.openimaj.math.matrix.algorithm.pca;

/**
 * Tests for {@link CovarPrincipalComponentAnalysis}
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class CovarPrincipalComponentAnalysisTest extends PrincipalComponentAnalysisTest {

	@Override
	protected PrincipalComponentAnalysis createPCA() {
		return new CovarPrincipalComponentAnalysis();
	}
	
}
