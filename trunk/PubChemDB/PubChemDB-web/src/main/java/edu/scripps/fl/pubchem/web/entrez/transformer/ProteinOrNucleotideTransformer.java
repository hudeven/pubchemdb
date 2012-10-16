package edu.scripps.fl.pubchem.web.entrez.transformer;

import org.apache.commons.collections.Transformer;
import org.dom4j.Node;

//public class ProteinOrNucleotideTransformer implements Transformer<Node, String> {
//	public String transform(Node docSumNode) {
//		String xtra = docSumNode.valueOf("Extra/text()");
//		String title = docSumNode.valueOf("Title/text()");
//		return String.format("%s%s", xtra, title);
//	}
//}

public class ProteinOrNucleotideTransformer implements Transformer {
	public Object transform(Object docSumNode) {
		String xtra = ((Node)docSumNode).valueOf("Extra/text()");
		String title = ((Node)docSumNode).valueOf("Title/text()");
		return String.format("%s%s", xtra, title);
	}
}