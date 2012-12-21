package org.openimaj.hadoop.tools.localfeature;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.demos.sandbox.vlad.VLADIndexer;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.feature.normalisation.HellingerNormaliser;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.image.feature.local.keypoints.FloatKeypoint;
import org.openimaj.image.feature.local.keypoints.Keypoint;

public class VLADExtractor extends Configured implements Tool {
	static enum COUNTERS {
		EMIT, NULL;
	}

	static class VLADMapper extends Mapper<Text, BytesWritable, Text, BytesWritable> {
		private VLADIndexer indexer;

		@Override
		protected void setup(Context context) throws IOException, InterruptedException
		{
			final Path p = new Path(
					"hdfs://seurat.ecs.soton.ac.uk/data/vlad64-pca128-pq16x8-indexer-mirflickr25k-sift1x.dat");
			final InputStream is = p.getFileSystem(context.getConfiguration()).open(p);
			indexer = VLADIndexer.read(is);
			is.close();
		}

		@Override
		protected void map(Text key, BytesWritable value, Context context)
				throws IOException, InterruptedException
		{
			final List<Keypoint> keys = MemoryLocalFeatureList.read(new ByteArrayInputStream(value.getBytes()),
					Keypoint.class);
			final MemoryLocalFeatureList<FloatKeypoint> fkeys =
					FloatKeypoint.convert(keys);

			for (final FloatKeypoint k : fkeys) {
				HellingerNormaliser.normalise(k.vector, 0);
			}

			final float[] vladData = indexer.extract(fkeys);

			if (vladData == null) {
				context.getCounter(COUNTERS.NULL).increment(1L);
				return;
			}

			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final DataOutputStream dos = new DataOutputStream(baos);
			for (final float f : vladData)
				dos.writeFloat(f);

			context.write(key, new BytesWritable(baos.toByteArray()));
			context.getCounter(COUNTERS.EMIT).increment(1L);
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		final Path[] paths = SequenceFileUtility.getFilePaths(
				"hdfs://seurat.ecs.soton.ac.uk/data/flickr-all-geo-16-46M-sift1x.seq", "part");
		final Path outputPath = new Path(
				"hdfs://seurat.ecs.soton.ac.uk/data/flickr-all-geo-vlad64-pca128-pq16x8-indexer-mirflickr25k-sift1x.seq");

		if (outputPath.getFileSystem(this.getConf()).exists(outputPath))
			outputPath.getFileSystem(this.getConf()).delete(outputPath, true);

		final Job job = TextBytesJobUtil.createJob(paths, outputPath, null, this.getConf());
		job.setJarByClass(this.getClass());
		job.setMapperClass(VLADMapper.class);
		job.setNumReduceTasks(0);

		SequenceFileOutputFormat.setCompressOutput(job, false);
		job.waitForCompletion(true);

		return 0;
	}

	public static void main(String[] args) throws Exception {
		ToolRunner.run(new VLADExtractor(), args);
	}
}
