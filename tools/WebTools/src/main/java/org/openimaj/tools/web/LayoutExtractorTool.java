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
package org.openimaj.tools.web;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.web.layout.ElementInfo;
import org.openimaj.web.layout.LayoutExtractor;

/**
 * Tool for extracting information from rendered webpages. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class LayoutExtractorTool {
	private static final int THUMBNAIL_HEIGHT = 100;
	private static final int THUMBNAIL_WIDTH = 100;
	private static final Float[] NON_CONTENT_COLOUR = RGBColour.CYAN;
	private static final Float[] NON_CONTENT_INSIDE_COLOUR = RGBColour.RED;
	private static final Float[] CONTENT_COLOUR = RGBColour.GREEN;

	@Option(name = "--thumbnail", aliases="-t", usage = "Write a thumbnail image of the page", required=false)
	File thumbnailFile;
	
	@Option(name = "--render", aliases="-r", usage = "Write a rendered image of the page", required=false)
	File renderFile;
	
	@Option(name = "--layout", aliases="-l", usage = "Write the layout information in CSV format. Passing \"-\" will cause the data to be written to STDOUT", required=false)
	File layoutFile;
	
	@Option(name = "--layout-render", aliases="-lr", usage = "Write the layout information as an image", required=false)
	File layoutRender;
	
	@Option(name = "--layout-render-overlay", aliases="-lro", usage = "Write the layout information as an image, overlayed on a render of the page", required=false)
	File layoutRenderOverlayed;
	
	@Option(name = "--content-layout-render", aliases="-clr", usage = "Write the content layout information as an image", required=false)
	File contentLayoutRender;
	
	@Option(name = "--content-layout-render-overlay", aliases="-clro", usage = "Write the content layout information as an image, overlayed on a render of the page", required=false)
	File contentLayoutRenderOverlayed;
	
	@Argument()
	String url;
	
	LayoutExtractor extractor = new LayoutExtractor();
	MBFImage render;
	
	protected void writeLayout() throws IOException {
		List<ElementInfo> info = extractor.getLayoutInfo();
		PrintWriter pw;
		
		if (layoutFile.getName().equals("-")) {
			pw = new PrintWriter(System.out);
		} else {
			pw = new PrintWriter(new FileWriter(layoutFile));
		}
		
		pw.println(ElementInfo.getCSVHeader());
		for (ElementInfo ei : info) {
			pw.println(ei.toCSVString());
		}
		
		if (!layoutFile.getName().equals("-")) {
			pw.close();
		}
	}
		
	protected MBFImage getRender() {
		if (render == null)
			render = extractor.render();
		return render;
	}
	
	/**
	 * Extract content.
	 * @throws IOException
	 */
	public void extractContent() throws IOException {
		if (!extractor.load(url)) {
			System.err.println("Error loading page: " + url);
			System.exit(1);
		}
		
		if (layoutFile != null) writeLayout();
		
		if (thumbnailFile != null) {
			MBFImage image = getRender();
			
			//crop first if its very long
			if (image.getHeight() > 1.5 * image.getWidth()) {
				image = image.extractROI(0, 0, image.getWidth(), image.getWidth());
			}
			
			MBFImage thumb = image.process(new ResizeProcessor(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT));
			ImageUtilities.write(thumb, thumbnailFile);
		}
		
		if (renderFile != null) {
			ImageUtilities.write(getRender(), renderFile);
		}
		
		if (layoutRender != null) {
			ImageUtilities.write(extractor.renderLayoutInfo(RGBColour.BLACK), layoutRender);
		}
		
		if (layoutRenderOverlayed != null) {
			ImageUtilities.write(extractor.renderLayoutInfo(getRender(), RGBColour.RED), layoutRenderOverlayed);
		}
		
		if (contentLayoutRender != null) {
			ImageUtilities.write(extractor.renderContentLayout(CONTENT_COLOUR, NON_CONTENT_INSIDE_COLOUR, NON_CONTENT_COLOUR), contentLayoutRender);
		}
		
		if (contentLayoutRenderOverlayed != null) {
			ImageUtilities.write(extractor.renderContentLayout(getRender(), CONTENT_COLOUR, NON_CONTENT_INSIDE_COLOUR, NON_CONTENT_COLOUR), contentLayoutRenderOverlayed);
		}
	}
	
	/**
	 * Main method
	 * @param args
	 * @throws IOException
	 */
	public static void main(String [] args) throws IOException {
		System.setOut(new PrintStream(System.out, true, "UTF-8"));
		
		LayoutExtractorTool extractor = new LayoutExtractorTool();
		CmdLineParser parser = new CmdLineParser(extractor);
		
		try {
		    parser.parseArgument(args);
		} catch(CmdLineException e) {
		    System.err.println(e.getMessage());
		    System.err.println("Usage: java -jar LayoutExtractor.jar [options...]");
		    parser.printUsage(System.err);
		    return;
		}
		
		extractor.extractContent();
	}
}
