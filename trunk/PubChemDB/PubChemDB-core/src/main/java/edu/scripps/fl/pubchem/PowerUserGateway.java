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
package edu.scripps.fl.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark Southern
 *  
 * 	// Code usage sample
 * 	PowerUserGateway pug = PowerUserGateway.newInstance();
 *	Document doc = PUGRequest.newMultiAssayResultRequest(
 *			Arrays.asList(new Integer[]{2546,2551}),
 *			Arrays.asList(new Integer[]{24892677,24892644}),
 *			Type.CID, Output.XML);
 *	pug.setRequest( doc );
 *	pug.submitAndWait(5000);
 *	URL url = pug.getResponseURL();
 *  
 * The following references where helpful:
 * 
 * http://pubchem.ncbi.nlm.nih.gov/pug/pughelp.html
 * http://fiehnlab.ucdavis.edu/staff/scholz/dev/power_user_gateway_in_java/power_user_gateway_NCBI_java
 *  
 */
public class PowerUserGateway {

	public enum Output {
		CSV, ASN1, XML;
	}
	
	public enum Type {
		AID("pcassay", "aid"), SID("pcsubstance", "sid"), CID("pccompound", "cid"), TID(null, "tid");
		private String database;
		private String type;
		private Type(String database, String type) {
			this.database = database;
			this.type = type;
		}
		public String getDatabase() {
			return database;
		}
		public String getType() {
			return type;
		}
		
		public static Type getType(String query) {
			if( "CID".equalsIgnoreCase(query))
				return Type.CID;
			else if( "SID".equalsIgnoreCase(query))
				return Type.SID;
			if( "TID".equalsIgnoreCase(query))
				return Type.TID;
			if( "AID".equalsIgnoreCase(query))
				return Type.AID;
			else throw new RuntimeException("Cannot determine type from query: " + query);
		}
	}

	private static final Logger log = LoggerFactory.getLogger(PowerUserGateway.class);
	private static final String SITE = "pubchem.ncbi.nlm.nih.gov";
	private static final int PORT = 80;

	private static final String PATH = "/pug/pug.cgi";

	public static Document getDocument(InputStream inputStream) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		DOMReader reader = new DOMReader();
		Document doc = reader.read(builder.parse(inputStream));
		log.debug(String.format("Received XML:%n%n%s%n%n", doc.asXML()));
		return doc;
	}

	public static PowerUserGateway newInstance() {
		return new PowerUserGateway();
	}

	private Document requestDocument;
	private Document responseDocument;
	private HttpClient client;

	private PowerUserGateway() {
		client = new DefaultHttpClient();
	}

	protected Document createStatusDocument(String requestId) throws Exception {
		InputStream is = getClass().getResourceAsStream("/edu/scripps/fl/pubchem/pug/PugStatus.xml");
		Document doc = getDocument(is);
		Node node = doc.selectSingleNode("//PCT-Request_reqid");
		node.setText("" + requestId);
		return doc;
	}

	protected InputStream fetchResponse(Document document) throws Exception {
//		PostMethod postMethod = new PostMethod(PATH);
//		postMethod.setRequestEntity(new StringRequestEntity(document.asXML(), "text/xml", "utf-8"));
//		client.executeMethod(postMethod);
//		return postMethod.getResponseBodyAsStream();
		
		HttpPost post = new HttpPost("http://" + SITE + PATH);
		post.setEntity(new StringEntity(document.asXML(), "text/xml", "utf-8"));
		HttpResponse response = client.execute(post);
		return response.getEntity().getContent();
	}

	public URL getResponseURL() throws IOException {
		URL url = null;
		Node node = getResponse().selectSingleNode("//*/PCT-Download-URL_url");
		if (node != null)
			url = new URL(node.getText());
		return url;
	}

	private String makeRequest() throws Exception {
		InputStream is = fetchResponse(getRequest());
		Document doc = getDocument(is); 
		setResponse( doc );
		Node node = doc.selectSingleNode("//PCT-Status[@value='server-error']");
		if( node != null ) {
			String msg = node.getParent().getParent().getText();
			throw new Exception(String.format("PUG Error: %s", msg));
		}
		node = doc.selectSingleNode("//*/PCT-Waiting_reqid");
		String requestId = null;
		if (node != null) {
			requestId = node.getText();
			log.debug("PCT-Waiting_reqid: " + requestId);
		}
		return requestId;
	}

	public String requestUpdate(String requestId) throws Exception {
		Document doc = createStatusDocument(requestId); 
		setRequest(doc);
		return makeRequest();
	}

	public void setRequest(Document document) {
		requestDocument = document;
	}
	
	protected void setResponse(Document document) {
		responseDocument = document;
	}
	
	public Document getRequest() {
		return requestDocument;
	}
	
	public Document getResponse() {
		return responseDocument;
	}

	public void submitAndWait(long sleepMillis) throws Exception {
		setResponse(null); // clear any existing use
		String requestId = makeRequest();
		while (null != requestId) {
			Thread.sleep(sleepMillis);
			requestId = requestUpdate(requestId);
		}
	}
}