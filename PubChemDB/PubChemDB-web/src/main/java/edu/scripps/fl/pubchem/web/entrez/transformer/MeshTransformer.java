package edu.scripps.fl.pubchem.web.entrez.transformer;

import org.apache.commons.collections.Transformer;
import org.dom4j.Node;

//public class MeshTransformer implements Transformer<Node, String> {
//	public String transform(Node docSumNode) {
////		String note = docSumNode.valueOf("DS_ScopeNote/text()");
//		String term = docSumNode.valueOf("DS_MeshTerms/string[1]/text()");
////		return String.format("%s - %s", term, note);
//		return term;
//	}
//}

public class MeshTransformer implements Transformer {
	public Object transform(Object docSumNode) {
		String term = ((Node)docSumNode).valueOf("DS_MeshTerms/string[1]/text()");
		return term;
	}
}