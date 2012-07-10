package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;

public class Rationaliser {
	
	ArrayList<Section> sections;

	public Rationaliser() {
	}

	public ArrayList<String> getCombinations(String word) {
		ArrayList<String> result = new ArrayList<String>();
		char[] characters = word.toCharArray();
		sections = new ArrayList<Section>();

		Character last = null;
		char lastCount = 0;
		StringBuffer unclassed = new StringBuffer();

		// Build Sections
		for (int i = 0; i < characters.length; i++) {
			char current = characters[i];
			// Process first char for new unclassed section.
			if (last == null) {
				last = current;
				lastCount++;
				continue;
			}
			// if we get a repeat
			if (last.equals(current)) {
				// eat if it is a repeat repeat
				if (lastCount > 1)
					continue;
				else {
					// put valid buffer in section
					if (unclassed.length() > 0) {
						StringBuffer pass = unclassed;
						unclassed = new StringBuffer();
						sections.add(new ValidSection(pass));
					}
					lastCount++;
					RepeatSection n = new RepeatSection(new StringBuffer(
							last.toString()));
					sections.add(n);
					continue;
				}
			}
			// if it is not a repeat
			if (lastCount == 1)unclassed.append(last);
			last = current;
			lastCount = 1;
		}
		if (lastCount == 1)unclassed.append(last);
		if(unclassed.length()>0) sections.add(new ValidSection(unclassed));
		
		//get all the combinations from the sections
		for (StringBuffer sb : getSubCombinations(0)) {
			result.add(sb.toString());
		}
		return result;
	}
	
	private  ArrayList<StringBuffer> getSubCombinations(int position){
		if(position==sections.size()-1) return sections.get(position).getCombinations();
		else{
			ArrayList<StringBuffer> result = new ArrayList<StringBuffer>();
			for (StringBuffer stringBuffer : sections.get(position).getCombinations()) {
				for (StringBuffer stringBuffer2 : getSubCombinations(position +1)) {
					result.add(new StringBuffer(new StringBuffer(stringBuffer).append(stringBuffer2)));
				}
			}
			return result;
		}		
	}

	private abstract class Section {
		public StringBuffer value;

		public Section(StringBuffer value) {
			this.value = value;
		}

		public abstract ArrayList<StringBuffer> getCombinations();
	}

	private class ValidSection extends Section {
		public ValidSection(StringBuffer value) {
			super(value);
		}

		@Override
		public ArrayList<StringBuffer> getCombinations() {
			ArrayList<StringBuffer> res = new ArrayList<StringBuffer>();
			res.add(new StringBuffer(this.value));
			return res;
		}
	}

	private class RepeatSection extends Section {

		public RepeatSection(StringBuffer value) {
			super(value);
		}

		@Override
		public ArrayList<StringBuffer> getCombinations() {
			ArrayList<StringBuffer> res = new ArrayList<StringBuffer>();
			res.add(new StringBuffer(this.value));
			StringBuffer two = new StringBuffer(this.value);
			two.append(value.toString().toCharArray()[0]);
			res.add(two);
			//System.out.println(two.toString());
			return res;
		}
	}
	
	public static void main(String[] args) {
		Rationaliser rc = new Rationaliser();
		for(String s: rc.getCombinations("Nooooooooooooooo")){
			System.out.println(s);
		}
	}

}
