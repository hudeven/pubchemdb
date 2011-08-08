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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.VisitorSupport;
import org.dom4j.io.DOMReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import edu.scripps.fl.pubchem.web.ELinkResult;
import edu.scripps.fl.pubchem.web.session.HttpClientBase;

public class EUtilsFactory extends HttpClientBase {
	
	private static final Logger log = LoggerFactory.getLogger(EUtilsFactory.class);
	private static final boolean DEBUGGING = false;
	private static EUtilsFactory instance;
	public static String TOOL = "PubChemDB";
	public static String EMAIL = "southern@scripps.edu";

	public static EUtilsFactory getInstance() {
		if (instance == null) {
			synchronized (EUtilsFactory.class) { // 1
				if (instance == null) {
					synchronized (EUtilsFactory.class) { // 3
						// inst = new Singleton(); //4
						instance = new EUtilsFactory();
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

	public Callable<InputStream> getInputStream(final String url, final Object... params) throws IOException {
		return new Callable<InputStream>() {
			public InputStream call() throws Exception {
				StringBuffer sb = new StringBuffer();
				sb.append(url).append("?");
				HttpPost post = new HttpPost(url);
				MultipartEntity entity = addParts(new MultipartEntity(), "tool", EUtilsFactory.this.TOOL, "email",
						EUtilsFactory.this.EMAIL);
				entity = addParts(entity, params);
				post.setEntity(entity);
				HttpResponse response = getHttpClient().execute(post);
				// log.debug("Fetching from: " + url + StringUtils.join(params,
				// " "));
				InputStream in = response.getEntity().getContent();
				return in;
			}
		};
	}
	
	public Callable<InputStream> getInputStream(final String url, final Collection<Object> params) throws IOException {
		return new Callable<InputStream>() {
			public InputStream call() throws Exception {
				StringBuffer sb = new StringBuffer();
				sb.append(url).append("?");
				HttpPost post = new HttpPost(url);
				MultipartEntity entity = addParts(new MultipartEntity(), "tool", EUtilsFactory.this.TOOL, "email",
						EUtilsFactory.this.EMAIL);
				entity = addParts(entity, params);
				post.setEntity(entity);
				HttpResponse response = getHttpClient().execute(post);
				// log.debug("Fetching from: " + url + StringUtils.join(params,
				// " "));
				InputStream in = response.getEntity().getContent();
				return in;
			}
		};
	}
	
	private MultipartEntity addParts(MultipartEntity entity, Object... pairs) throws UnsupportedEncodingException {
		for (int ii = 0; ii < pairs.length; ii += 2)
			entity.addPart(pairs[ii].toString(), new StringBody(pairs[ii + 1].toString()));
		return entity;
	}
	
	private MultipartEntity addParts(MultipartEntity entity, Collection<Object> pairs) throws UnsupportedEncodingException {
		Iterator<Object> iter = pairs.iterator();
		while(iter.hasNext()) {
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
		return getDocument(getInputStream(url, params).call());
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

	@SuppressWarnings("unchecked")
	public Set<String> getDatabases() throws Exception {
		Document document = getDocument("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/einfo.fcgi");
		List<Node> list = (List<Node>) document.selectNodes("/eInfoResult/DbList/DbName", ".");
		Set<String> dbs = new HashSet<String>(list.size());
		for (Node node : list)
			dbs.add(node.getText());
		return dbs;
	}

	public Collection<Long> getIds(String query, String db, final Collection<Long> ids, int chunkSize) throws Exception {
		int retStart = 0;
		Document document = getDocument("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi", "db", db, "term",
				query, "retstart", retStart, "retmax", "" + chunkSize, "usehistory", "y", "version", "2.0");
		while (true) {
			String webEnv = document.selectSingleNode("/eSearchResult/WebEnv").getText();
			String queryKey = document.selectSingleNode("/eSearchResult/QueryKey").getText();
			int count = Integer.parseInt(document.selectSingleNode("/eSearchResult/Count").getText());
			int retMax = Integer.parseInt(document.selectSingleNode("/eSearchResult/RetMax").getText());
			retStart = Integer.parseInt(document.selectSingleNode("/eSearchResult/RetStart").getText());

			document.accept(new VisitorSupport() {
				public void visit(Element element) {
					if ("Id".equals(element.getName())) {
						Long id = Long.parseLong(element.getText());
						ids.add(id);
					}
				}
			});
			if ((retStart + retMax) < (count - 1)) {
				retStart += retMax;
				document = getDocument("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi", "db", db, "term",
						query, "retstart", retStart, "retmax", "" + chunkSize, "usehistory", "y", "QueryKey", queryKey,
						"WebEnv", webEnv, "version", "2.0");
			} else
				break;
		}
		return ids;
	}

	public List<Long> getIds(String query, String db) throws Exception {
		return getIds(query, db, 50000);
	}

	public List<Long> getIds(String query, String db, int chunkSize) throws Exception {
		return (List<Long>) getIds(query, db, new ArrayList<Long>(), chunkSize);
	}

	public Document getSummary(Object id, String db) throws Exception {
		return getDocument("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi", "db", db, "id", id, "retmode",
				"xml", "version", "2.0");
	}

	public InputStream getSummaries(Collection<Long> ids, String db) throws Exception {
		String idStr = StringUtils.join(ids, ",");
		return getInputStream("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi", "db", db, "id", idStr,
				"retmode", "xml", "version", "2.0").call();
	}
}