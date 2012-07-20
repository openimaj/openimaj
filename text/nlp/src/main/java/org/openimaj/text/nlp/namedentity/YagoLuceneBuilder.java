package org.openimaj.text.nlp.namedentity;

import java.io.File;
import java.io.IOException;

import org.openimaj.text.nlp.namedentity.YagoCompanyIndexFactory.YagoCompanyIndex;

public class YagoLuceneBuilder {
	
	private static String indexPath = "src/main/resources/org/openimaj/text/namedentity/yagolucene";

	
	public static void main(String[] args) {
		File f = new File(indexPath);
		if(f.isDirectory()){
			for(File ff : f.listFiles()){
				ff.delete();
			}
			try {
				YagoCompanyIndex yci = YagoCompanyIndexFactory.createFromSparqlEndPoint(YagoCompanyIndexFactory.YAGO_SPARQL_ENDPOINT, indexPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}

}
