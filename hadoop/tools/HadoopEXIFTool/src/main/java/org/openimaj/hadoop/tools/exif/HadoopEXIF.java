package org.openimaj.hadoop.tools.exif;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;
import org.openimaj.hadoop.sequencefile.MetadataConfiguration;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.image.ImageUtilities;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifTool.Feature;
import com.thebuzzmedia.exiftool.ExifTool.Tag;

public class HadoopEXIF extends Configured implements Tool{
	private static final String ARGS_KEY = "clusterquantiser.args";

	public static class HadoopEXIFMapper extends Mapper<Text, BytesWritable, Text, BytesWritable>{
		
		
		private ExifTool tool;
//		private static ExifTool tool;
		public HadoopEXIFMapper(){}
		
		@Override
		protected void setup(Mapper<Text, BytesWritable, Text, BytesWritable>.Context context)throws IOException, InterruptedException{
			HadoopEXIFOptions options = new HadoopEXIFOptions(context.getConfiguration().getStrings(ARGS_KEY),false);
			options.prepare();
			System.setProperty("exiftool.path",options.getExifPath());
			tool = new ExifTool();
		}
//		
//		private synchronized static void loadExifTool(Mapper<Text, BytesWritable, Text, BytesWritable>.Context context) {
//			if(tool==null){
//				HadoopEXIFOptions options = new HadoopEXIFOptions(context.getConfiguration().getStrings(ARGS_KEY),false);
//				options.prepare();
//				System.setProperty("exiftool.path",options.getExifPath());
//				tool = new ExifTool();
//			}
//		}

		@Override
		protected void map(Text key, BytesWritable value, Mapper<Text, BytesWritable, Text, BytesWritable>.Context context) throws java.io.IOException, InterruptedException 
		{
			try{
				File tmp = File.createTempFile("prefix", ".image", new File("/tmp")); 
				FileOutputStream fos = new FileOutputStream(tmp);
				IOUtils.copyBytes(new ByteArrayInputStream(value.getBytes()), fos, context.getConfiguration());
				fos.close();
				Map<Tag, String> allExif = tool.getImageMeta(tmp.getAbsoluteFile(), ExifTool.Tag.values());
				tmp.delete();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				PrintWriter pw = new PrintWriter(bos);
				StringBuilder builder = new StringBuilder();
				for(Entry<Tag,String> entry: allExif.entrySet()){
					pw.print(entry.getKey());
					pw.print(' ');
					pw.print('"');
					pw.print(entry.getValue());
					pw.print('"');
					pw.println();
				}
				pw.close();
				context.write(key, new BytesWritable(bos.toByteArray()));
			}
			catch(Throwable e){
				System.err.println("... Problem with this image! Keeping Calm. Carrying on.");
				e.printStackTrace(System.err);
			}
		}
	}
	@Override
	public int run(String[] args) throws Exception {
		HadoopEXIFOptions options = new HadoopEXIFOptions(args,true);
		options.prepare();
//		String clusterFileString = options.getInputString();
		Path[] paths = options.getInputPaths();
		TextBytesSequenceFileUtility util = new TextBytesSequenceFileUtility(paths[0].toUri() , true);
		Map<String,String> metadata = new HashMap<String,String>();
		if (util.getUUID() != null) metadata.put(MetadataConfiguration.UUID_KEY, util.getUUID());
		metadata.put(MetadataConfiguration.CONTENT_TYPE_KEY, "application/imageexif");
		
		Job job = TextBytesJobUtil.createJob(paths, options.getOutputPath(), metadata,this.getConf());
//		job.setOutputValueClass(Text.class);
		job.setJarByClass(this.getClass());
		job.setMapperClass(HadoopEXIF.HadoopEXIFMapper.class);
		job.getConfiguration().setStrings(ARGS_KEY, args);
		job.setNumReduceTasks(0);
		SequenceFileOutputFormat.setCompressOutput(job, false);
		long start,end;
		start = System.currentTimeMillis();
		job.waitForCompletion(true);
		end = System.currentTimeMillis();
		System.out.println("Took: " + (end - start) + "ms");
		return 0;
	}
	
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new HadoopEXIF(), args);
	}
}
