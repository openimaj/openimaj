package org.openimaj.math.matrix.algorithm.pca;

/**
 * Tests for {@link ThinSvdPrincipalComponentAnalysis}
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ThinSvdPrincipalComponentAnalysisTest extends PrincipalComponentAnalysisTest {

	@Override
	protected PrincipalComponentAnalysis createPCA() {
		return new ThinSvdPrincipalComponentAnalysis(2);
	}
	
}
