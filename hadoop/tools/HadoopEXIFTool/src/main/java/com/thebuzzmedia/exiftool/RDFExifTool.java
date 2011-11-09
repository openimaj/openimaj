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
package com.thebuzzmedia.exiftool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;

public class RDFExifTool extends ExifTool {
	
	private String nameSpace;

	public RDFExifTool(String rdfNameSpace){
		super();
		this.nameSpace = rdfNameSpace;
	}
	
	public String getImageRDF(File image, Tag... tags)
			throws IllegalArgumentException, SecurityException, IOException {
		return getImageRDF(image, Format.NUMERIC, tags);
	}
	
	public String getImageRDF(File image, String realName, Tag... tags) throws IllegalArgumentException, SecurityException, IOException {
		return getImageRDF(image, Format.NUMERIC, realName,tags);
	}
	
	public String getImageRDF(File image, Format format,  Tag... tags) throws IllegalArgumentException, SecurityException, IOException{
		return getImageRDF(image, Format.NUMERIC, image.getName(),tags);
	}
	public String getImageRDF(File image, Format format, String realName, Tag... tags)
			throws IllegalArgumentException, SecurityException, IOException {
		if (image == null)
			throw new IllegalArgumentException(
					"image cannot be null and must be a valid stream of image data.");
		if (format == null)
			throw new IllegalArgumentException("format cannot be null");
		if (tags == null || tags.length == 0)
			throw new IllegalArgumentException(
					"tags cannot be null and must contain 1 or more Tag to query the image for.");
		if (!image.canRead())
			throw new SecurityException(
					"Unable to read the given image ["
							+ image.getAbsolutePath()
							+ "], ensure that the image exists at the given path and that the executing Java process has permissions to read it.");

		long startTime = System.currentTimeMillis();

		if (DEBUG)
			log("Querying %d tags from image: %s", tags.length,
					image.getAbsolutePath());

		long exifToolCallElapsedTime = 0;

		List<String> args = new ArrayList<String>();

		// Clear process args
		args.clear();
		log("\tUsing ExifTool in non-daemon mode (-stay_open False)...");

		/*
		 * Since we are not using a stayOpen process, we need to setup the
		 * execution arguments completely each time.
		 */
		args.add(EXIF_TOOL_PATH);

		args.add("-X");

		for (int i = 0; i < tags.length; i++)
			args.add("-" + tags[i].getName());

		args.add(image.getAbsolutePath());

		// Run the ExifTool with our args.
		BufferedReader reader = startRDFExifToolProcess(args);

		// Begin tracking the duration ExifTool takes to respond.
		exifToolCallElapsedTime = System.currentTimeMillis();

		String line;
		StringBuilder builder = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append('\n');
		}

		// Print out how long the call to external ExifTool process took.
		log("\tFinished reading ExifTool response in %d ms.",
				(System.currentTimeMillis() - exifToolCallElapsedTime));
		reader.close();

		if (DEBUG)
			log("\tImage Meta Processed in %d ms [queried %d tags]",
					(System.currentTimeMillis() - startTime), tags.length);

		String rdfString = builder.toString();
		Model model = ModelFactory.createDefaultModel();
		model.read(new StringReader(rdfString), String.format("%s#",nameSpace));
		
		String queryString = "SELECT ?o {?o ?p ?v}";
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query,model);
		
		try {
			ResultSet results = qexec.execSelect();
			for(;results.hasNext();){
				QuerySolution soln = results.nextSolution();
				Resource r = soln.getResource("o");
				log("Renaming resource");
				ResourceUtils.renameResource(r, String.format("%s#%s",nameSpace,realName));
			}
		} 
		catch (Exception e) {}
		finally {qexec.close();}
		StringWriter writer = new StringWriter();
		model.write(writer);
		return writer.toString();
	}

	private BufferedReader startRDFExifToolProcess(List<String> args) {
		Process proc = null;

		log("\tAttempting to start external ExifTool process using args: %s",
				args);

		try {
			proc = new ProcessBuilder(args).start();
			log("\t\tSuccessful");
		} catch (Exception e) {
			String message = "Unable to start external ExifTool process using the execution arguments: "
					+ args
					+ ". Ensure ExifTool is installed correctly and runs using the command path '"
					+ EXIF_TOOL_PATH
					+ "' as specified by the 'exiftool.path' system property.";

			log(message);
			throw new RuntimeException(message, e);
		}

		log("\tSetting up Read/Write streams to the external ExifTool process...");
		log("\t\tSuccessful, returning streams to caller.");
		return new BufferedReader(new InputStreamReader(proc.getInputStream()));
	}

}
