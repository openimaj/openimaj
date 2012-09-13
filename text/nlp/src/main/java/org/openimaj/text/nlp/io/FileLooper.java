package org.openimaj.text.nlp.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public abstract class FileLooper {
	BufferedReader reader;

	@SuppressWarnings("javadoc")
	public FileLooper(File file) throws FileNotFoundException {
		FileReader fr = new FileReader(file);
		this.reader = new BufferedReader(fr);
	}

	/**
	 * Iterates through each line to do the work.
	 */
	public void loop() {
		String s = null;
		try {
			while ((s = reader.readLine()) != null) {
				doWork(s);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Do what you want to each line here.
	 * 
	 * @param s
	 */
	protected abstract void doWork(String s);
}
