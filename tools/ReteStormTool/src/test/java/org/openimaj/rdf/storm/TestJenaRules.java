package org.openimaj.rdf.storm;

import java.io.File;
import java.io.IOException;

import org.apache.thrift7.transport.TSocket;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.io.FileUtils;
import org.openimaj.rdf.storm.tool.ReteStorm;

public class TestJenaRules {

	/**
	 * the output folder
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	File rules;
	File triples;

	@Before
	public void init() throws IOException{

		rules = folder.newFile("test.rules");
		triples = folder.newFile("test.rdfs");

		FileUtils.copyStreamToFile(TestJenaRules.class.getResourceAsStream("/test.rules"), rules);
		FileUtils.copyStreamToFile(TestJenaRules.class.getResourceAsStream("/test.rdfs"), triples);

	}

	@Test
	public void testRules() throws Throwable
	{
		// Check if kestrel is available!
		try{
			TSocket t = new TSocket("127.0.0.1",22133);
			t.setTimeout(1000);
			t.open();
			if(!t.isOpen()){
				System.out.println("Kestrel server closed, can't test!");
				return;
			}
		}
		catch(Throwable t){
			return; // no kestrel server for test!
		}
		String[] cmds = new String[]{
				"-rl", "JENA",
				"-ffb", "-prepi",
				"-i", rules.getAbsolutePath(),
				"-tm", "LOCAL",
				"-us", "file://"+triples.getAbsolutePath(),
				"-kiq", "test_input",
				"-koq", "test_output",
				"-st", "20000"
		};

		ReteStorm.main(cmds);
	}
}
