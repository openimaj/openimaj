/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.citation.annotation.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.jbibtex.Value;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

/**
 * A Mocked version of a {@link Reference} 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class MockReference implements InvocationHandler {
	private String[] author;
	private String title;
	private ReferenceType type;
	private String year;
	private String journal = "";
	private String booktitle = "";
	private String[] pages = {};
	private String chapter = "";
    private String edition = "";
    private String url = "";
    private String note = "";
    private String[] editor = {};
    private String institution = "";
    private String month = "";
    private String number = "";
    private String organization = "";
    private String publisher = "";
    private String school = "";
    private String series = "";
    private String volume = "";
    private String [] customData = {};
    
    /**
     * Construct from a BibTeXEntry
     * @param entry the BibTeXEntry
     */
    public MockReference(BibTeXEntry entry) {
    	type = ReferenceType.getReferenceType(entry.getType().getValue());
    	
    	Map<Key, Value> fields = entry.getFields();
    	for (Entry<Key, Value> e : fields.entrySet()) {
    		String ks = e.getKey().getValue();
    		Value v = e.getValue();
    		
    		if (ks.equalsIgnoreCase("author"))
    			author = ((StringValue) v).getString().split(" and ");
    		else if (ks.equalsIgnoreCase("title"))
    			title = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("year"))
    			year = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("journal"))
    			journal = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("booktitle"))
    			booktitle = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("pages"))
    			pages = ((StringValue) v).getString().split(",|-|--");
    		else if (ks.equalsIgnoreCase("chapter"))
    			chapter = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("edition"))
    			edition = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("url"))
    			url = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("note"))
    			note = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("editor"))
    			editor = ((StringValue) v).getString().split(" and ");
    		else if (ks.equalsIgnoreCase("institution"))
    			institution = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("month"))
    			month = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("number"))
    			number = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("organization"))
    			organization = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("publisher"))
    			publisher = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("school"))
    			school = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("series"))
    			series = ((StringValue) v).getString();
    		else if (ks.equalsIgnoreCase("volume"))
    			volume = ((StringValue) v).getString();
    		else {
    			List<String> data = new ArrayList<String>();
    			data.addAll(Arrays.asList(customData));
    			
    			if (v instanceof StringValue) {
    				data.add(ks);
    				data.add(((StringValue)v).getString());
    			}
    			
    			customData = data.toArray(customData);
    		}
    	}	
    }
    
    /**
     * The name(s) of the author(s)
     * 
     * @return The name(s) of the author(s)
     */
    public String[] author() {
    	return author;
    }

    /**
     * The title of the work
     * 
     * @return The title of the work
     */
    public String title() {
    	return title;
    }

    /**
     * The type of publication
     * 
     * @return The type of publication
     * @see ReferenceType
     */
    public ReferenceType type() {
    	return type;
    }

    /**
     * The year of publication (or, if unpublished, the year of creation)
     * 
     * @return The year of publication (or, if unpublished, the year of creation)
     */
    public String year() {
    	return year;
    }

    /**
     * The journal or magazine the work was published in
     * 
     * @return The journal or magazine the work was published in
     */
    public String journal() {
    	return journal;
    }
    
    /**
     * The title of the book, if only part of it is being cited
     * 
     * @return The title of the book, if only part of it is being cited
     */
    public String booktitle() {
    	return booktitle;
    }

    /**
     * Page numbers 
     * 
     * @return Page numbers
     */
    public String[] pages() {
    	return pages;
    }

    /**
     * The chapter number
     * 
     * @return The chapter number
     */
    public String chapter() {
    	return chapter;
    }
    
    /**
     * The edition of a book, long form (such as "first" or "second")
     * 
     * @return  The edition of a book, long form (such as "first" or "second")
     */
    public String edition() {
    	return edition;
    }
    
    /**
     * An optional URL reference where the publication can be found.
     * 
     * @return A URL where the reference can be found. 
     */
    public String url() {
    	return url;
    }

    /**
     * Miscellaneous extra information
     * 
     * @return Miscellaneous extra information
     */
    public String note() {
    	return note;
    }
    
    /**
     * The name(s) of the editor(s)
     * 
     * @return The name(s) of the editor(s)
     */
    public String[] editor() {
    	return editor;
    }
    
    /**
     * The institution that was involved in the publishing, but not necessarily the publisher
     * 
     * @return The institution that was involved in the publishing, but not necessarily the publisher
     */
    public String institution() {
    	return institution;
    }
    
    /**
     * The month of publication (or, if unpublished, the month of creation)
     * 
     * @return The month of publication (or, if unpublished, the month of creation)
     */
    public String month() {
    	return month;
    }
    
    /**
     * The "(issue) number" of a journal, magazine, or tech-report, if applicable. (Most publications have a "volume", but no "number" field.)
     * 
     * @return The "(issue) number" of a journal, magazine, or tech-report, if applicable. (Most publications have a "volume", but no "number" field.)
     */
    public String number() {
    	return number;
    }
    
    /**
     * The conference sponsor
     * 
     * @return The conference sponsor
     */
    public String organization() {
    	return organization;
    }
    
    /**
     * The publisher's name
     * 
     * @return The publisher's name
     */
    public String publisher() {
    	return publisher;
    }
    
    /**
     * The school where the thesis was written
     * 
     * @return The school where the thesis was written
     */
    public String school() {
    	return school;
    }
    
    /**
     * The series of books the book was published in (e.g. "The Hardy Boys" or "Lecture Notes in Computer Science")
     * 
     * @return The series of books the book was published in (e.g. "The Hardy Boys" or "Lecture Notes in Computer Science")
     */
    public String series() {
    	return series;
    }
    
    /**
     * The volume of a journal or multi-volume book
     * 
     * @return The volume of a journal or multi-volume book
     */
    public String volume() {
    	return volume;
    }
    
    /**
     * A list of custom key value data pairs.
     * 
     * @return A list of custom key value data pairs.
     */
    public String [] customData() {
    	return customData;
    }

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Method newmethod = getClass().getMethod(method.getName(), method.getParameterTypes());
		
		return newmethod.invoke(this, args);
	}
	
	/**
	 * @return the {@link MockReference} as a {@link Reference} instance
	 */
	public Reference asReference() {
		return (Reference) Proxy.newProxyInstance(MockReference.class.getClassLoader(), new Class<?>[]{Reference.class}, this);
	}
	
	/**
	 * Make a {@link Reference} from a {@link BibTeXEntry}.
	 * @param entry the {@link BibTeXEntry}.
	 * @return the {@link Reference}
	 */
	public static Reference makeReference(BibTeXEntry entry) {
		return new MockReference(entry).asReference();
	}
}
