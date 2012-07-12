package org.openimaj.usmf.preprocessing;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.text.nlp.namedentity.AnnieCompanyExtractor;
import org.openimaj.text.nlp.namedentity.YagoCompanyExtractor;
import org.openimaj.twitter.GeneralJSONTwitter;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.FileTwitterStatusList;

/**
 * @author laurence
 *	Testing all the PipeSections
 */
public class PipeTester {

	@Rule
	public static TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testThePipe() {
		
		//Build an array of the workflow pipe components.
		ArrayList<PipeSection<?,?>> workflow = new ArrayList<PipeSection<?,?>>();
		workflow.add(new TweetTokeniserPipe());
		workflow.add(new CompanyPipe(new AnnieCompanyExtractor()));
		workflow.add(new LanguagePipe());

		DefaultEmptyPipe<USMFStatus, USMFStatus> myPipe=null;
		try {
			myPipe = new DefaultEmptyPipe<USMFStatus, USMFStatus>(
					USMFStatus.class, USMFStatus.class, workflow);
		} catch (PipeSectionJoinException e) {
			
			e.printStackTrace();
		}

		File twitterfile = null;
		try {
			twitterfile = fileFromeStream(USMFStatus.class
					.getResourceAsStream("/org/openimaj/twitter/json_tweets.txt"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		FileTwitterStatusList<USMFStatus> status = FileTwitterStatusList.readUSMF(
				twitterfile, "UTF-8", GeneralJSONTwitter.class);
		for (USMFStatus twitterStatus : status) {
			if (twitterStatus.isInvalid())
				continue;			
			myPipe.pipe(twitterStatus);			
		}
	}

	private static File fileFromeStream(InputStream stream) throws IOException {
		File f = folder.newFile("broken_raw" + stream.hashCode() + ".txt");
		PrintWriter writer = new PrintWriter(new BufferedOutputStream(
				new FileOutputStream(f)));
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		String line = null;
		while ((line = reader.readLine()) != null) {
			writer.println(line);
		}
		writer.flush();
		writer.close();
		return f;
	}

}
