package edu.scripps.fl.pubchem.web.entrez.transformer;

import org.apache.commons.collections.Transformer;
import org.dom4j.Node;

//public class PCAssayTransformer implements Transformer<Node, String> {
//	public String transform(Node docSumNode) {
//		return docSumNode.valueOf("AssayName/text()");
//	}
//}

public class PCAssayTransformer implements Transformer {
	public Object transform(Object docSumNode) {
		return ((Node)docSumNode).valueOf("AssayName/text()");
	}
}