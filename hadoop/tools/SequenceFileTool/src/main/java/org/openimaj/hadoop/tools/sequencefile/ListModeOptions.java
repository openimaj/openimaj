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
package org.openimaj.hadoop.tools.sequencefile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.openimaj.hadoop.sequencefile.RecordInformationExtractor;
import org.semanticdesktop.aperture.mime.identifier.magic.MagicMimeTypeIdentifier;

/**
 * Options for controlling what is printed when listing the contents
 * of a {@link SequenceFile} with the {@link SequenceFileTool}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public enum ListModeOptions {
	/**
	 * Print the record key
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	KEY {
		@Override
		public RecordInformationExtractor getExtractor() {
			return new RecordInformationExtractor(){
				@Override
				public <K,V> String extract(K key, V value, long offset, Path seqFile) {
					return key.toString();
				}
			};
		}
	},
	/**
	 * Print the offset of the record in the {@link SequenceFile}
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	OFFSET {
		@Override
		public RecordInformationExtractor getExtractor() {
			return new RecordInformationExtractor(){
				@Override
				public <K,V> String extract(K key, V value, long offset, Path seqFile) {
					return ((Long)offset).toString();
				}
			};
		}		
	},
	/**
	 * Print the path to the {@link SequenceFile} in question. 
	 * This is useful if you're working with a directory of
	 * {@link SequenceFile}s
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	SEQUENCEFILE {
		@Override
		public RecordInformationExtractor getExtractor() {
			return new RecordInformationExtractor(){
				@Override
				public <K,V> String extract(K key, V value, long offset, Path seqFile) {
					return seqFile.toString();
				}
			};
		}
	},
	/**
	 * Print the mimetype of the value in each record
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	MIMETYPE {
		@Override
		public RecordInformationExtractor getExtractor() {
			return new RecordInformationExtractor(){
				@Override
				public <K,V> String extract(K key, V value, long offset, Path seqFile) {
					if(value instanceof BytesWritable) {
						MagicMimeTypeIdentifier match;
						try {
							BytesWritable bw = (BytesWritable)value;
							match = new MagicMimeTypeIdentifier ();
							String ident = match.identify(bw.getBytes(),key.toString(),new URIImpl(seqFile.toUri().toString()));
							return ident;
						} catch(Exception e){
							System.err.println("Failed!");
						}
					}
					return null;
				}
			};
		}
	},
	/**
	 * Print the size of the record value in bytes
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	SIZE {
		@Override
		public RecordInformationExtractor getExtractor() {
			return new RecordInformationExtractor(){
				@Override
				public <K,V> String extract(K key, V value, long offset, Path seqFile) {
					if(value instanceof BytesWritable) {
						return "" + ((BytesWritable)value).getLength();
					}
					return null;
				}
			};
		}
	},
	/**
	 * Print the dimensions of each records value if it is a
	 * valid image.
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	IMAGE_DIMENSIONS {
		@Override
		public RecordInformationExtractor getExtractor() {
			return new RecordInformationExtractor() {
				@Override
				public <K,V> String extract(K key, V value, long offset, Path seqFile) {
					if(value instanceof BytesWritable) {
						try {
							BufferedImage im = ImageIO.read(new ByteArrayInputStream(((BytesWritable) value).getBytes()));
							return String.format("%d %d", im.getWidth(), im.getHeight());
						} catch (IOException e) {
							return null;
						}
					}
					return null;
				}
			};
		}
	};
	
	/**
	 * @return a {@link RecordInformationExtractor} for extracting information from a {@link SequenceFile} record.
	 */
	public abstract RecordInformationExtractor getExtractor();

	/**
	 * Construct a list of extractors from the given options.
	 * 
	 * @param options the options
	 * @return the extractors in the same order as the given options
	 */
	public static List<RecordInformationExtractor> listOptionsToExtractPolicy(List<ListModeOptions> options) {
		List<RecordInformationExtractor> extractors = new ArrayList<RecordInformationExtractor>();
		
		for(ListModeOptions opt : options) 
			extractors.add(opt.getExtractor());
		
		return extractors;
	}
}
