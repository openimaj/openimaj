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
	private int seen = 0;

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
	public String process(ImageCollectionEntry<T> image) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageUtilities.write(image.image, "png", bos);
		BytesWritable bw = new BytesWritable(bos.toByteArray());
		String imageName = "" + this.seen ;
		utility.appendData(new Text(imageName), bw);
		this.seen ++;
		return imageName;
	}
	
	@Override
	public void end() throws IOException{
		utility.close();
	}

}
