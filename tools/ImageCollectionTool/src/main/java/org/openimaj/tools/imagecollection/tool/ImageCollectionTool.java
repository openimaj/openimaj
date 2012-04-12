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
package org.openimaj.tools.imagecollection.tool;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.io.IOUtils;
import org.openimaj.tools.imagecollection.collection.ImageCollection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionSetupException;
import org.openimaj.tools.imagecollection.collection.config.ImageCollectionConfig;
import org.openimaj.tools.imagecollection.collection.config.ImageCollectionMode;
import org.openimaj.tools.imagecollection.collection.config.ImageCollectionProcessorMode;
import org.openimaj.tools.imagecollection.collection.config.MetaMapperMode;
import org.openimaj.tools.imagecollection.metamapper.MetaMapper;
import org.openimaj.tools.imagecollection.processor.ImageCollectionProcessor;
import org.openimaj.tools.imagecollection.tool.ImageCollectionProcessorJob.ProcessorJobEvent;
import org.openimaj.tools.imagecollection.tool.ImageCollectionProcessorJob.ProcessorJobListener;

public class ImageCollectionTool<T extends Image<?,T>> implements ProcessorJobListener {
	@Option(name="--input", aliases="-i", required=false, usage="Input Config File (json)", metaVar="STRING")
	private String input = null;
	
	@Option(name="--input-string", aliases="-is", required=false, usage="Input Config String (json).", metaVar="STRING")
	private String inputString = null;
	
	@Option(name="--output-mode", aliases="-om", required=false, usage="Image Collection output mode", handler=ProxyOptionHandler.class)
	private ImageCollectionProcessorMode processorMode = ImageCollectionProcessorMode.DIR;
	private ImageCollectionProcessorMode.ModeOp processorModeOp;
	private ImageCollectionProcessor<MBFImage> processor = null;
	
	@Option(name="--collection-mode", aliases="-cm", required=false, usage="Image Collection to pass json to")
	private ImageCollectionMode collectionMode = null;
	private ImageCollection<MBFImage> collection = null;
	
	@Option(name="--mapper-mode", aliases="-mm", required=false, usage="Imge Collection entry metadata mapper", handler=ProxyOptionHandler.class)
	private MetaMapperMode mapperMode = MetaMapperMode.FILE;
	private MetaMapper metaMapper;
	
	public void setup() throws IOException, ImageCollectionSetupException{
		ImageCollectionConfig config;
		if(input!= null){
			config = IOUtils.read(new File(input),ImageCollectionConfig.class);
		}
		else if(inputString!= null){
			config = new ImageCollectionConfig(inputString);
		}
		else{
			try{
				config = IOUtils.read(System.in,ImageCollectionConfig.class);
			}
			catch(ClassCastException t){
				throw new IOException("Not parseable!");
			}
		}
		
		if(collectionMode==null){
			collection = ImageCollectionMode.guessType(config);
		}
		else{
			collection = collectionMode.initCollection(config);
		}
		
		if(collection == null){
			throw new IOException("Could not read collection");
		}
		
		this.processor = processorModeOp.processor();
		this.metaMapper = mapperMode.mapper(this.processor);
	}
	
	private void run() {
		ImageCollectionProcessorJob<MBFImage> job = new ImageCollectionProcessorJob<MBFImage>(
				this.collection,
				this.processor,
				this.metaMapper
		);
		job.addListener(this);
		Thread t = new Thread(job);
		t.start();
	}
	
	public static void main(String args[]){
		ImageCollectionTool<MBFImage> tool = new ImageCollectionTool<MBFImage>();
		CmdLineParser parser = new CmdLineParser(tool);
		
	    try {
		    parser.parseArgument(args);
		    tool.setup();
			tool.run();
		} catch(CmdLineException e) {
		    parserError(parser,e);
		} catch (IOException e) {
			parserError(parser,e);
		} catch (ImageCollectionSetupException e) {
			parserError(parser,e);
		}
		
		
	}

	private static void parserError(CmdLineParser parser, Exception e) {
		System.err.println(e.getMessage());
	    System.err.println("Usage: java -jar ImageCollectionTool.jar [arguments]");
	    parser.printUsage(System.err);
	    return;
	}

	@Override
	public void progressUpdate(ProcessorJobEvent event) {
		System.out.print("\r");
		StringBuilder progress = new StringBuilder();
		int progressLen = 100;
		if(event.validTotal){
			
			double progressProp = (double)event.imagesDone / (double)event.imagesTotal;
			progress.append(String.format("%s/%s",event.imagesDone,event.imagesTotal));
			progress.append('[');
			int currentProgress = (int) (progressLen * progressProp);
			for(int i = 0; i < currentProgress; i++){
				progress.append('#');
			}
			for(int i = currentProgress; i < progressLen; i++){
				progress.append(' ');
			}
		}
		else{
			progress.append(String.format("%s/%s",event.imagesDone,"?"));
			progress.append('[');
			int hashIndex = event.imagesDone % progressLen;
			if((event.imagesDone / progressLen) % 2 == 1){
				hashIndex = progressLen - hashIndex;
			}
			
			for(int i = 0; i < progressLen; i++){
				if(i == hashIndex)
					progress.append('#');
				else
					progress.append(' ');
			}
		}
		progress.append(']');
		System.out.print(progress.toString());
	}
}
