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
import org.openimaj.tools.imagecollection.ImageCollectionMode;
import org.openimaj.tools.imagecollection.ImageCollectionProcessorJob;
import org.openimaj.tools.imagecollection.ImageCollectionProcessorJob.ProcessorJobEvent;
import org.openimaj.tools.imagecollection.ImageCollectionProcessorJob.ProcessorJobListener;
import org.openimaj.tools.imagecollection.ImageCollectionProcessorMode;
import org.openimaj.tools.imagecollection.collection.ImageCollection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionConfig;
import org.openimaj.tools.imagecollection.collection.ImageCollectionSetupException;
import org.openimaj.tools.imagecollection.processor.ImageCollectionProcessor;

public class ImageCollectionTool<T extends Image<?,T>> implements ProcessorJobListener {
	@Option(name="--input", aliases="-i", required=false, usage="Input Config File (json)", metaVar="STRING")
	private String input = null;
	
	@Option(name="--input-string", aliases="-is", required=false, usage="Input Config String (json).", metaVar="STRING")
	private String inputString = null;
	
	@Option(name="--output-mode", aliases="-om", required=false, usage="Image Collection output mode", handler=ProxyOptionHandler.class)
	private ImageCollectionProcessorMode processorMode = ImageCollectionProcessorMode.DIR;
	private ImageCollectionProcessor<MBFImage> processor = null;
	
	@Option(name="--collection-mode", aliases="-cm", required=false, usage="Image Collection to pass json to")
	private ImageCollectionMode collectionMode = null;
	private ImageCollection<MBFImage> collection = null;
	
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
		
		this.processor = processorMode.processor();
	}
	
	private void run() {
		ImageCollectionProcessorJob<MBFImage> job = new ImageCollectionProcessorJob<MBFImage>(this.collection,this.processor);
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
		double progressProp = (double)event.imagesDone / (double)event.imagesTotal;
		
		int progressLen = 100;
		int currentProgress = (int) (progressLen * progressProp);
		progress.append(String.format("%s/%s",event.imagesDone,event.imagesTotal));
		progress.append('[');
		for(int i = 0; i < currentProgress; i++){
			progress.append('#');
		}
		for(int i = currentProgress; i < progressLen; i++){
			progress.append(' ');
		}
		progress.append(']');
		
		System.out.println(progress.toString());
	}
}
