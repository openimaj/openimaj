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
		private String output = "./out";
		
		@Option(name="--force", aliases="-f", required=false, usage="force delete existing files (if any)", metaVar="STRING")
		private boolean force = true;
		
		@Option(name="--image-name", aliases="-im", required=false, usage="image naming format, %s replaced with image name", metaVar="STRING")
		private String imageNameFormat = "%s.png";
		
		@Override
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
