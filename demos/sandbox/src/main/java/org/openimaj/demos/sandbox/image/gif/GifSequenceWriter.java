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
package org.openimaj.demos.sandbox.image.gif;

// 
//  GifSequenceWriter.java
//  
//  Created by Elliot Kroo on 2009-04-25.
//
// This work is licensed under the Creative Commons Attribution 3.0 Unported
// License. To view a copy of this license, visit
// http://creativecommons.org/licenses/by/3.0/ or send a letter to Creative
// Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;

/**
 * A {@link GifSequenceWriter} uses java {@link ImageIO} to write animated gifs.
 * Taken wholesale from: http://elliot.kroo.net/software/java/GifSequenceWriter/
 * and modified to play nicely with openimaj
 * 
 * @author Elliot Kroo (elliot[at]kroo[dot]net), Sina Samangooei
 *         (ss@ecs.soton.ac.uk)
 * 
 */
public class GifSequenceWriter {
	protected ImageWriter gifWriter;
	protected ImageWriteParam imageWriteParam;
	protected IIOMetadata imageMetaData;
	private boolean delayedInit;
	private ImageOutputStream outputStream;
	private int timeBetweenFramesMS;
	private boolean loopContinuously;

	/**
	 * Creates a new GifSequenceWriter
	 * 
	 * @param outputStream
	 *            the ImageOutputStream to be written to
	 * @param imageType
	 *            one of the imageTypes specified in BufferedImage
	 * @param timeBetweenFramesMS
	 *            the time between frames in miliseconds
	 * @param loopContinuously
	 *            wether the gif should loop repeatedly
	 * @throws IIOException
	 *             if no gif ImageWriters are found
	 * @throws IOException
	 */
	public GifSequenceWriter(ImageOutputStream outputStream, int imageType,int timeBetweenFramesMS, boolean loopContinuously) throws IIOException, IOException {
		this.delayedInit = false;
		init(outputStream,imageType,timeBetweenFramesMS,loopContinuously);
	}
	
	/**
	 * Creates a new GifSequenceWriter. Not specifying the imageType means that initialisation of the writer is delayed until the first image
	 * is written
	 * 
	 * @param outputStream
	 *            the ImageOutputStream to be written to
	 * @param timeBetweenFramesMS
	 *            the time between frames in miliseconds
	 * @param loopContinuously
	 *            whether the gif should loop repeatedly
	 * @throws IIOException
	 *             if no gif ImageWriters are found
	 * @throws IOException
	 */
	public GifSequenceWriter(ImageOutputStream outputStream, int timeBetweenFramesMS, boolean loopContinuously) throws IIOException, IOException {
		this.delayedInit = true;
		this.outputStream = outputStream;
		this.timeBetweenFramesMS = timeBetweenFramesMS;
		this.loopContinuously = loopContinuously;
	}

	private void init(ImageOutputStream outputStream, int imageType,int timeBetweenFramesMS, boolean loopContinuously) throws IOException {
		// my method to create a writer
		gifWriter = getWriter();
		imageWriteParam = gifWriter.getDefaultWriteParam();
		ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);

		imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier,imageWriteParam);

		String metaFormatName = imageMetaData.getNativeMetadataFormatName();

		IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

		IIOMetadataNode graphicsControlExtensionNode = getNode(root,"GraphicControlExtension");

		graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
		graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
		graphicsControlExtensionNode.setAttribute("transparentColorFlag",
				"FALSE");
		graphicsControlExtensionNode.setAttribute("delayTime",
				Integer.toString(timeBetweenFramesMS / 10));
		graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

		IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
		commentsNode.setAttribute("CommentExtension", "Created by MAH");

		IIOMetadataNode appEntensionsNode = getNode(root,
				"ApplicationExtensions");

		IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");

		child.setAttribute("applicationID", "NETSCAPE");
		child.setAttribute("authenticationCode", "2.0");

		int loop = loopContinuously ? 0 : 1;

		child.setUserObject(new byte[] { 0x1, (byte) (loop & 0xFF),
				(byte) ((loop >> 8) & 0xFF) });
		appEntensionsNode.appendChild(child);

		imageMetaData.setFromTree(metaFormatName, root);

		gifWriter.setOutput(outputStream);

		gifWriter.prepareWriteSequence(null);
		delayedInit = false;
	}

	/**
	 * Write an image to the sequence
	 * 
	 * @param img
	 * @throws IOException
	 */
	public void writeToSequence(Image<?, ?> img) throws IOException {
		BufferedImage bimg = ImageUtilities.createBufferedImage(img);
		if(delayedInit){
			init(outputStream, bimg.getType(), timeBetweenFramesMS, loopContinuously);
		}
		writeToSequence(bimg);
	}

	private void writeToSequence(RenderedImage img) throws IOException {
		gifWriter.writeToSequence(new IIOImage(img, null, imageMetaData),
				imageWriteParam);
	}

	/**
	 * Close this GifSequenceWriter object. This does not close the underlying
	 * stream, just finishes off the GIF.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		gifWriter.endWriteSequence();
	}

	/**
	 * Returns the first available GIF ImageWriter using
	 * ImageIO.getImageWritersBySuffix("gif").
	 * 
	 * @return a GIF ImageWriter object
	 * @throws IIOException
	 *             if no GIF image writers are returned
	 */
	private static ImageWriter getWriter() throws IIOException {
		Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
		if (!iter.hasNext()) {
			throw new IIOException("No GIF Image Writers Exist");
		} else {
			return iter.next();
		}
	}

	/**
	 * Returns an existing child node, or creates and returns a new child node
	 * (if the requested node does not exist).
	 * 
	 * @param rootNode
	 *            the <tt>IIOMetadataNode</tt> to search for the child node.
	 * @param nodeName
	 *            the name of the child node.
	 * 
	 * @return the child node, if found or a new node created with the given
	 *         name.
	 */
	private static IIOMetadataNode getNode(IIOMetadataNode rootNode,
			String nodeName) {
		int nNodes = rootNode.getLength();
		for (int i = 0; i < nNodes; i++) {
			if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
				return ((IIOMetadataNode) rootNode.item(i));
			}
		}
		IIOMetadataNode node = new IIOMetadataNode(nodeName);
		rootNode.appendChild(node);
		return (node);
	}
	
	/**
	 * Write an array of images to a gif file. The type of the output gif is taken from the first frame.
	 * All future frames are assumed to be the same type.
	 * 
	 * @param frames
	 * @param timeBetweenFramesMS
	 * @param loopContinuously
	 * @param file 
	 * 
	 * @throws IOException 
	 */
	public static void writeGif(List<Image<?,?>> frames,int timeBetweenFramesMS, boolean loopContinuously,File file) throws IOException{
		ImageOutputStream output = new FileImageOutputStream(file);
		try {
			writeGif(frames,timeBetweenFramesMS,loopContinuously,output);
		} finally{
			output.close();
		}
	}
	
	/**
	 * Construct a {@link GifSequenceWriter} which writes to a file
	 * @param timeBetweenFramesMS
	 * @param loopContinuously
	 * @param f
	 * @return {@link GifSequenceWriter} instance 
	 * 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws IIOException 
	 */
	public static GifSequenceWriter createWriter(int timeBetweenFramesMS, boolean loopContinuously, File f) throws IIOException, FileNotFoundException, IOException {
		return new GifSequenceWriter(new FileImageOutputStream(f),timeBetweenFramesMS,loopContinuously);
	}
	
	/**
	 * Write an array of images to a gif output stream
	 * 
	 * @param frames
	 * @param timeBetweenFramesMS
	 * @param loopContinuously
	 * @param stream
	 * @throws IOException 
	 */
	public static void writeGif(List<Image<?,?>> frames,int timeBetweenFramesMS, boolean loopContinuously,OutputStream stream) throws IOException{
		ImageOutputStream output = new MemoryCacheImageOutputStream(stream);
		writeGif(frames,timeBetweenFramesMS,loopContinuously,output);
	}

	/**
	 * Write some frames into an {@link ImageOutputStream}
	 * @param frames
	 * @param timeBetweenFramesMS
	 * @param loopContinuously
	 * @param output
	 * @throws IOException
	 */
	public static void writeGif(List<Image<?, ?>> frames,int timeBetweenFramesMS, boolean loopContinuously,ImageOutputStream output) throws IOException {
		if(frames.size() == 0){
			throw new IOException("No frames!");
		}
		GifSequenceWriter writer = new GifSequenceWriter(output,timeBetweenFramesMS, loopContinuously);
		for (Image<?, ?> image : frames) {
			writer.writeToSequence(image);
		}
		writer.close();
	}
}
