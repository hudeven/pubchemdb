package edu.scripps.fl.pubchem.web.entrez.transformer;

import org.apache.commons.collections.Transformer;
import org.dom4j.Node;

//public class TaxonomyTransformer implements Transformer<Node, String> {
//	public String transform(Node docSumNode) {
//		return docSumNode.valueOf("ScientificName/text()");
//	}
//}

public class TaxonomyTransformer implements Transformer {
	public Object transform(Object docSumNode) {
		return ((Node)docSumNode).valueOf("ScientificName/text()");
	}
}