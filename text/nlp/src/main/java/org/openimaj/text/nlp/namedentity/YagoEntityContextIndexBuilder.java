package org.openimaj.text.nlp.namedentity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.openimaj.io.FileUtils;
import org.openimaj.text.nlp.namedentity.YagoEntityContextScorerFactory.YagoEntityContextScorer;

/**
 * Tool for building Lucene Indexes for {@link YagoEntityContextScorerFactory} to build {@link YagoEntityContextScorer}
 */
public class YagoEntityContextIndexBuilder {
	
	private static final String DEFAULT_INDEX_DIRECTORY = File.separator+".YagoWikiIndex";
	private static String FROM_ENDPOINT ="-from";
	private static String TO_PATH ="-to";
	private static String HELP="-help";
	private static Object QUIET="-quiet";


		
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
	        
			String to = null;
			String from = null;
			boolean verbose = true;
			if(args.length == 0){
				buildDefault();
			}
			else{
				ArrayList<String> gs = new ArrayList<String>(Arrays.asList(args));
				for(int i = 0; i<gs.size();i++){
					String c = gs.get(i);
					if(c.equals(FROM_ENDPOINT)){
						from = gs.get(i+1);
						i++;
						continue;
					}
					else if(c.equals(TO_PATH)){
						to = gs.get(i+1);
						i++;
						continue;
					}
					else if(c.equals(HELP)){
						printUsage();
					}
					else if(c.equals(QUIET)){
						verbose=false;
					}
					else{
						invalidArgument(c);
					}
				}
				if(to==null)to = getDefaultMapFilePath();
				if(from==null)from = YagoQueryUtils.YAGO_SPARQL_ENDPOINT;
				File f = validateLocalOutput(to,true,false);
				f.mkdirs();
				YagoEntityContextScorerFactory yf = new YagoEntityContextScorerFactory(verbose);
				try {
					yf.createFromSparqlEndPoint(YagoQueryUtils.YAGO_SPARQL_ENDPOINT, to);
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}			
		}

		private static void invalidArgument(String c) {
			System.out.println("INVALID ARGUMENT: "+c);
			printUsage();
			System.exit(1);
		}

		private static void printUsage() {
			System.out.println("Usage:");
		}
		
		/**
		 * @return = default path to the text file for building the HashMap
		 */
		public static String getDefaultMapFilePath(){
			return System.getProperty("user.home")+DEFAULT_INDEX_DIRECTORY;
		}
		
		/**
		 * Builds the Lucene index in the default location in the home directory
		 * @throws IOException
		 */
		public static void buildDefault() throws IOException{
			File f = validateLocalOutput(getDefaultMapFilePath(),true,false);
			f.mkdirs();
			YagoEntityContextScorerFactory yf = new YagoEntityContextScorerFactory(true);
			try {
				yf.createFromSparqlEndPoint(YagoQueryUtils.YAGO_SPARQL_ENDPOINT, getDefaultMapFilePath());
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}	

}
