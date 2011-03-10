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
package edu.scripps.fl.pubchem.app.summary;

import java.util.Iterator;

import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.stage.ExtendedBaseStage;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

public class NodesStage extends ExtendedBaseStage {

	@Override
	public void innerProcess(Object obj) throws StageException {
		Document document = (Document) obj;
		try {
			Element setElem = (Element) document.selectSingleNode("/eSummaryResult/DocumentSummarySet");
			Iterator<Node> iter = setElem.nodeIterator();
			for (; iter.hasNext();) {
				Node node = iter.next();
				if ("DocumentSummary".equals(node.getName())) // eUtils version 2.0 
					emit(node);
			}
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}

	public String status() {
		return "";
	}
}