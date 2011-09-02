package org.openimaj.tools.imagecollection;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.openimaj.image.Image;
import org.openimaj.tools.imagecollection.processor.DirectoryImageProcessor;
import org.openimaj.tools.imagecollection.processor.ImageCollectionProcessor;
import org.openimaj.tools.imagecollection.processor.SequenceFileProcessor;

public enum ImageCollectionProcessorMode implements CmdLineOptionsProvider{
	DIR{
		@Option(name="--output-file", aliases="-o", required=false, usage="directory to output images", metaVar="STRING")
		private String output = ".";
		
		@Option(name="--force", aliases="-f", required=false, usage="force delete existing files (if any)", metaVar="STRING")
		private boolean force = true;
		
		@Option(name="--image-name", aliases="-im", required=false, usage="image naming format, %s replaced with image name", metaVar="STRING")
		private String imageNameFormat = "%s.png";
		
		public <T extends Image<?,T>> ImageCollectionProcessor<T> processor(){
			return new DirectoryImageProcessor<T>(output,force,imageNameFormat);
		}
	},
	
	SF{
		@Option(name="--output-file", aliases="-o", required=false, usage="sequence file output", metaVar="STRING")
		private String output = ".";
		
		@Option(name="--force", aliases="-f", required=false, usage="force delete existing files (if any)", metaVar="STRING")
		private boolean force = true;
		
		@Override
		public <T extends Image<?,T>> ImageCollectionProcessor<T> processor(){
			return new SequenceFileProcessor<T>(output,force);
		}
	};
	public abstract <T extends Image<?,T>> ImageCollectionProcessor<T> processor();
	@Override
	public Object getOptions() {
		return this;
	}

	

}
