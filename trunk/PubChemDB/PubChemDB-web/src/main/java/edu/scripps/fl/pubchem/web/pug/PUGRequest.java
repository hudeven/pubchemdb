/*
 * Copyright 2011 The Scripps Research Institute
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
package edu.scripps.fl.pubchem.web.pug;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import edu.scripps.fl.pubchem.web.pug.PowerUserGateway.Output;
import edu.scripps.fl.pubchem.web.pug.PowerUserGateway.Type;

/**
 * @author Mark Southern
 *
 * Some wrappers for PUG XML Requests
 */
public class PUGRequest {

	public static Document newMultiAssayResultRequest(Collection aids, Collection scids, Type type, Output output) throws Exception {
		URL url = PUGRequest.class.getResource("/edu/scripps/fl/pubchem/pug/PugMultipleAIDandCIDRequest.xml");
		Document doc = PowerUserGateway.getDocument(url.openStream());
		setResponseIds(doc, Type.AID, aids);
		setResponseIds(doc, type, scids);
		setOutputType(doc, output);
		return doc;
	}
	
	public static Document newDescriptionXmlRequest(int aid) throws Exception {
		URL url = PUGRequest.class.getResource("/edu/scripps/fl/pubchem/pug/PugDescrXmlRequest.xml");
		Document doc = PowerUserGateway.getDocument(url.openStream());
		doc.selectSingleNode("//PCT-QueryAssayDescription_aid").setText("" + aid);
		return doc;
	}
	
	public static Document newBioActivitySummaryRequest(Collection scids, Type type) throws Exception {
		URL url = PUGRequest.class.getResource("/edu/scripps/fl/pubchem/pug/PugBioActivityTableRequest.xml");
		Document doc = PowerUserGateway.getDocument(url.openStream());
		setResponseIds2(doc, type, scids);
		return doc;
	}
	
	private static void setResponseIds2(Document document, Type type, Collection<Object> ids) throws Exception {
		Node node = document.selectSingleNode("//PCT-ID-List_db"); 
		node.setText(type.getDatabase());
		node = document.selectSingleNode(".//PCT-ID-List_uids");
		for (Node child : (List<Node>) node.selectNodes("*"))
			child.detach();
		for (Object id : ids) {
			Element aidElem = DocumentHelper.createElement("PCT-ID-List_uids_E");
			aidElem.setText(id.toString());
			((Element) node).add(aidElem);
		}
	}
	
	public static void setResponseIds(Document document, Type type, Collection<Object> ids) throws Exception {
		String idTypePath = "//PCT-QueryAssayData_";
		idTypePath += Type.AID.equals(type) ? "aids" : "scids";
		Node localRoot = document.selectSingleNode(idTypePath);
		Node node = localRoot.selectSingleNode(".//PCT-ID-List_db"); 
		node.setText(type.getDatabase());
		node = localRoot.selectSingleNode(".//PCT-ID-List_uids");
//		if (node == null)
//			throw new Exception("Cannot find PCT-ID-List_uids node");
		for (Node child : (List<Node>) node.selectNodes("*"))
			child.detach();
		for (Object id : ids) {
			Element aidElem = DocumentHelper.createElement("PCT-ID-List_uids_E");
			aidElem.setText(id.toString());
			((Element) node).add(aidElem);
		}
	}
	
	public static void setOutputType(Document document, Output output) throws Exception {
		Node node = document.selectSingleNode("//PCT-QueryAssayData_output");
		if (node == null)
			throw new Exception("Document does not contain a PCT-QueryAssayData_output node");
		if (Output.XML.equals(output)) {
			node.setText("1");
			((Element) node).attribute("value").setText("assay-xml");
		} else if (Output.ASN1.equals(output)) {
			node.setText("2");
			((Element) node).attribute("value").setText("assay-text-asn");
		} else if (Output.CSV.equals(output)) {
			node.setText("4");
			((Element) node).attribute("value").setText("csv");
		}
	}
	
}
