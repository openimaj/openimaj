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
package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.util.pair.IndependentPair;

/**
 * Emits each word with the total number of times the word was seen
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class MapValuesByTime extends Mapper<Text,BytesWritable,LongWritable,BytesWritable>{


	/**
	 * construct the map instance (do nothing)
	 */
	public MapValuesByTime() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setup(Mapper<Text,BytesWritable,LongWritable,BytesWritable>.Context context) throws IOException, InterruptedException {
		loadOptions(context);
	}
	private static String[] options;
	private static HashMap<String, IndependentPair<Long, Long>> wordIndex;

	protected static synchronized void loadOptions(Mapper<Text,BytesWritable,LongWritable,BytesWritable>.Context context) throws IOException {
		if (options == null) {
			try {
				options = context.getConfiguration().getStrings(Values.ARGS_KEY);
				wordIndex = WordIndex.readWordCountLines(options[0]);
				System.out.println("Wordindex loaded: " + wordIndex.size());
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	public void map(final Text key, BytesWritable value, final Mapper<Text,BytesWritable,LongWritable,BytesWritable>.Context context) throws IOException, InterruptedException{
		try {
			if(!wordIndex.containsKey(key.toString())) return;
			System.out.println("Mapping values for word: " + key);
			final int wordI = (int)((long)(wordIndex.get(key.toString()).secondObject()));
			IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
				@Override
				protected Object readValue(DataInput in) throws IOException {
					WordDFIDF idf = new WordDFIDF();
					idf.readBinary(in);
					System.out.println("... Found (" + key + ") at time: " + idf.timeperiod);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(baos);
					idf.writeBinary(dos);
					dos.writeInt(wordI);
					dos.flush();
					dos.close();
					try {
						context.write(new LongWritable(idf.timeperiod), new BytesWritable(baos.toByteArray()));
					} catch (InterruptedException e) {
						throw new IOException(e);
					}
					return new Object();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldnt read word or timeperiod from word: " + key);
		}

	}
}