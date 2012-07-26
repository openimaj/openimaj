package org.openimaj.rdf.rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

import com.hp.hpl.jena.n3.N3EventPrinter;
import com.hp.hpl.jena.n3.N3Parser;
import com.hp.hpl.jena.n3.N3ParserEventHandler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class N3Rules implements N3ParserEventHandler {
	public N3Rules(InputStream stream) throws RecognitionException, TokenStreamException {
		this(stream,"");
	}
	public N3Rules(InputStream stream, String base) throws RecognitionException, TokenStreamException {
		Model m = ModelFactory.createOntologyModel();
//		N3Parser parser = new N3Parser(stream, this);
		N3Parser parser = new N3Parser(stream, new N3EventPrinter(System.out));
		parser.parse();
//		m.read(stream, base,"N3");
	}
	
	public static void main(String[] args) throws FileNotFoundException, RecognitionException, TokenStreamException {
		new N3Rules(new FileInputStream(new File("/Users/ss/Development/python/pychinko/pychinko/rules/rdfs-rules.n3")));
	}
	@Override
	public void startDocument() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void endDocument() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void error(Exception ex, String message) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void startFormula(int line, String context) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void endFormula(int line, String context) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void quad(int line, AST subj, AST prop, AST obj, String context) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void directive(int line, AST directive, AST[] args, String context) {
		// TODO Auto-generated method stub
		
	}
}
