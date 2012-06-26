package org.kohsuke.args4j;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for checking the old-style (deprecated) enums options
 * still work. Don't use this style in new code --- it's a really
 * bad idea to give an enum internal state, especially in environments
 * where it might be used more than once!
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ProxyOptionsOldStyleEnumsTest {
	enum OptionsEnum implements CmdLineOptionsProvider {
		WITH {
			@Option(name = "-i", required=true)
			int intOption;

			@Override
			public int getValue() {
				return intOption;
			}
		},
		WITHOUT {
			@Override
			public int getValue() {
				return Integer.MIN_VALUE;
			}			
		}
		;

		@Override
		public Object getOptions() {
			return this;
		}
		
		public abstract int getValue();
	}
	
	class OptionsHolder {
		@Option(name = "-o", required = false, handler = ProxyOptionHandler.class)
		OptionsEnum optionEnum = OptionsEnum.WITH;
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
	 * Test defaults; should throw because of missing -i
	 * @throws CmdLineException
	 */
	@Test(expected = CmdLineException.class)
	public void testHandler1() throws CmdLineException {
		parser.parseArgument();
	}
	
	/**
	 * Test WITHOUT
	 * @throws CmdLineException
	 */
	@Test
	public void testHandler2() throws CmdLineException {
		parser.parseArgument("-o", "WITHOUT");
		assertEquals(Integer.MIN_VALUE, options.optionEnum.getValue());
	}
	
	/**
	 * Test WITH, without required; should throw because of missing -i
	 * @throws CmdLineException
	 */
	@Test(expected = CmdLineException.class)
	public void testHandler3() throws CmdLineException {
		parser.parseArgument("-o", "WITH");
	}
	
	/**
	 * Test WITH and required
	 * @throws CmdLineException
	 */
	@Test
	public void testHandler4() throws CmdLineException {
		parser.parseArgument("-o", "WITH", "-i", "1");
		assertEquals(1, options.optionEnum.getValue());
	}
}
