/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.tools.globalfeature;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * A tool for running other tools in parallel, in a similar manner to a UNIX
 * Makefile.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ParallelExecutor {
	@Option(name = "--input", aliases = "-i", usage = "input directory", required = true)
	private File inputBase;

	@Option(name = "--output", aliases = "-o", usage = "output directory", required = true)
	private File outputBase;

	@Option(name = "--output-ext", aliases = "-e", usage = "output extension", required = true)
	private File outputExt;

	@Option(name = "--input-regex", aliases = "-r", usage = "input regex")
	private String inputRegex;

	@Option(name = "--class", aliases = "-c", usage = "class to run", required = true)
	private String commandClass;

	@Option(name = "--args", aliases = "-a", usage = "arguments to pass", required = true)
	private String commandArgs;

	@Option(name = "--force", aliases = "-f", usage = "force regenerate")
	private boolean force = false;

	@Option(name = "-j", usage = "n paralled jobs", required = false)
	private int njobs = 1;

	@Option(name = "--timing", aliases = "-t", usage = "print timing information")
	private boolean timing = false;

	@Option(name = "--verbose", aliases = "-v", usage = "print timing information")
	private boolean verbose = false;

	// private TLongObjectHashMap<URLClassLoader> classLoaders = new
	// TLongObjectHashMap<URLClassLoader>();

	synchronized Class<?> loadClass(String clzName) throws ClassNotFoundException {
		// long id = Thread.currentThread().getId();
		URLClassLoader tmp;

		// if (classLoaders.containsKey(id)) {
		// tmp = classLoaders.get(id);
		// } else {
		tmp = new URLClassLoader(new URL[] { getClassPath() }) {
			@Override
			public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
				Class<?> cl = null;
				try {
					cl = findLoadedClass(name);
					if (cl != null)
						return cl;
					cl = findClass(name);
				} catch (final ClassNotFoundException e) {
					cl = super.loadClass(name);
				}

				return cl;
			}
		};

		// classLoaders.put(id, tmp);
		// }

		return tmp.loadClass(clzName);
	}

	private static URL getClassPath() {
		final String resName = ParallelExecutor.class.getName().replace('.', '/') + ".class";
		final String loc = ParallelExecutor.class.getClassLoader().getResource(resName).toExternalForm();
		URL cp;
		try {
			cp = new URL(loc.substring(0, loc.length() - resName.length()));
		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return cp;
	}

	class Job implements Callable<Boolean> {
		File file;

		public Job(File file) {
			this.file = file;
		}

		@Override
		public Boolean call() throws Exception {
			try {
				Class<?> clz = null;
				try {
					clz = loadClass(commandClass);
					// clz = Class.forName(commandClass);
				} catch (final ClassNotFoundException e) {
					clz = loadClass("org.openimaj.tools.globalfeature." + commandClass);
					// clz =
					// Class.forName("uk.ac.soton.ecs.jsh2."+commandClass);
				}

				final Method m = clz.getMethod("main", String[].class);

				final File output = getOutput(file);
				output.getParentFile().mkdirs();

				String theCommandArgs = commandArgs.replace("__IN__", file.getAbsolutePath());
				theCommandArgs = theCommandArgs.replace("__OUT__", output.getAbsolutePath());

				if (verbose)
					System.err.println("java " + commandClass + " " + theCommandArgs);

				m.invoke(null, new Object[] { theCommandArgs.split(" ") });
			} catch (final Throwable e) {
				e.printStackTrace();
				return false;
			}

			return true;
		}
	}

	void execute() throws InterruptedException {
		final double t1 = System.currentTimeMillis();
		final List<File> inputFiles = getInputs();
		final double t2 = System.currentTimeMillis();
		if (timing) {
			System.out.println("Input files:\t" + inputFiles.size());
			System.out.println("Search time:\t" + ((t2 - t1) / 1000.0) + " secs");
		}

		final List<Job> jobs = new ArrayList<Job>();

		for (final File f : inputFiles)
			jobs.add(new Job(f));
		final double t3 = System.currentTimeMillis();
		if (timing)
			System.out.println("Job create:\t" + ((t3 - t2) / 1000.0) + " secs");

		final ExecutorService es = Executors.newFixedThreadPool(njobs);
		es.invokeAll(jobs);
		es.shutdown();

		final double t4 = System.currentTimeMillis();
		if (timing) {
			System.out.println("Proc time total:\t" + ((t4 - t3) / 1000.0) + " secs");
			System.out.println("Proc time per file:\t" + (((t4 - t3) / 1000.0) / inputFiles.size()) + " secs");

			System.out.println("Norm Proc time total:\t" + (njobs * (t4 - t3) / 1000) + " secs");
			System.out.println("Norm Proc time per file:\t" + (njobs * ((t4 - t3) / 1000.0) / inputFiles.size())
					+ " secs");
		}
	}

	private List<File> getInputs() {
		final List<File> files = new ArrayList<File>();

		getInputs(files, inputBase);

		return files;
	}

	private void getInputs(List<File> files, File dir) {
		for (final File f : dir.listFiles()) {
			if (f.isDirectory()) {
				getInputs(files, f);
			} else {
				// check matches regex
				if (inputRegex == null || f.getName().matches(inputRegex)) {
					// check output
					final File output = getOutput(f);

					if (!output.exists() || force) {
						files.add(f);
					}
				}
			}
		}
	}

	private File getOutput(File f) {
		final File tmp = new File(outputBase, f.getAbsolutePath().replace(inputBase.getAbsolutePath(), ""));
		String outputName = tmp.getName();
		if (tmp.getName().contains(".")) {
			outputName = tmp.getName().substring(0, tmp.getName().lastIndexOf("."));
		}
		return new File(tmp.getParent(), outputName + outputExt);
	}

	/**
	 * The main method of the tool.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		final ParallelExecutor tool = new ParallelExecutor();

		final CmdLineParser parser = new CmdLineParser(tool);

		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err
					.println("Usage: java -cp GlobalFeaturesTool.jar uk.ac.soton.ecs.jsh2.ParallelExecutor [options...]");
			parser.printUsage(System.err);
			return;
		}

		tool.execute();
	}
}
