/*
 * Copyright 2010 The Scripps Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.scripps.fl.dom4j.util;

import org.dom4j.CDATA;
import org.dom4j.Element;
import org.dom4j.Text;
import org.dom4j.VisitorSupport;

public class AppendingVisitorSupport extends VisitorSupport {
	
	private boolean DEBUGGING = false;
	private StringBuffer sb = new StringBuffer();
	
	public String getText() {
		return sb.toString();
	}
	
	public void visit(CDATA node) { // Visits the given CDATA
		sb.append(node.getText());
	}

	public void visit(Element node) { // Visits the given Element
		if( "br".equalsIgnoreCase(node.getName())) {
			if(DEBUGGING)
				System.out.println(String.format("%s\t%s\t<<%s>>", "Element", node.getName(), node.getText()));
			sb.append(String.format("%n"));
		}
	}

	public void visit(Text node) { // Visits the given Text
		String text = node.getText();
		if( text != null && ! text.matches("^\\s*$") ) {
			if(DEBUGGING)
				System.out.println(String.format("%s\t%s\t<<%s>>", "Text", node.getName(), node.getText()));
			text = text.replaceAll("^\\r?\\n", "");
			sb.append(text);
		}
	}
}