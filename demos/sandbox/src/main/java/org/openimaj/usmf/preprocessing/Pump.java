package org.openimaj.usmf.preprocessing;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.GeneralJSONTwitter;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.StreamTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusList;

public class Pump implements Iterable{
	
	private TwitterStatusList<USMFStatus> statusList;
	private PipeSection<?,?> pipe;
	
	public Pump(InputStream stream, Class<? extends GeneralJSON> generalJSON, PipeSection<?,?> pipe){
		this.pipe = pipe;
		try {
			statusList = StreamTwitterStatusList.readUSMF(stream, generalJSON);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Iterator iterator() {
		return new PumpIterator(this);
	}
	
	private class PumpIterator implements Iterator{
		
		public PumpIterator(Pump source){
			
		}

		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Object next() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}
		
	}

}
