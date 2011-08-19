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
package edu.scripps.fl.pubchem.web.entrez;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.scripps.fl.pubchem.web.session.HttpClientBase;

public class EUtilsWebSession extends HttpClientBase {

	private static final Logger log = LoggerFactory.getLogger(EUtilsWebSession.class);
	private static final boolean DEBUGGING = true;
	private static EUtilsWebSession instance;
	public static String TOOL = "PubChemDB";
	public static String EMAIL = "southern@scripps.edu";

	public static EUtilsWebSession getInstance() {
		if (instance == null) {
			synchronized (EUtilsWebSession.class) { // 1
				if (instance == null) {
					synchronized (EUtilsWebSession.class) { // 3
						// inst = new Singleton(); //4
						instance = new EUtilsWebSession();
					}
					// instance = inst; //5
				}
			}
		}
		return instance;
	}

	public static String getTool() {
		return System.getProperty("Entrez.Tool");
	}

	public static String getEmail() {
		return System.getProperty("Entrez.Email");
	}

	class InputStreamCallable implements Callable<InputStream> {
		private String url;
		List<Object> params;

		public InputStreamCallable(String url, List<Object> params) {
			this.url = url;
			this.params = params;
		}

		public InputStream call() throws IOException {
			HttpPost post = new HttpPost(url);
			if (params.size() % 2 != 0)
				params.add("");
			params.add("tool");
			params.add(EUtilsWebSession.this.TOOL);
			params.add("email");
			params.add(EUtilsWebSession.this.EMAIL);
			MultipartEntity entity = addParts(new MultipartEntity(), params);
//			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(getNameValuePairs(params));
			post.setEntity(entity);
			log.debug("Fetching from: " + generateUrl(url, params));
			HttpResponse response = getHttpClient().execute(post);
			if (DEBUGGING) {
				File file = File.createTempFile("eutils", ".xml");
				log.info("DEBUG MODE: Copying eUtils stream to: " + file);
				response.getEntity().writeTo(new BufferedOutputStream(new FileOutputStream(file)));
				return new BufferedInputStream(new FileInputStream(file));
			}
			else
				return response.getEntity().getContent();
		}
	}

	private String generateUrl(String url, Collection<Object> params) {
		StringBuffer sb = new StringBuffer();
		sb.append(url).append("?");
		Iterator<Object> iter = params.iterator();
		int counter = 0;
		while (iter.hasNext()) {
			String name = iter.next().toString();
			String value = iter.hasNext() ? iter.next().toString() : "";
			sb.append(name).append("=").append(value);
			if (iter.hasNext())
				sb.append("&");
		}
		return sb.toString();
	}

	public Callable<InputStream> getInputStream(final String url, final Object... params) throws IOException {
		List list = new ArrayList(Arrays.asList(params));
		return new InputStreamCallable(url, list);
	}

	public Callable<InputStream> getInputStream(final String url, final List<Object> params) throws IOException {
		return new InputStreamCallable(url, params);
	}
	
	private List<NameValuePair> getNameValuePairs(List<Object> params) {
		List<NameValuePair> data = new ArrayList();
		for (int ii = 0; ii < params.size(); ii += 2) {
			String name = params.get(ii).toString();
			String value = "";
			if ((ii + 1) < params.size())
				value = params.get(ii + 1).toString();
			data.add(new BasicNameValuePair(name, value));
		}
		return data;
	}

	private MultipartEntity addParts(MultipartEntity entity, Collection<Object> pairs) throws UnsupportedEncodingException {
		Iterator<Object> iter = pairs.iterator();
		while (iter.hasNext()) {
			String name = iter.next().toString();
			String key = iter.hasNext() ? iter.next().toString() : "";
			entity.addPart(name, new StringBody(key.toString()));
		}
		return entity;
	}

	public Callable<Document> getDocumentCallable(final String url, final Object... params) throws Exception {
		return new Callable<Document>() {
			public Document call() throws Exception {
				return getDocument(getInputStream(url, params).call());
			}
		};
	}

	public Document getDocument(final String url, final Object... params) throws Exception {
		List list = new ArrayList(Arrays.asList(params));
		return getDocument(getInputStream(url, list).call());
	}

	public Document getDocument(final String url, final Collection<Object> params) throws Exception {
		return getDocument(getInputStream(url, params).call());
	}

	public Document getDocument(InputStream in) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		DOMReader reader = new DOMReader();
		Document document = reader.read(builder.parse(new BufferedInputStream(in)));
		// SAXReader reader = new SAXReader();
		// Document document = reader.read(in);
		in.close();
		if (DEBUGGING)
			new XMLWriter().write(document);
		Node node = document.selectSingleNode("/eSearchResult/ERROR");
		if (node != null)
			throw new Exception(node.getText());
		return document;
	}

	public Set<String> getDatabases() throws Exception {
		Document document = getDocument("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/einfo.fcgi");
		List<Node> list = document.selectNodes("/eInfoResult/DbList/DbName", ".");
		Set dbs = new HashSet(list.size());
		for (Node node : list)
			dbs.add(node.getText());
		return dbs;
	}

	public Collection<Long> getIds(String query, String db, Collection<Long> ids) throws Exception {
		return getIds(query, db, ids, 1000000);
	}

	public List<Long> getIds(String query, String db) throws Exception {
		return (List<Long>) getIds(query, db, new ArrayList<Long>(), 1000000);
	}

	class ESearchHandler extends DefaultHandler {
		private StringBuffer buf;
		String webEnv;
		String queryKey;
		int retMax;
		int retStart;
		int count;
		private Collection<Long> ids;

		public ESearchHandler(Collection<Long> ids) {
			this.ids = ids;
		}

		public void characters(char ch[], int start, int length) throws SAXException {
			buf.append(ch, start, length);
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equalsIgnoreCase("Id")) {
				Long id = Long.parseLong(buf.toString());
				ids.add(id);
			} else if (qName.equalsIgnoreCase("WebEnv"))
				webEnv = buf.toString();
			else if (qName.equalsIgnoreCase("QueryKey"))
				queryKey = buf.toString();
			else if (qName.equalsIgnoreCase("RetMax"))
				retMax = Integer.parseInt(buf.toString());
			else if (qName.equalsIgnoreCase("RetStart"))
				retStart = Integer.parseInt(buf.toString());
			else if (qName.equalsIgnoreCase("Count")) {
				count = Integer.parseInt(buf.toString());
				if (ids instanceof ArrayList)
					((ArrayList<Long>) ids).ensureCapacity(ids.size() + count);
			}
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			buf = new StringBuffer();
		}
	}

	public Collection<Long> getIds(String query, String db, Collection<Long> ids, int chunkSize) throws Exception {
		int retStart = 0;
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		ESearchHandler handler = new ESearchHandler(ids);
		InputStream is = getInputStream("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi", "db", db, "term", query, "retstart", retStart, "retmax",
				chunkSize, "usehistory", "y", "version", "2.0").call();
		while (true) {
			saxParser.parse(is, handler);
			if ((handler.retStart + handler.retMax) < (handler.count - 1)) {
				retStart += handler.retMax;
				is = getInputStream("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi", "db", db, "term", query, "retstart", retStart, "retmax",
						"" + chunkSize, "usehistory", "y", "QueryKey", handler.queryKey, "WebEnv", handler.webEnv, "version", "2.0").call();
			} else
				break;
		}
		return ids;
	}

	public Document getSummary(Object id, String db) throws Exception {
		return getDocument("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi", "db", db, "id", id, "retmode", "xml", "version", "2.0");
	}

	public InputStream getSummaries(Collection<Long> ids, String db) throws Exception {
		String idStr = StringUtils.join(ids, ",");
		return getInputStream("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi", "db", db, "id", idStr, "retmode", "xml", "version", "2.0").call();
	}

	public InputStream getSummaries(EntrezHistoryKey key) throws Exception {
		return getInputStream("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi", "db", key.getDatabase(), "WebEnv", key.getWebEnv(), "query_key", key.getQueryKey(),
				"retmode", "xml", "version", "2.0").call();
	}

	public Document getSummariesAsDocument(Collection<Long> ids, String db) throws Exception {
		return getDocument(getSummaries(ids, db));
	}

	public EntrezHistoryKey postIds(String db, Long... ids) throws Exception {
		return postIds(db, Arrays.asList(ids));
	}

	public EntrezHistoryKey postIds(String db, Collection<Long> ids) throws Exception {
		String idStr = StringUtils.join(ids, ",");
		Document doc = getDocument("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/epost.fcgi?", "db", db, "id", idStr);
		String queryKey = doc.valueOf("/ePostResult/QueryKey/text()");
		String webEnv = doc.valueOf("/ePostResult/WebEnv/text()");
		EntrezHistoryKey key = new EntrezHistoryKey(db, webEnv, queryKey);
		return key;
	}
}