package org.openimaj.math.matrix.algorithm.pca;

/**
 * Tests for {@link SvdPrincipalComponentAnalysis}
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class SvdPrincipalComponentAnalysisTest extends PrincipalComponentAnalysisTest {

	@Override
	protected PrincipalComponentAnalysis createPCA() {
		return new SvdPrincipalComponentAnalysis();
	}
	
}
