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
package org.openimaj.tools.imagecollection.collection.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Scanner;

import org.openimaj.io.ReadWriteableASCII;

import com.jayway.jsonpath.JsonPath;

/**
 * An ImageCollectionConfig can be written to and read from an ASCII str
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ImageCollectionConfig implements ReadWriteableASCII{

	
	private String json;

	public ImageCollectionConfig(){
		this.json = "{}";
	}
	

	public ImageCollectionConfig(String json) {
		this.json = json;
	}


	@Override
	public void readASCII(Scanner in) throws IOException {
		StringBuilder builder = new StringBuilder();
		while(in.hasNextLine())builder.append(in.nextLine());
		json = builder.toString();
		try {
			read("$");
		} catch (ParseException e) {
			throw new IOException("Could not validate json");
		}
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.print(json);
	}

	public <T> T read(String path) throws ParseException {
		@SuppressWarnings("unchecked")
		T i = (T)((Object)JsonPath.read(this.json, path));
		
		return i;
	}
	
	public boolean containsValid(String videoTag) {
		String r;
		try {
			r = read(videoTag);
		} catch (ParseException e) {
			return false;
		}
		return r!=null;
	}

	
}
