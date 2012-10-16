package edu.scripps.fl.pubchem.web.entrez.transformer;

import org.apache.commons.collections.Transformer;
import org.dom4j.Node;

//public class GeneTransformer implements Transformer<Node, String> {
//	public String transform(Node docSumNode) {
//		String desc = docSumNode.valueOf("Description/text()");
//		String org = docSumNode.valueOf("Orgname/text()");
//		return String.format("%s [%s]", desc, org);
//	}
//}

public class GeneTransformer implements Transformer {
	public Object transform(Object docSumNode) {
		String desc = ((Node)docSumNode).valueOf("Description/text()");
		String org = ((Node)docSumNode).valueOf("Orgname/text()");
		return String.format("%s [%s]", desc, org);
	}
}