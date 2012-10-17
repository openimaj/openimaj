/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.kestrel.writing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.RiotWriter;

import backtype.storm.tuple.Fields;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;

/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NTripleWritingScheme implements WritingScheme{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2734506908903229738L;
//	private KryoValuesSerializer serializer;
//	private KryoValuesDeserializer deserializer;
	
	@Override
	public byte[] serialize(List<Object> objects) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Graph graph = GraphFactory.createGraphMem();
		for (Object object : objects) {
			Triple triple = (Triple)object;
			graph.add(triple);
		}
		RiotWriter.writeTriples(os, graph);
		try {
			os.flush();
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		byte[] ret = os.toByteArray();
		
		return ret;
	}
	@Override
	public List<Object> deserialize(byte[] ser) {
		ByteArrayInputStream bais = new ByteArrayInputStream(ser);
		final Object triples = new ArrayList<Object>();
		RiotReader.createParserNTriples(bais, new Sink<Triple>() {
			
			@Override
			public void close() {
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public void send(Triple item) {
				((List<Object>)triples).add(item);
			}
			
			@Override
			public void flush() {
			}
		}).parse();
		return Arrays.asList(triples);
	}
	@Override
	public Fields getOutputFields() {
		return new Fields("triples");
	}
}
