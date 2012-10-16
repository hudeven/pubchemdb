package edu.scripps.fl.pubchem.web.entrez.transformer;

import org.apache.commons.collections.Transformer;
import org.dom4j.Node;

//public class BioSystemsTransformer implements Transformer<Node, String> {
//	public String transform(Node docSumNode) {
//		return docSumNode.valueOf("biosystem/biosystemname/text()");
//	}
//}

public class BioSystemsTransformer implements Transformer {
	public Object transform(Object docSumNode) {
		return ((Node)docSumNode).valueOf("biosystem/biosystemname/text()");
	}
}