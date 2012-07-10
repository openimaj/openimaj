/*
 *  JOrtho
 *
 *  Copyright (C) 2005-2009 by i-net software
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *  
 * Created on 20.05.2009
 */
package org.openimaj.text.jorthospellcheck;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.InflaterInputStream;

/**
 * A implementation of an Iterator which split a large text into lines. It is used to read words lists.
 * @author Volker Berlin
 */
public class WordIterator implements Iterator<String> {

    private BufferedReader input;
    private String word;
    
    /**
     * Load the directory from a compressed list of words with UTF8 encoding. The words must be delimmited with
     * newlines.
     * 
     * @param filename
     *            the name of the file
     * @throws IOException
     *             If an I/O error occurs.
     * @throws NullPointerException
     *             If filename is null.
     */
    public WordIterator( URL filename ) throws IOException {
        this( createInflaterStream(filename), "UTF8" );
    }
    
    /**
     * Load the directory from plain a list of words. The words must be delimmited with newlines.
     * 
     * @param stream
     *            a InputStream with words
     * @param charsetName
     *            the name of a codepage for example "UTF8" or "Cp1252"
     * @throws IOException
     *             If an I/O error occurs.
     * @throws NullPointerException
     *             If stream or charsetName is null.
     */
    public WordIterator( InputStream stream, String charsetName ) throws IOException {
       this( new InputStreamReader( stream, charsetName ) );
    }

    /**
     * Load the directory from plain a list of words. The words must be delimmited with newlines.
     * 
     * @param reader
     *            a Reader with words
     * @throws IOException
     *             If an I/O error occurs.
     * @throws NullPointerException
     *             If reader is null.
     */
    public WordIterator( Reader reader ) throws IOException {
        input = new BufferedReader( reader );
        word = input.readLine();
    }
    
    /**
     * Create a plain stream from a compressed file.
     * @param filename the file of a JOrtho dictionary
     * @return the stream
     * @throws IOException If an I/O error occurs.
     */
    private static InputStream createInflaterStream( URL filename ) throws IOException{
        URLConnection conn = filename.openConnection();
        conn.setReadTimeout( 5000 );
        InputStream input = conn.getInputStream();
        input = new InflaterInputStream( input );
        return new BufferedInputStream( input );
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return word != null;
    }

    /**
     * {@inheritDoc}
     */
    public String next() {
        if(!hasNext()){
            throw new NoSuchElementException();
        }
        String next = word;
        try {
            word = input.readLine();
            if(word == null){
                input.close();
            }
        } catch( IOException e ) {
            word = null;
            e.printStackTrace();
        }
        return next;
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
