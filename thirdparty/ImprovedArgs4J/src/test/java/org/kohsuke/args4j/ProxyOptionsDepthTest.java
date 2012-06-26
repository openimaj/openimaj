package org.kohsuke.args4j;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Some tests for the {@link ProxyOptionHandler} using multi-level proxies
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ProxyOptionsDepthTest {
	static class ObjectWithOptions2 {
		@Option(name = "-r", required = true)
		int value;
	}

	static class ObjectWithOptions {
		@Option(name = "-opt2", handler = ProxyOptionHandler.class)
		OptionsEnum2 options = OptionsEnum2.FIRST;
		Object optionsOp;
	}
	
	enum OptionsEnum2 implements CmdLineOptionsProvider {
		FIRST {
			@Override
			public Object getOptions() {
				return new ObjectWithOptions2();
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
	 * Should throw because OptionsEnum2.FIRST has a required field 
	 * 
	 * @throws CmdLineException 
	 */
	@Test(expected = CmdLineException.class)
	public void testHandler1() throws CmdLineException {
		parser.parseArgument();
	}
	
	/**
	 * test defaults with required -r
	 * 
	 * @throws CmdLineException 
	 */
	@Test
	public void testHandler2() throws CmdLineException {
		parser.parseArgument("-r", "1");
		assertEquals(1, ((ObjectWithOptions2)((ObjectWithOptions)options.optOp).optionsOp).value);
	}
	
	/**
	 * Should throw because OptionsEnum2.FIRST has a required field 
	 * 
	 * @throws CmdLineException 
	 */
	@Test(expected = CmdLineException.class)
	public void testHandler3() throws CmdLineException {
		parser.parseArgument("-opt", "FIRST");
	}
	
	/**
	 * Should pass 
	 * 
	 * @throws CmdLineException 
	 */
	@Test
	public void testHandler4() throws CmdLineException {
		parser.parseArgument("-opt", "FIRST", "-opt2", "SECOND");
	}
	
	/**
	 * Should throw because of missing field
	 * 
	 * @throws CmdLineException 
	 */
	@Test(expected = CmdLineException.class)
	public void testHandler5() throws CmdLineException {
		parser.parseArgument("-opt", "FIRST", "-opt2", "FIRST");
	}
	
	/**
	 * Should pass
	 * 
	 * @throws CmdLineException 
	 */
	@Test
	public void testHandler6() throws CmdLineException {
		parser.parseArgument("-opt", "FIRST", "-opt2", "FIRST", "-r", "1");
		assertEquals(1, ((ObjectWithOptions2)((ObjectWithOptions)options.optOp).optionsOp).value);
	}
	
	/**
	 * Should pass (using defaults for -opts2)
	 * 
	 * @throws CmdLineException 
	 */
	@Test
	public void testHandler7() throws CmdLineException {
		parser.parseArgument("-opt", "FIRST", "-r", "1");
		assertEquals(1, ((ObjectWithOptions2)((ObjectWithOptions)options.optOp).optionsOp).value);
	}
}
