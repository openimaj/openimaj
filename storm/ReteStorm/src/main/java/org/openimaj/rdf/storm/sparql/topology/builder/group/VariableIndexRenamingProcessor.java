package org.openimaj.rdf.storm.sparql.topology.builder.group;

import com.hp.hpl.jena.sparql.syntax.Element;

public class VariableIndexRenamingProcessor {

	private Element element;
	private String[] varNames;

	public VariableIndexRenamingProcessor(Element element, String[] varNames) {
		this.element = element;
		this.varNames = varNames;
	}

	public String constructQueryString(String originalQuery){
		int index = 0;
		for (String oldName : varNames) {
			originalQuery = originalQuery.replaceAll("[?]" + oldName, "?" + index++);
		}
		return originalQuery;
	}

//	public Element variableIndexRename(){
//		return variableIndexRename(this.element);
//	}
//
//	private Element variableIndexRename(Element elm) {
//		if(elm instanceof ElementPathBlock){
//			return variableIndexRename((ElementPathBlock)elm);
//		}
//		else if(elm instanceof ElementGroup){
//			return variableIndexRename((ElementGroup)elm);
//
//		}
//		else if(elm instanceof ElementFilter){
//			return variableIndexRename((ElementFilter)elm);
//		}
//		return null;
//	}
//
//	private Element variableIndexRename(ElementPathBlock elm) {
//		ElementPathBlock renamedBlock = new ElementPathBlock();
//		for (TriplePath tpath : elm.getPattern()) {
//			Triple tp = tpath.asTriple();
//			Node subject=null,predicate=null,object=null;
//			if (tp.getSubject().isVariable()) {
//				int indexOf = Arrays.asList(varNames).indexOf(tp.getSubject().getName());
//				subject = Node.createVariable("" + indexOf);
//			}
//			else{
//				subject = tp.getSubject();
//			}
//			if (tp.getPredicate().isVariable()) {
//				int indexOf = Arrays.asList(varNames).indexOf(tp.getPredicate().getName());
//				predicate = Node.createVariable("" + indexOf);
//			}
//			else{
//				predicate = tp.getPredicate();
//			}
//			if (tp.getObject().isVariable()) {
//				int indexOf = Arrays.asList(varNames).indexOf(tp.getObject().getName());
//				object = Node.createVariable("" + indexOf);
//			} else if (tp.getObject().isLiteral() && tp.getObject().getLiteralValue() instanceof Functor) {
//				Node[] oldFunctorArgs = ((Functor) tp.getObject().getLiteralValue()).getArgs();
//				Node[] newFunctorArgs = new Node[oldFunctorArgs.length];
//				int i = 0;
//				for (Node n : oldFunctorArgs){
//					Node toAdd = null;
//					if (n.isVariable()) {
//						int indexOf = Arrays.asList(varNames).indexOf(n.getName());
//						toAdd = Node.createVariable("" + indexOf);
//					}
//					else{
//						toAdd = n;
//					}
//					newFunctorArgs[i++] = toAdd;
//				}
//				String functorName = ((Functor)tp.getObject().getLiteralValue()).getName();
//				object = Functor.makeFunctorNode(functorName, newFunctorArgs);
//			}
//			else{
//				object = tp.getObject();
//			}
//			renamedBlock.addTriple(new TriplePath(new Triple(subject, predicate, object)));
//		}
//		return renamedBlock;
//	}
//
//	private Element variableIndexRename(ElementGroup elm) {
//		ElementGroup newGroup = new ElementGroup();
//		for (Element gelm : elm.getElements()) {
//			newGroup.addElement(variableIndexRename(gelm));
//		}
//		return newGroup;
//	}
//
//	private Element variableIndexRename(ElementFilter elm) {
//		return elm;
//	}

}
