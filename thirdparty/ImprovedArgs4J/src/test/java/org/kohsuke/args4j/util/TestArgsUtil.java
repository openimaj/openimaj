package org.kohsuke.args4j.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;

/**
 * Test the {@link ArgsUtil} static functions
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TestArgsUtil {
	static enum Opt {
		FIRST, SECOND;
	}

	interface ProxyOptionDetails {
	}

	static enum ProxyOption implements CmdLineOptionsProvider {
		FIRST {

			@Override
			public Object getOptions() {
				return new ProxyOptionDetails() {
					@Option(
							name = "--first-proxy-option",
							aliases = "-fpo",
							required = false,
							usage = "a string",
							multiValued = true)
					String firstOption = "first";
				};
			}
		},
		SECOND {
			@Override
			public Object getOptions() {
				return new ProxyOptionDetails() {
					@Option(
							name = "--second-proxy-option",
							aliases = "-spo",
							required = false,
							usage = "a string",
							multiValued = true)
					String secondOption = "second";
				};
			}
		};
	}

	static class Arguments {
		@Option(name = "--a-list", aliases = "-l", required = false, usage = "simple list", multiValued = true)
		List<String> list = new ArrayList<String>();

		@Option(name = "--a-bool", aliases = "-b", required = false, usage = "a boolean", multiValued = false)
		boolean bool;

		@Option(name = "--s-string", aliases = "-s", required = false, usage = "simple string", metaVar = "STRING")
		int string;

		@Option(name = "--a-enum", aliases = "-e", required = false, usage = "Enum entry")
		Opt statusType = Opt.FIRST;

		@Option(
				name = "--proxy-option",
				aliases = "-p",
				required = false,
				usage = "Something with proxy options!",
				handler = ProxyOptionHandler.class)
		ProxyOption outputModeOption = ProxyOption.FIRST;
		ProxyOptionDetails outputModeOptionOp = (ProxyOptionDetails) ProxyOption.FIRST
				.getOptions();
	}

	/**
	 * The test
	 * 
	 * @throws Exception
	 */
	@Test
	public void testArgs() throws Exception {
		final Arguments args = new Arguments();
		args.list.add("wang");
		args.list.add("bang");
		args.statusType = Opt.SECOND;
		String[] asArgArray = ArgsUtil.extractArguments(args);
		System.out.println(Arrays.toString(asArgArray));

		CmdLineParser parser = new CmdLineParser(args);
		parser.parseArgument(asArgArray);

		args.outputModeOption = ProxyOption.SECOND;
		args.outputModeOptionOp = (ProxyOptionDetails) args.outputModeOption.getOptions();
		args.string = 2;
		args.bool = true;
		asArgArray = ArgsUtil.extractArguments(args);

		parser = new CmdLineParser(args);
		parser.parseArgument(asArgArray);
		System.out.println(Arrays.toString(asArgArray));
	}

}
