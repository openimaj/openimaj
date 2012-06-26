package org.kohsuke.args4j;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Some tests for the {@link ProxyOptionHandler}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ProxyOptionsListTest {
	static class ObjectWithOptions {
		@Option(name = "-r", required = true)
		int required;
	}
	
	enum OptionsEnum implements CmdLineOptionsProvider {
		FIRST {
			@Override
			public Object getOptions() {
				return new ObjectWithOptions();
			}
		},
		SECOND {
			@Override
			public Object getOptions() {
				return new Object();
			}
		}
		;
	}
	
	class OptionsHolder {
		@Option(name = "-opt", required = false, handler = ProxyOptionHandler.class)
		List<OptionsEnum> opt;
		List<Object> optOp;
	}
	
	OptionsHolder options;
	CmdLineParser parser;
	
	/**
	 * Setup
	 */
	@Before
	public void setup() {
		options = new OptionsHolder();
		parser = new CmdLineParser(options);
	}
	
	/**
	 * No options 
	 * 
	 * @throws CmdLineException 
	 */
	@Test
	public void testHandler1() throws CmdLineException {
		parser.parseArgument();
	}
	
	/**
	 * One option (SECOND)
	 * 
	 * @throws CmdLineException 
	 */
	@Test
	public void testHandler2() throws CmdLineException {
		parser.parseArgument("-opt", "SECOND");
	}
	
	/**
	 * One option (FIRST); should fail because of missing required
	 * 
	 * @throws CmdLineException 
	 */
	@Test(expected = CmdLineException.class)
	public void testHandler3() throws CmdLineException {
		parser.parseArgument("-opt", "FIRST");
	}
	
	/**
	 * One option (FIRST) with required
	 * 
	 * @throws CmdLineException 
	 */
	@Test
	public void testHandler4() throws CmdLineException {
		parser.parseArgument("-opt", "FIRST", "-r", "1");
		assertEquals(1, options.optOp.size());
		assertEquals(1, ((ObjectWithOptions)options.optOp.get(0)).required);
	}
	
	/**
	 * Two options: (SECOND), (FIRST with required)
	 * 
	 * @throws CmdLineException 
	 */
	@Test
	public void testHandler5() throws CmdLineException {
		parser.parseArgument("-opt", "SECOND", "-opt", "FIRST", "-r", "1");
		assertEquals(2, options.optOp.size());
		assertEquals(1, ((ObjectWithOptions)options.optOp.get(1)).required);
	}
	
	/**
	 * Two options: (FIRST with required), (SECOND)
	 * 
	 * @throws CmdLineException 
	 */
	@Test
	public void testHandler6() throws CmdLineException {
		parser.parseArgument("-opt", "FIRST", "-r", "1", "-opt", "SECOND");
		assertEquals(2, options.optOp.size());
		assertEquals(1, ((ObjectWithOptions)options.optOp.get(0)).required);
	}
	
	/**
	 * Two options: (FIRST without required), (SECOND). Should throw because of missing required.
	 * 
	 * @throws CmdLineException 
	 */
	@Test(expected = CmdLineException.class)
	public void testHandler7() throws CmdLineException {
		parser.parseArgument("-opt", "FIRST", "-opt", "SECOND");
		System.out.println(options.optOp);
	}
	
	/**
	 * Two options: (SECOND), (FIRST without required). Should throw because of missing required.
	 * 
	 * @throws CmdLineException 
	 */
	@Test(expected = CmdLineException.class)
	public void testHandler8() throws CmdLineException {
		parser.parseArgument("-opt", "SECOND", "-opt", "FIRST");
	}
}
