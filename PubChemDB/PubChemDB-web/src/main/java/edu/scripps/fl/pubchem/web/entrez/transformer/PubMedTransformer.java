package edu.scripps.fl.pubchem.web.entrez.transformer;

import java.util.List;

import org.apache.commons.collections.Transformer;
import org.dom4j.Node;

public class PubMedTransformer implements Transformer { //<Node, String> {
	
	private int maxLength;
	
	public PubMedTransformer() {
		this(Integer.MAX_VALUE);
	}
	
	public PubMedTransformer(int maxLength) {
		this.maxLength = maxLength;
	}
	
//	public String transform(Node docSumNode) {
	public Object transform(Object obj) {
		Node docSumNode = (Node) obj;
		List<Node> authorNodes = docSumNode.selectNodes("Authors/Author/Name/text()");
		StringBuffer authors = new StringBuffer();
		for (Node node : authorNodes ) {
			if (authors.length() > 0)
				authors.append(", ");
			authors.append(node.getText());

		}
		if( authors.length() > 0 )
			authors.append(". ");
		String title = docSumNode.valueOf("Title/text()");
		String source = docSumNode.valueOf("Source/text()");
		String pubDate = docSumNode.valueOf("PubDate/text()");
		String volume = docSumNode.valueOf("Volume/text()");
		String issue = docSumNode.valueOf("Issue/text()");
		String pages = docSumNode.valueOf("Pages/text()");
		String label = formatLabel(authors, title, source, pubDate, volume, issue, pages);
		if( label.length() > maxLength & authorNodes.size() > 0 )
			label = formatLabel(authorNodes.get(0).getText() + " et al. ", title, source, pubDate, volume, issue, pages);
		if( label.length() > maxLength )
			label = label.substring(1, maxLength - 3) + "...";
		return label;
	}
	
	protected String formatLabel(Object... items) {
		String label = String.format("%s%s %s %s; %s(%s) %s", items);
		return label;
	}
}