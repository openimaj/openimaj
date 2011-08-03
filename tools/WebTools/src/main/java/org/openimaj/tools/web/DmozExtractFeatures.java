package org.openimaj.tools.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.web.layout.ElementInfo;
import org.openimaj.web.layout.LayoutExtractor;

public class DmozExtractFeatures {
	final static String csvregex = ",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))";

	public static void main(String[] args) throws IOException {
		File inputCSV = new File("/Users/jsh2/Desktop/NewWebAnalysis/dmoz-content.csv");
		File outputDirBase = new File("/Users/jsh2/Desktop/NewWebAnalysis/extracted");
		
//		File inputCSV = new File(args[0]);
//		File outputDirBase = new File(args[1]);

		System.setOut(new PrintStream(System.out, true, "UTF-8"));


		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputCSV), "UTF-8"));

		String it;
		while ( (it = br.readLine()) != null) {
			String[] parts = it.split(csvregex);
			
			String url = parts[2];				
			System.out.println(url);

			File dir = new File(outputDirBase, parts[0].replace("\"", "") + "/" + parts[1] + "/" + url.replace(":", "|").replace("/", "_"));
			File layoutfile = new File(dir, "layout.csv");
			File imagefile = new File(dir, "render.png");


			if (layoutfile.exists()) 
				continue;

			LayoutExtractor le = new LayoutExtractor(30000L); //timeout after 30s
			if (le.load(url)) {
				dir.mkdirs();
				PrintWriter layoutfilePW = new PrintWriter(new FileWriter(layoutfile));

				List<ElementInfo> info = le.getLayoutInfo();
				layoutfilePW.println(ElementInfo.getCSVHeader());
				for (ElementInfo ei : info) {
					layoutfilePW.println(ei.toCSVString());
				}

				layoutfilePW.close();

				MBFImage image = le.render(1024,768);
				if (image != null)
					ImageUtilities.write(image, imagefile);
			}
		}
	}
}
