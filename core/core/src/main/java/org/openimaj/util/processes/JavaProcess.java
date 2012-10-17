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
package org.openimaj.util.processes;

import java.io.File;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Commandline.Argument;
import org.apache.tools.ant.types.Path;
import org.openimaj.util.array.ArrayUtils;

/**
 * Helper methods for launching a new JVM and running code within it. Advanced
 * configuration can be achieved by constructing a {@link ProcessOptions} and
 * calling {@link #runProcess(ProcessOptions)}. For most other use-cases the
 * static methods provide a convenient shortcut.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class JavaProcess {
	/**
	 * Options describing a JVM environment.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static class ProcessOptions {
		File workingDir;
		String classPath;
		String mainClass;
		String jvmArgs;
		File jarFile;
		String args;
		boolean cloneVM;
		boolean outputErr = true;
		boolean outputOut = true;

		/**
		 * Construct a new {@link ProcessOptions} that inherits the classpath of
		 * the current JVM and will runs the main method of the given class.
		 * 
		 * @param clz
		 *            the main class
		 */
		public ProcessOptions(Class<?> clz) {
			this.workingDir = new File(System.getProperty("user.dir", new File(".").getAbsolutePath()));
			this.mainClass = clz.getName();
			cloneVM = true;
		}

		/**
		 * Construct a new {@link ProcessOptions} that will run the given jar
		 * file.
		 * 
		 * @param jarFile
		 *            the jar file
		 */
		public ProcessOptions(File jarFile) {
			this.workingDir = new File(System.getProperty("user.dir", new File(".").getAbsolutePath()));
			this.jarFile = jarFile;
			cloneVM = false;
		}

		/**
		 * Construct a new {@link ProcessOptions} with the given classpath and
		 * main class.
		 * 
		 * @param classPath
		 *            the classpath the JVM can search
		 * @param mainClass
		 *            the main class to run
		 */
		public ProcessOptions(String classPath, String mainClass) {
			this.workingDir = new File(System.getProperty("user.dir", new File(".").getAbsolutePath()));
			this.classPath = classPath;
			this.mainClass = mainClass;
			cloneVM = false;
		}

		/**
		 * Set the working directory for the JVM. By default this is set to the
		 * working directory of the current JVM. Passing a null value resets to
		 * the default.
		 * 
		 * @param workingDir
		 *            the working directory
		 */
		public void setWorkingDirectory(File workingDir) {
			if (workingDir == null)
				workingDir = new File(System.getProperty("user.dir", new File(".").getAbsolutePath()));

			this.workingDir = workingDir;
		}

		/**
		 * Set the argument string to pass to the program
		 * 
		 * @param args
		 *            the argument string
		 */
		public void setArgs(String args) {
			this.args = args;
		}

		/**
		 * Set the arguments to pass to the program
		 * 
		 * @param args
		 *            the arguments
		 */
		public void setArgs(String[] args) {
			this.args = ArrayUtils.toString(args, " ");
		}

		/**
		 * Set the JVM arguments to use when launching the program.
		 * 
		 * @param jvmArgs
		 *            the JVM arguments.
		 */
		public void setJvmArgs(String jvmArgs) {
			this.jvmArgs = jvmArgs;
		}

		/**
		 * Set the JVM arguments to use when launching the program.
		 * 
		 * @param jvmArgs
		 *            the JVM arguments.
		 */
		public void setJvmArgs(String[] jvmArgs) {
			this.jvmArgs = ArrayUtils.toString(jvmArgs, " ");
		}

		protected void setup(Java java) {
			if (jarFile != null) {
				java.setJar(jarFile);
			} else {
				if (classPath == null) {
					java.setClasspath(Path.systemClasspath);
				} else {
					java.setClasspath(new Path(java.getProject(), classPath));
				}
				java.setClassname(mainClass);
			}

			if (jvmArgs != null) {
				final Argument jvmArg = java.createJvmarg();
				jvmArg.setLine(jvmArgs);
			}

			if (args != null) {
				final Argument taskArgs = java.createArg();
				taskArgs.setLine(args);
			}

			java.getProject().addBuildListener(new SimpleListener(java, outputOut, outputErr));

			java.setCloneVm(cloneVM);
		}

		/**
		 * Echo the {@link System#err} stream from the running JVM
		 * 
		 * @param outputErr
		 *            true to echo; false to hide
		 */
		public void outputErr(boolean outputErr) {
			this.outputErr = outputErr;
		}

		/**
		 * Echo the {@link System#out} stream from the running JVM
		 * 
		 * @param outputOut
		 *            true to echo; false to hide
		 */
		public void outputOut(boolean outputOut) {
			this.outputOut = outputOut;
		}
	}

	/**
	 * Run the <code>main</code> main method of the given class in a new JVM.
	 * The new JVM has the same classpath as the invoking JVM. No JVM arguments
	 * are set.
	 * 
	 * @param clz
	 *            the class to run
	 * @param args
	 *            the arguments to pass to the main method
	 * 
	 * @throws ProcessException
	 *             if an error occurs during execution or the return code is
	 *             non-zero.
	 */
	public static void runProcess(Class<?> clz, String[] args) throws ProcessException {
		final ProcessOptions opts = new ProcessOptions(clz);
		opts.setArgs(args);

		runProcess(opts);
	}

	/**
	 * Run the <code>main</code> main method of the given class in a new JVM.
	 * The new JVM has the same classpath as the invoking JVM. No JVM arguments
	 * are set.
	 * 
	 * @param clz
	 *            the class to run
	 * @param argString
	 *            the arguments to pass to the main method
	 * 
	 * @throws ProcessException
	 *             if an error occurs during execution or the return code is
	 *             non-zero.
	 */
	public static void runProcess(Class<?> clz, String argString) throws ProcessException {
		final ProcessOptions opts = new ProcessOptions(clz);
		opts.setArgs(argString);

		runProcess(opts);
	}

	/**
	 * Run the <code>main</code> main method of the given class in a new JVM.
	 * The new JVM has the same classpath as the invoking JVM.
	 * 
	 * @param clz
	 *            the class to run
	 * @param jvmArgs
	 *            arguments to pass to the JVM
	 * @param args
	 *            the arguments to pass to the main method
	 * 
	 * @throws ProcessException
	 *             if an error occurs during execution or the return code is
	 *             non-zero.
	 */
	public static void runProcess(Class<?> clz, String[] jvmArgs, String[] args) throws ProcessException {
		final ProcessOptions opts = new ProcessOptions(clz);
		opts.setArgs(args);
		opts.setJvmArgs(jvmArgs);

		runProcess(opts);
	}

	/**
	 * Run the <code>main</code> main method of the given class in a new JVM.
	 * The new JVM has the same classpath as the invoking JVM.
	 * 
	 * @param clz
	 *            the class to run
	 * @param jvmArgs
	 *            arguments to pass to the JVM
	 * @param argString
	 *            the arguments to pass to the main method
	 * 
	 * @throws ProcessException
	 *             if an error occurs during execution or the return code is
	 *             non-zero.
	 */
	public static void runProcess(Class<?> clz, String jvmArgs, String argString) throws ProcessException {
		final ProcessOptions opts = new ProcessOptions(clz);
		opts.setArgs(argString);
		opts.setJvmArgs(jvmArgs);

		runProcess(opts);
	}

	/**
	 * Run the process described by the given {@link ProcessOptions} in a
	 * separate JVM and wait for it to exit.
	 * 
	 * @param op
	 *            the {@link ProcessOptions}.
	 * @throws ProcessException
	 *             if an error occurs during execution or the return code is
	 *             non-zero.
	 */
	public static void runProcess(ProcessOptions op) throws ProcessException {
		final Project project = new Project();
		project.setBaseDir(op.workingDir);
		project.init();
		project.fireBuildStarted();

		BuildException caught = null;

		try {
			final Java java = new Java();
			java.setNewenvironment(true);
			java.setTaskName("runjava");
			java.setProject(project);
			java.setFork(true);
			java.setFailonerror(true);

			op.setup(java);

			java.init();
			java.execute();
		} catch (final BuildException e) {
			caught = e;
			throw new ProcessException(caught);
		} finally {
			project.log("finished");
			project.fireBuildFinished(caught);
		}
	}

	private static class SimpleListener implements BuildListener {
		Java proc;
		boolean outputOut;
		boolean outputErr;

		SimpleListener(Java proc, boolean outputOut, boolean outputErr) {
			this.proc = proc;
			this.outputOut = outputOut;
			this.outputErr = outputErr;
		}

		@Override
		public void taskStarted(BuildEvent event) {
		}

		@Override
		public void taskFinished(BuildEvent event) {
		}

		@Override
		public void targetStarted(BuildEvent event) {
		}

		@Override
		public void targetFinished(BuildEvent event) {
		}

		@Override
		public void messageLogged(BuildEvent event) {
			if (proc != event.getSource())
				return;

			if (outputOut && event.getPriority() == Project.MSG_INFO)
				System.out.println(event.getMessage());
			else if (outputErr && event.getPriority() == Project.MSG_ERR)
				System.err.println(event.getMessage());
		}

		@Override
		public void buildStarted(BuildEvent event) {
		}

		@Override
		public void buildFinished(BuildEvent event) {
		}
	}
}
