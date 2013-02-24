package org.openimaj.pgm.vb.lda.mle;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.pgm.util.SimpleCorpusReader;
import org.openimaj.pgm.util.Corpus;
import org.openimaj.pgm.util.CorpusReader;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestLDAMLE {
	private Corpus corpus;

	@Before
	public void before() throws IOException{
		CorpusReader cr = new SimpleCorpusReader(TestLDAMLE.class.getResourceAsStream("/org/openimaj/pgm/vb/lda/mle/berry.txt"));;
		this.corpus = cr.readCorpus(); 
	}
	
	@Test
	public void testLDAEstimation(){
		LDALearner learner = new LDALearner(10);
		learner.estimate(corpus);
	}
}
