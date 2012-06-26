package org.kohsuke.args4j;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Some tests for the {@link ProxyOptionHandler}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ProxyOptionsTest {
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
		OptionsEnum opt = OptionsEnum.FIRST;
		Object optOp;
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
	 * Should throw because OptionsEnum.FIRST has a required field 
	 * 
	 * @throws CmdLineException 
	 */
	@Test(expected = CmdLineException.class)
	public void testHandler1() throws CmdLineException {
		parser.parseArgument();
	}
	
	/**
	 * Test that -r works WITHOUT -opt
	 * 
	 * @throws CmdLineException
	 */
	@Test
	public void testHandler2() throws CmdLineException {
		parser.parseArgument("-r", "10101");
		
		assertEquals(((ObjectWithOptions)options.optOp).required, 10101);
	}
	
	/**
	 * Test that -r works WITH -opt
	 * 
	 * @throws CmdLineException
	 */
	@Test
	public void testHandler3() throws CmdLineException {
		parser.parseArgument("-opt", "FIRST", "-r", "10101");
		
		assertEquals(((ObjectWithOptions)options.optOp).required, 10101);
	}
	
	/**
	 * Test that -opt SECOND works, and trying to get the
	 * required option throws a {@link ClassCastException}
	 * 
	 * @throws CmdLineException
	 */
	@Test(expected = ClassCastException.class)
	public void testHandler4() throws CmdLineException {
		parser.parseArgument("-opt", "SECOND");
		
		((ObjectWithOptions)options.optOp).required = 1;
	}
	
	/**
	 * Test that -opt SECOND with -r throws
	 * 
	 * @throws CmdLineException
	 */
	@Test(expected = CmdLineException.class)
	public void testHandler5() throws CmdLineException {
		parser.parseArgument("-opt", "SECOND", "-r", "10101");
	}
	
	/**
	 * Test that -r is clobbered by the opt and an exception is
	 * thrown because of this.
	 * 
	 * @throws CmdLineException
	 */
	@Test(expected = CmdLineException.class)
	public void testHandler6() throws CmdLineException {
		parser.parseArgument("-r", "10101", "-opt", "SECOND");
	}
}
