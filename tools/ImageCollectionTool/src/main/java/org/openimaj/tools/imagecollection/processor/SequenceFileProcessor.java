package org.openimaj.tools.imagecollection.processor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;

public class SequenceFileProcessor<T extends Image<?, T>> extends ImageCollectionProcessor<T> {
	
	String sequenceFile = "output.seq";
	boolean force = true;
	private TextBytesSequenceFileUtility utility;

	public SequenceFileProcessor(String output, boolean force) {
		this.sequenceFile = output;
		this.force = force;
	}

	public static FileSystem getFileSystem(URI uri) throws IOException {
		Configuration config = new Configuration();
		FileSystem fs = FileSystem.get(uri, config);
		if (fs instanceof LocalFileSystem) fs = ((LocalFileSystem)fs).getRaw();
		return fs;
	}

	
	@Override
	public void start() throws IOException{
		if(force)
		{
			URI outuri = SequenceFileUtility.convertToURI(sequenceFile);
			FileSystem fs = getFileSystem(outuri);
			fs.delete(new Path(outuri.toString()), true);
		}
		
		utility = new TextBytesSequenceFileUtility(sequenceFile, false);
	}

	@Override
	public void process(ImageCollectionEntry<T> image) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageUtilities.write(image.image, "png", bos);
		BytesWritable bw = new BytesWritable(bos.toByteArray());
		utility.appendData(new Text(image.name), bw);
	}
	
	@Override
	public void end() throws IOException{
		utility.close();
	}

}
