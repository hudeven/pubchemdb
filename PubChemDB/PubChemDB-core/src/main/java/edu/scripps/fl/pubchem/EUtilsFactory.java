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
package edu.scripps.fl.pubchem;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.VisitorSupport;
import org.dom4j.io.DOMReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.db.Relation;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.EPostRequest;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.EPostResult;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.ESearchRequest;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.ESearchResult;

public class EUtilsFactory {
	private static final Logger log = LoggerFactory.getLogger(EUtilsFactory.class);
	private static final boolean DEBUGGING = false;
	private static EUtilsFactory instance;
	private static EUtilsServiceStub eUtils;

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

	private EUtilsFactory() {
		try {
			eUtils = new EUtilsServiceStub();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void finalize() throws Throwable {
		if (eUtils != null) {
			eUtils.cleanup();
			eUtils = null;
		}
	}

	public EUtilsServiceStub getService() {
		return eUtils;
	}

	private MultipartEntity addParts(MultipartEntity entity, Object... pairs) throws UnsupportedEncodingException {
		for (int ii = 0; ii < pairs.length; ii += 2)
			entity.addPart(pairs[ii].toString(), new StringBody(pairs[ii + 1].toString()));
		return entity;
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
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpResponse response = httpclient.execute(post);
				// log.debug("Fetching from: " + url + StringUtils.join(params,
				// " "));
				InputStream in = response.getEntity().getContent();
				return in;
			}
		};
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

	public Collection<Long> getIds(Long id, String fromDb, String toDb) throws Exception {
		Document document = EUtilsFactory.getInstance().getDocument(
				"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi", "dbfrom", fromDb, "db", toDb, "id", "" + id);
		List<Node> linkSetDbs = document.selectNodes("/eLinkResult/LinkSet/LinkSetDb");
		Set<Long> relatedIds = new HashSet();
		int counter = 0;
		for (Node linkSetDb : linkSetDbs) {
			String linkName = linkSetDb.selectSingleNode("LinkName").getText();
			List<Node> ids = linkSetDb.selectNodes("Link/Id");
			for (Node idNode : ids) {
				long relatedId = Long.parseLong(idNode.getText());
				if (id.equals(relatedId))
					continue;
				relatedIds.add(relatedId);
			}
		}
		return relatedIds;
	}

	public List<Relation> getRelations(Long id, String fromDb, String toDb) throws Exception {
		Document document = EUtilsFactory.getInstance().getDocument(
				"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi", "dbfrom", fromDb, "db", toDb, "id", "" + id);
		return getRelations(document);
	}

	public List<Relation> getRelations(Document document) {
		String fromDb = document.selectSingleNode("/eLinkResult/LinkSet/DbFrom").getText();
		String idStr = document.selectSingleNode("/eLinkResult/LinkSet/IdList/Id").getText();
		Long id = Long.parseLong(idStr);
		List<Node> linkSetDbs = document.selectNodes("/eLinkResult/LinkSet/LinkSetDb");
		ArrayList<Relation> list = new ArrayList();
		for (Node linkSetDb : linkSetDbs) {
			String toDb = linkSetDb.selectSingleNode("DbTo").getText();
			String linkName = linkSetDb.selectSingleNode("LinkName").getText();
			List<Node> ids = linkSetDb.selectNodes("Link/Id");
			list.ensureCapacity(list.size() + ids.size());
			for (Node idNode : ids) {
				long relatedId = Long.parseLong(idNode.getText());
				if (id == relatedId)
					continue;
				Relation relation = new Relation();
				relation.setRelationName(linkName);
				relation.setFromDb(fromDb);
				relation.setFromId(id);
				relation.setToDb(toDb);
				relation.setToId(relatedId);
				list.add(relation);
			}
		}
		return list;
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
		return (List<Long>) getIds(query, db, new ArrayList(), chunkSize);
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

	public EntrezHistoryKey ePost(String db, String ids) throws Exception {
		EPostRequest request = new EPostRequest();
		request.setDb(db);
		request.setId(ids);
		request.setEmail(EMAIL);
		request.setTool(TOOL);
		EPostResult result = getService().run_ePost(request);
		if (null != result.getERROR())
			throw new Exception(result.getERROR());
		return new EntrezHistoryKey(db, result.getWebEnv(), result.getQueryKey());
	}

	public EntrezHistoryKey eSearch(String db, String searchTerm) throws Exception {
		ESearchRequest eSearch = new ESearchRequest();
		eSearch.setDb(db);
		eSearch.setTerm(searchTerm);
		eSearch.setUsehistory("y");
		eSearch.setRetMax("0");
		eSearch.setEmail(EMAIL);
		eSearch.setTool(TOOL);
		ESearchResult result = getService().run_eSearch(eSearch);
		Integer count = Integer.parseInt(result.getCount());
		if (count < 0)
			throw new Exception(String.format("Unexpected result. eSearch returned %s", count));
		return new EntrezHistoryKey(db, result.getWebEnv(), result.getQueryKey());
	}
}