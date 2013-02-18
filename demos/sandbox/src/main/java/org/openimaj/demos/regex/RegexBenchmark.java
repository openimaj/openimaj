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
package org.openimaj.demos.regex;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.procedure.TDoubleProcedure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.openimaj.time.Timer;


/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RegexBenchmark {
	interface RegexEngine{
		public boolean applyRegex(String string);
		public void prepareEngine(String regex);
		@Override
		public String toString();
	}

	static class JavaRegexEngine implements RegexEngine{

		private Pattern pattern;
		@Override
		public boolean applyRegex(String string) {
			Matcher matcher = pattern.matcher(string);
			return matcher.matches();
		}

		@Override
		public void prepareEngine(String regex) {
			pattern = Pattern.compile(regex);
		}
		@Override
		public String toString() {
			return "Java regex engine";
		}
	}
	static class PerlEng implements RegexEngine{

		private org.apache.oro.text.regex.Pattern pattern;
		private Perl5Matcher matcher;

		@Override
		public boolean applyRegex(String string) {
			return matcher.matches(string, pattern);
		}

		@Override
		public void prepareEngine(String regex) {
			Perl5Compiler cmp = new Perl5Compiler();
			matcher = new Perl5Matcher();
			try {
				this.pattern = cmp.compile(regex);
			} catch (MalformedPatternException e) {
			}
		}

		@Override
		public String toString() {
			return "Perl5 Matcher";
		}

	}

	static class StreamFlyer implements RegexEngine{

		private com.googlecode.streamflyer.regex.fast.Pattern pattern;

		@Override
		public boolean applyRegex(String string) {
			return pattern.matcher(string).matches();
		}

		@Override
		public void prepareEngine(String regex) {
			pattern = com.googlecode.streamflyer.regex.fast.Pattern.compile(regex);
		}
		@Override
		public String toString() {
			return "StreamFlyter";
		}

	}

	List<RegexEngine> engines = new ArrayList<RegexEngine>();
	private String testRegex;
	private int nTests;
	private int lString;
	private String possible;
	private Random random;
	private TDoubleArrayList averageTimes;
	/**
	 * @param args
	 */
	public static void main(String args[]){
		RegexBenchmark benchmark = new RegexBenchmark();
		benchmark.addEngine(new JavaRegexEngine());
		benchmark.addEngine(new StreamFlyer());
		benchmark.addEngine(new PerlEng());
		benchmark.performTests();


	}
	private void addEngine(RegexEngine javaRegexEngine) {
		this.engines.add(javaRegexEngine);
	}
	/**
	 *
	 */
	public RegexBenchmark() {
		testRegex = ".*#(opendata|datagovuk|datagov|govuk|openuk|opengovuk|linkedata|govdata|semanticWeb).*";
		nTests = 1000;
		lString = 60000;
		possible = "#abcdefghijklmnopqrstuvwxyz ";
		random = new Random();
	}
	private void performTests() {
		averageTimes = new TDoubleArrayList(engines.size());
		int timeIndex = 0;
		for (RegexEngine engine : this.engines) {
			System.out.println("Preping: " + engine);
			averageTimes.add(0);
		}
		for (int i = 0; i < nTests; i++) {
			String generated = generateString();
//			System.out.println(generated);
			timeIndex = 0;
			for (RegexEngine engine : this.engines) {
				Timer t = new Timer();
				t.start();
				engine.prepareEngine(testRegex);
				engine.applyRegex(generated);
				averageTimes.set(timeIndex, averageTimes.get(timeIndex) + t.duration());
				timeIndex++;
			}
		}
		averageTimes.forEach(new TDoubleProcedure() {
			int i = 0;
			@Override
			public boolean execute(double value) {
				RegexEngine engine = engines.get(i);
				System.out.println(engine + " took: " + value/nTests);
				i++;
				return true;
			}
		});
	}
	private String generateString() {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < lString; i++) {
			builder.append(possible.charAt(random.nextInt(possible.length())));
		}
		return builder.toString();
	}
}
