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
package org.openimaj.hadoop.mapreduce.stage;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.openimaj.util.reflection.ReflectionUtils;

import com.hadoop.mapreduce.LzoTextInputFormat;

/**
 * A stage in a multi step job. Each step is told where the jobs data will come from, where the output
 * should be directed and then is expected to produce a stage. The job is configured and set up based on
 * the generic types assigned to the stage. For most jobs these generics and providing the mapper/reducer classes
 * should be enough. If any further settings need to be configured use the {@link #setup(Job)} which is called before the
 * job is being returned
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 * @param <INPUT_FORMAT> The job's input format. Must be a {@link FileOutputFormat}. Used to {@link FileInputFormat#setInputPaths(Job, Path...)} with the stage's input locations
 * @param <OUTPUT_FORMAT> The job's output format. Must be a {@link FileOutputFormat}. Used to {@link FileOutputFormat#setOutputPath(Job, Path)} with the stage's output location
 * @param <INPUT_KEY> The key format of the input to the map task 
 * @param <INPUT_VALUE> The value format of the input to the map task
 * @param <MAP_OUTPUT_KEY> The key format of the output of the map task (and therefore the input of the reduce)
 * @param <MAP_OUTPUT_VALUE> The value format of the output of the map task (and therefore the input of the reduce)
 * @param <OUTPUT_KEY> The key format of the output of the reduce task
 * @param <OUTPUT_VALUE> The valueformat of the output of the reduce task 
 *
 */

@SuppressWarnings({ "unused", "unchecked" })
public abstract class Stage<
	INPUT_FORMAT extends FileInputFormat<INPUT_KEY, INPUT_VALUE>,
	OUTPUT_FORMAT extends FileOutputFormat<OUTPUT_KEY, OUTPUT_VALUE>,
	INPUT_KEY, INPUT_VALUE,
	MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE,
	OUTPUT_KEY, OUTPUT_VALUE
>
{
	private Class<INPUT_FORMAT> inputFormatClass;
	private Class<OUTPUT_FORMAT> outputFormatClass;
	
	private Class<INPUT_VALUE> inputValueClass;
	private Class<INPUT_KEY> inputKeyClass;
	private Class<MAP_OUTPUT_KEY> mapOutputKeyClass;
	private Class<MAP_OUTPUT_VALUE> mapOutputValueClass;
	private Class<OUTPUT_KEY> outputKeyClass;
	private Class<OUTPUT_VALUE> outputValueClass;
	private List<Class<?>> genericTypes;

	/**
	 * Inititalise all the classes based on the generics
	 */
	public Stage() {
		this.genericTypes = ReflectionUtils.getTypeArguments(Stage.class, this.getClass());
		this.inputFormatClass = (Class<INPUT_FORMAT>) genericTypes.get(0);
		this.outputFormatClass = (Class<OUTPUT_FORMAT>) genericTypes.get(1);
		this.inputKeyClass = (Class<INPUT_KEY>) genericTypes.get(2);
		this.inputValueClass = (Class<INPUT_VALUE>) genericTypes.get(3);
		this.mapOutputKeyClass = (Class<MAP_OUTPUT_KEY>) genericTypes.get(4);
		this.mapOutputValueClass = (Class<MAP_OUTPUT_VALUE>) genericTypes.get(5);
		this.outputKeyClass = (Class<OUTPUT_KEY>) genericTypes.get(6);
		this.outputValueClass = (Class<OUTPUT_VALUE>) genericTypes.get(7);
	}
	
	/**
	 * @return the name of the output directory of this stage. If the name is null the directory itself is used. 
	 */
	public String outname(){
		return null;
	}
	/**
	 * @param inputs the input paths to be expected
	 * @param output the output location
	 * @param conf the job configuration
	 * @return the job to be launched in this stage
	 * @throws Exception 
	 * @throws IOException 
	 */
	public Job stage(Path[] inputs, Path output, Configuration conf) throws Exception{
		
		Job job = new Job(conf);
		// A bit of a dirty hack, if any input file is an lzo file sneakily switch the input format class to LZOTextInput 
		if(inputFormatClass.equals(TextInputFormat.class) && containsLZO(inputs)){
			job.setInputFormatClass(LzoTextInputFormat.class);
		}
		else{			
			job.setInputFormatClass(inputFormatClass);
		}
		job.setMapOutputKeyClass(mapOutputKeyClass);
		job.setMapOutputValueClass(mapOutputValueClass);
		job.setOutputKeyClass(outputKeyClass);
		job.setOutputValueClass(outputValueClass);
		job.setOutputFormatClass(outputFormatClass);
		
		job.setJarByClass(this.getClass());
		setInputPaths(job, inputs);
		setOutputPath(job, output);
		job.setMapperClass(mapper());
		job.setReducerClass(reducer());
		setup(job);
		return job;
	}
	
	private boolean containsLZO(Path[] inputs) {
		for (Path path : inputs) {
			if(path.getName().endsWith(".lzo"))return true;
		}
		return false;
	}

	/**
	 * Add any final adjustments to the job's config
	 * @param job
	 */
	public void setup(Job job){
	}
	
	/**
	 * By default this method returns the {@link NullMapper} class. This mapper outputs the values handed
	 * as they are. 
	 * @return the class of the mapper to use
	 */
	public Class<? extends Mapper<INPUT_KEY,INPUT_VALUE,MAP_OUTPUT_KEY,MAP_OUTPUT_VALUE>> mapper(){
		NullMapper<INPUT_KEY,INPUT_VALUE,MAP_OUTPUT_KEY,MAP_OUTPUT_VALUE> nr = new NullMapper<INPUT_KEY,INPUT_VALUE,MAP_OUTPUT_KEY,MAP_OUTPUT_VALUE>();
		return (Class<? extends Mapper<INPUT_KEY, INPUT_VALUE, MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE>>) nr.getClass();
	}
	/**
	 * By default this method returns the {@link NullReducer} class. This reducer outputs the values handed as they are. 
	 * @return the class of the reducer to use
	 */
	public Class<? extends Reducer<MAP_OUTPUT_KEY,MAP_OUTPUT_VALUE,OUTPUT_KEY,OUTPUT_VALUE>> reducer(){
		NullReducer<MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE, OUTPUT_KEY, OUTPUT_VALUE> nr = new NullReducer<MAP_OUTPUT_KEY,MAP_OUTPUT_VALUE,OUTPUT_KEY,OUTPUT_VALUE>();
		return (Class<? extends Reducer<MAP_OUTPUT_KEY, MAP_OUTPUT_VALUE, OUTPUT_KEY, OUTPUT_VALUE>>) nr.getClass();
	}
	

	private void setOutputPath(Job job, Path output){
		try {
			Method method = outputFormatClass.getMethod("setOutputPath", Job.class,Path.class);
			method.invoke(null, job,output);
		} catch (Exception e) {
			System.err.println("Couldn't set output path!");
		}
	}
	private void setInputPaths(Job job, Path[] inputs) {
		try {
			Method method = inputFormatClass.getMethod("setInputPaths", Job.class,Path[].class);
			method.invoke(null, job,inputs);
		} catch (Exception e) {
			System.err.println("Couldn't set input path!");
		}
	}
}