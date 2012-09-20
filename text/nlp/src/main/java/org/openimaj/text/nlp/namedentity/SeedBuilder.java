package org.openimaj.text.nlp.namedentity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

/**
 * Builds the seed directory required by {@link EntityExtractionResourceBuilder}.
 * This should only be done as a once off, then keep the seed directory.
 * That is why it is so hacky.
 * 
 * Usage:
 * 1)Create a directory in the decompressed Yago tsv folder called "seedDirectory".
 * 2) grep type_star.tsv for:
 * 	a)wordnet_organization_108008335 into a file called wordnet_organization_108008335.txt inside the seedDirectory.
 * 	b)wordnet_person_100007846 into a file called wordnet_person_100007846.txt inside the seedDirectory.
 * 	c)wordnet_location_100027167 into a file called wordnet_location_100027167.txt inside the seedDirectory.
 * 3) run main with the path of the tsv directory as an argument. (use -Xmx2g, 3g if possible)
 * 4) seedDirectory is now ready to be passed to {@link EntityExtractionResourceBuilder} as an argument to build the resources.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class SeedBuilder {
	private String yagoDirectory;
	private static String seedDirectory = "seedDirectory";
	

	

	private SeedBuilder(String yagoTSVDirectory) {
		this.yagoDirectory=yagoTSVDirectory;
	}

	/**
	 * @param args = path to the Yago2 tsv directory.
	 */
	public static void main(String[] args) {
		SeedBuilder sb = new SeedBuilder(args[0]);
		sb.build();
	}
	
	@SuppressWarnings("javadoc")
	public void build(){
		System.out.println("Building hash...");
		HashSet<String> filters = null;
		try {
			filters = buildEntityHash(yagoDirectory);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Total Entities : " + filters.size());
		FileFilterer ff = new FileFilterer(
				yagoDirectory+File.separator+"means.tsv",
				yagoDirectory+File.separator+seedDirectory+File.separator+"means_stripped.tsv",
				filters) {
			@Override
			protected String getCompareValue(String line) {
				String[] values = line.split("\\s+");
				return values[2];
			}
		};
		ff.filter();
		ff = new FileFilterer(
				yagoDirectory+File.separator+"isCalled.tsv",
				yagoDirectory+File.separator+seedDirectory+File.separator+"isCalled_stripped.tsv",
				filters) {
			@Override
			protected String getCompareValue(String line) {
				String[] values = line.split("\\s+");
				return values[1];
			}
		};
		ff.filter();
		ff = new FileFilterer(
				yagoDirectory+File.separator+"created.tsv",
				yagoDirectory+File.separator+seedDirectory+File.separator+"created_stripped.tsv",
				filters) {
			@Override
			protected String getCompareValue(String line) {
				String[] values = line.split("\\s+");
				return values[1];
			}
		};
		ff.filter();
		ff = new FileFilterer(
				yagoDirectory+File.separator+"hasWikipediaUrl.tsv",
				yagoDirectory+File.separator+seedDirectory+File.separator+"hasWikipediaUrl_stripped.tsv",
				filters) {
			@Override
			protected String getCompareValue(String line) {
				String[] values = line.split("\\s+");
				return values[1];
			}
		};
		ff.filter();
		ff = new FileFilterer(
				yagoDirectory+File.separator+"hasAnchorText.tsv",
				yagoDirectory+File.separator+seedDirectory+File.separator+"hasAnchorText_stripped.tsv",
				filters) {
			@Override
			protected String getCompareValue(String line) {
				String[] values = line.split("\\s+");
				return values[1];
			}
		};
		ff.filter();
		ff = new FileFilterer(
				yagoDirectory+File.separator+"hasWikipediaAnchorText.tsv",
				yagoDirectory+File.separator+seedDirectory+File.separator+"hasWikipediaAnchorText_stripped.tsv",
				filters) {
			@Override
			protected String getCompareValue(String line) {
				String[] values = line.split("\\s+");
				return values[1];
			}
		};
		ff.filter();
		System.out.println("Done");
	}

	private  HashSet<String> buildEntityHash(String directoryPath) throws FileNotFoundException {
		HashSet<String> result = new HashSet<String>();
		BufferedReader org = openIn(directoryPath + File.separator
				+ seedDirectory+File.separator+"wordnet_organization_108008335.txt");
		String s = null;
		int clashes = 0;
		try {
			while ((s = org.readLine()) != null) {
				String[] values = s.split("\\s+");
				if(result.contains(values[1])){
					clashes++;
				}
				else result.add(values[1]);
			}
			org.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		BufferedReader per = openIn(directoryPath + File.separator
				+seedDirectory+File.separator+ "wordnet_person_100007846.txt");
		s = null;
		try {
			while ((s = per.readLine()) != null) {
				String[] values = s.split("\\s+");
				if(result.contains(values[1])){
					clashes++;
				}
				else result.add(values[1]);
			}
			per.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedReader loc = openIn(directoryPath + File.separator
				+seedDirectory+File.separator+ "wordnet_location_100027167.txt");
		s = null;
		try {
			while ((s = loc.readLine()) != null) {
				String[] values = s.split("\\s+");
				if(result.contains(values[1])){
					clashes++;
				}
				else result.add(values[1]);
			}
			loc.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		System.out.println("Ent Clashes: "+clashes);
		return result;
	}

	

	private static BufferedReader openIn(String path) throws FileNotFoundException {
		FileReader fr = null;
		fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		return br;
	}

	

	
	
	private abstract class FileFilterer{
		
		private HashSet<String> filterValues;
		private BufferedReader in;
		private BufferedWriter out;
		private String inString;
		
		public FileFilterer(String fileToFilter, String filteredResultsFile,
				HashSet<String> validFilterValues) {
			this.filterValues = validFilterValues;
			try {
				in = openIn(fileToFilter);
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			}
			openOut(filteredResultsFile);
			inString = fileToFilter;
		}
		
		private void openOut(String filteredResultsFile) {
			FileWriter fw = null;
			try {
				fw = new FileWriter(filteredResultsFile);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			out = new BufferedWriter(fw);
			try {
				out.write("");
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		
		private void filter() {
			String s;
			System.out.println("Filtering : " + inString);
			int count = 0;
			int vcount = 0;
			try {
				while ((s = in.readLine()) != null) {
					count++;
					if (filterValues.contains(getCompareValue(s))) {
						out.append(s + "\n");
						vcount++;
					}
				}
			} catch (IOException e) {			
				e.printStackTrace();
			}
			try {
				in.close();
				out.flush();
				out.close();
			} catch (IOException e) {			
				e.printStackTrace();
			}
			System.out.println("Finished : " + inString + "\nFiltered " + count
					+ " to " + vcount);
		}
		
		protected abstract String getCompareValue(String line);
		
	}
}
