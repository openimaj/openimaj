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
package org.openimaj.hadoop.mapreduce.stage.helper;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.openimaj.hadoop.mapreduce.stage.Stage;

/**
 * A helper class for a common stage type. In this case, a stage that goes from
 * a sequence file to a sequence file with types
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <MAP_OUTPUT_KEY>
 *            The key format of the output of the map task (and therefore the
 *            input of the reduce)
 * @param <MAP_OUTPUT_VALUE>
 *            The value format of the output of the map task (and therefore the
 *            input of the reduce)
 * @param <OUTPUT_KEY>
 *            The key format of the output of the reduce task
 * @param <OUTPUT_VALUE>
 *            The valueformat of the output of the reduce task
 * 
 */
public abstract class TextSequenceFileStage<MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE, OUTPUT_KEY, OUTPUT_VALUE> extends Stage<
			TextInputFormat,
			SequenceFileOutputFormat<OUTPUT_KEY, OUTPUT_VALUE>,
			LongWritable, Text,
			MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE,
			OUTPUT_KEY, OUTPUT_VALUE>
{

}
