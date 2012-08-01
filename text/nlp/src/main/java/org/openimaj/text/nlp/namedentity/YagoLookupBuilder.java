package org.openimaj.text.nlp.namedentity;

import java.io.File;
import java.io.IOException;

import org.openimaj.io.FileUtils;

public class YagoLookupBuilder {
	
private static final String DEFAULT_MAP_TEXTFILE = "/YagoLookup/Alias.txt";

	
	/**
	 * Validate the (local) ouput from an String and return the 
	 * corresponding file.
	 * 
	 * @param out where the file will go
	 * @param overwrite whether to overwrite existing files
	 * @param contin whether an existing output should be continued (i.e. ignored if it exists)
	 * @return the output file location, deleted if it is allowed to be deleted
	 * @throws IOException if the file exists, but can't be deleted
	 */
	public static File validateLocalOutput(String out, boolean overwrite, boolean contin) throws IOException {
		if(out == null){
			throw new IOException("No output specified");
		}
		File output = new File(out);
		if(output.exists()){
			if(overwrite){
				if(!FileUtils.deleteRecursive(output)) throw new IOException("Couldn't delete existing output");
			}
			else if(!contin){
				throw new IOException("Output already exists, didn't remove");
			}
		}
		return output;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String listPath = null;
		boolean verbose = true;
		if(args.length == 0){
			listPath = DEFAULT_MAP_TEXTFILE;
		}
		else{
			listPath = args[0];
		}
		File f = validateLocalOutput(listPath,true,false);
		f.createNewFile();
		new YagoLookupMapFactory(verbose).createListFileFromSparqlEndpoint(YagoQueryUtils.YAGO_SPARQL_ENDPOINT,listPath);
	}

}
