package org.openimaj.hadoop.mapreduce.stage;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

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
	private Map<String,Class<?>> ptypeMap = null;
	private TypeVariable<?>[] types;

	/**
	 * Inititalise all the classes based on the generics
	 */
	public Stage() {
		this.inputFormatClass = getGenericClass(0);
		this.outputFormatClass = getGenericClass(1);
		this.inputKeyClass = getGenericClass(2);
		this.inputValueClass = getGenericClass(3);
		this.mapOutputKeyClass = getGenericClass(4);
		this.mapOutputValueClass = getGenericClass(5);
		this.outputKeyClass = getGenericClass(6);
		this.outputValueClass = getGenericClass(7);
	}
	
	/**
	 * @return the name of the output directory of this stage
	 */
	public abstract String outname();
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
		job.setInputFormatClass(inputFormatClass);
		job.setMapOutputKeyClass(mapOutputKeyClass);
		job.setMapOutputValueClass(mapOutputValueClass);
		job.setOutputKeyClass(outputKeyClass);
		job.setOutputValueClass(outputValueClass);
		job.setOutputFormatClass(outputFormatClass);
		
		setInputPaths(job, inputs);
		setOutputPath(job, output);
		job.setMapperClass(mapper());
		job.setReducerClass(reducer());
		setup(job);
		return job;
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
	
	private <T> Class<T> getGenericClass(int n){
		
		if(ptypeMap == null){
			ptypeMap = new HashMap<String,Class<?>>();
			Type inputFormatClassT = getClass().getGenericSuperclass();
			if(inputFormatClassT == null){
			}
			// From the class go down the tree while we havn't found stage
			// stage is the ParameterizedType that has the class stage
			while(!(inputFormatClassT instanceof ParameterizedType) || ((ParameterizedType)inputFormatClassT).getRawType() != Stage.class){
				if(inputFormatClassT== null)
				{
					// Generics not provided, null. Let's hope they overwrote #stage or this will end very badly
					System.err.println("WARNING: no generics given, assuming stage() has been provided");
					return null;
				}
				
				// If we have found another ParameterizedType on the way, then we must register its classes
				if(inputFormatClassT instanceof ParameterizedType)
				{
					registerPType((ParameterizedType) inputFormatClassT);
					inputFormatClassT = ((ParameterizedType)inputFormatClassT).getRawType();
				}
				// Go up one more level
				inputFormatClassT = ((Class<?>)inputFormatClassT).getGenericSuperclass();
			}
			// Assuming we've found stage, see what types have made it this far!
			ParameterizedType ptype = (ParameterizedType)inputFormatClassT;
			registerPType(ptype);
			Type[] currentVals = ((Class<?>)ptype.getRawType()).getTypeParameters();
			Type[] paramVals = ptype.getActualTypeArguments();
			this.types = new TypeVariable[paramVals.length];
			for (int i = 0; i < types.length; i++) {
				Type pVal = paramVals[i];
				if(pVal instanceof ParameterizedType || pVal instanceof Class<?>){
					// Then the type came from this class, we can carry on
					this.types[i] = (TypeVariable<?>) currentVals[i] ;
				}
				else{
					// The type came from another place, replace it!
					this.types[i] = (TypeVariable<?>) pVal;
				}
			}
		}
		if(types == null) return null;
		if(n < 0 || n > types.length){
			return null;
		}
		TypeVariable<?> type = types[n];
		Class<?> toret = this.ptypeMap.get(type.toString());
		return (Class<T>) toret;
	}

	/**
	 * Given a ParameterizedType find all the types which it specifies and hold its classes
	 * If a given type is defined by another ParameterizedType then we hold its raw type
	 * If a given type is neither a ParameterizedType or a Class that means it was defined further up the tree and must
	 * already be registered
	 * @param inputFormatClassT
	 */
	private void registerPType(ParameterizedType inputFormatClassT) {
		Class<?> c = (Class<?>) inputFormatClassT.getRawType();
		Type[] paramVals = inputFormatClassT.getActualTypeArguments();
		TypeVariable<?>[] params = c.getTypeParameters();
		
		for (int i = 0; i < params.length; i++) {
			String key = params[i].toString();
			if(ptypeMap.containsKey(key)) continue;
			Type type = paramVals[i];
			if(type instanceof ParameterizedType){
				ParameterizedType ptype = (ParameterizedType)type;
				ptypeMap.put(key, (Class<?>) ptype.getRawType());
			}
			else if(type instanceof Class<?>){
				ptypeMap.put(key, (Class<?>) type);
			}
			else{
				this.ptypeMap.put(key, this.ptypeMap.get(type.toString()));
			}
		}
		
	}
}