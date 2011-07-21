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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.VisitorSupport;

/*
 * loops through form controls and saves the name/value pairs in a Map
 */
public class FormControlVisitorSupport extends VisitorSupport {

	private Map<String, String> map = new LinkedHashMap<String, String>();

	public Map<String, String> getFormParameters() {
		return map;
	}

	public void visit(Element node) { // Visits the given Element
		if ("input".equalsIgnoreCase(node.getName())) {
			String value = node.valueOf("@value");
			if ("checkbox".equalsIgnoreCase(node.valueOf("@type")))
				if( "".equals( node.valueOf("@checked") ) )
					value = "";
			addParameter(node.valueOf("@name"), value);
		}
		else if ("textarea".equalsIgnoreCase(node.getName()))
			addParameter(node.valueOf("@name"), node.getText());
		else if ("select".equalsIgnoreCase(node.getName())) {
			for (Node n : (List<Node>) node.selectNodes("option")) {
				for (Node attr : (List<Node>) ((Element) n).attributes()) {
					if ("selected".equalsIgnoreCase(attr.getName())) {
						addParameter(node.valueOf("@name"), n.valueOf("@value"));
						return;
					}
				}
			}
		}
	}
	
	protected void addParameter(String key, String value) {
		if("".equals(key))
			return;
		value = value.replaceAll("^\r?\n", "");
		map.put(key, value);
	}
}