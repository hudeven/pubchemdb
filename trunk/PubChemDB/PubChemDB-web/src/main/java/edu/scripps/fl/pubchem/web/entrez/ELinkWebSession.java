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
package edu.scripps.fl.pubchem.web.entrez;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import edu.scripps.fl.pubchem.web.ELinkResult;
import edu.scripps.fl.pubchem.web.session.WebSessionBase;

public class ELinkWebSession extends WebSessionBase {

	private static final Logger log = LoggerFactory.getLogger(ELinkWebSession.class);

	public static ELinkWebSession newInstance(String dbFrom, String db, List<String> linkNames, List<Long> ids, String term) throws Exception {
		ELinkWebSession session = new ELinkWebSession();
		session.setDbFrom(dbFrom);
		session.setDb(db);
		session.setLinkName(StringUtils.join(linkNames, ","));
		session.setIds(ids);
		session.setTerm(term);
		return session;
	}
	
	public static ELinkWebSession newInstance(String dbFrom, List<String> linkNames, EntrezHistoryKey key, String term) throws Exception {
		ELinkWebSession session = new ELinkWebSession();
		session.setDbFrom(dbFrom);
		session.setDb(key.getDatabase());
		session.setLinkName(StringUtils.join(linkNames, ","));
		session.setKey(key);
		session.setTerm(term);
		return session;
	}

	private Long id;
	private String dbFrom;
	private String db;
	private String linkName;
	private Collection<Long> ids;
	private EntrezHistoryKey key = null;
	private String term;
	private List<ELinkResult> results;

	public Set<Long> getAllIds(String linkName) throws Exception {
		log.info("Fetching ids from elink session.");
		Set<Long> ids = new HashSet<Long>();
		log.info("Fetching ids from elink session.");
		for (ELinkResult result : getELinkResults()) {
			List<Long> idList = result.getIds(db, linkName);
			if (null != idList)
				ids.addAll(idList);
		}
		log.info("Retrieved ids from elink session.");
		return ids;
	}

	public String getDb() {
		return db;
	}

	public String getDbFrom() {
		return dbFrom;
	}

	public List<ELinkResult> getELinkResults() {
		return results;
	}

	public Map<Long, ELinkResult> getELinkResultsAsMap() throws Exception {
		log.info("getting map from elink session.");
		Map map = new HashMap();
		for (ELinkResult result : getELinkResults()) {
			map.put(result.getId(), result);
		}
		log.info("obtained map from elink session.");
		return map;
	}

	@SuppressWarnings("unchecked")
	protected Collection<ELinkResult> getELinkResultsSAX(final Collection<ELinkResult> relations) throws Exception {
		log.info("Memory in use before inputstream: " + memUsage());
		InputStream is = EUtilsWebSession.getInstance().getInputStream("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi", getParams()).call();
		log.info("Memory in use after inputstream: " + memUsage());
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		DefaultHandler handler = new DefaultHandler() {
			private ELinkResult result;
			private List<Long> idList;
			private StringBuffer buf;
			private int depth;
			private String linkName;
			private String dbTo;

			public void characters(char ch[], int start, int length) throws SAXException {
				buf.append(ch, start, length);
			}

			public void endElement(String uri, String localName, String qName) throws SAXException {
				if (qName.equalsIgnoreCase("LinkSet"))
					relations.add(result);
				else if (qName.equalsIgnoreCase("LinkSetDb")) {
					if (idList.size() > 0)
						result.setIds(dbTo, linkName, idList);
				} else if (qName.equalsIgnoreCase("LinkName"))
					linkName = buf.toString();
				else if (qName.equalsIgnoreCase("dbTo"))
					dbTo = buf.toString();
				else if (qName.equalsIgnoreCase("DbFrom"))
					result.setDbFrom(buf.toString());
				else if (qName.equalsIgnoreCase("Id")) {
					Long id = Long.parseLong(buf.toString());
					if (depth == 4)
						result.setId(id);
					else if (depth == 5)
						idList.add(id);
				}
				depth--;
			}

			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				depth++;
				buf = new StringBuffer();
				if (qName.equalsIgnoreCase("LinkSet"))
					result = new ELinkResult();
				else if (qName.equalsIgnoreCase("LinkSetDb"))
					idList = new ArrayList();
			}
		};
		log.info("Memory in use before parsing: " + memUsage());
		log.info("Parsing elink results using SAX.");
		try {
			saxParser.parse(is, handler);
		}
		catch(SAXParseException ex) {
			throw new Exception("Error parsing ELink output: " + ex.getMessage());
		}
		log.info("Finished parsing elink results.");
		log.info("Memory in use after parsing: " + memUsage());
		return relations;
	}

	public Long getId() {
		return id;
	}

	public Collection<Long> getIds() {
		return ids;
	}

	public EntrezHistoryKey getKey() {
		return key;
	}

	public String getLinkName() {
		return linkName;
	}

	protected List<Object> getParams() throws Exception {
		List<Object> params = new ArrayList();
		params.add("db");
		params.add(getDb());
		
		if (null != getLinkName() && !"".equals(getLinkName())) {
			params.add("linkname");
			params.add(getLinkName());
		}
		
		if ( null != getKey() ) {
//			params.add("dbfrom");
//			params.add(key.getDatabase());
			params.add("WebEnv");
			params.add(getKey().getWebEnv());
			params.add("query_key");
			params.add(getKey().getQueryKey());
		}
		else if (ids.size() > 0 ) {
			params.add("dbfrom");
			params.add(getDbFrom());
			for (Number id : ids) {
				params.add("id");
				params.add(id.longValue());
			}
		}
		else
			throw new Exception("ELink: must provide either a List of Ids or an Entrez History Key");
		
		if (null != getTerm() && !"".equals(getTerm())) {
			params.add("term");
			params.add(getTerm());
		}
		params.add("version");
		params.add("2.0");
		return params;
	}

	public String getTerm() {
		return term;
	}

	private long memUsage() {
		long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		return mem / 1024 / 1024;
	}

	@SuppressWarnings("unchecked")
	public List<ELinkResult> run() throws Exception {
		getELinkResultsSAX(results = new ArrayList(1000));
		return results;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public void setDbFrom(String dbFrom) {
		this.dbFrom = dbFrom;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setIds(Collection<Long> ids) {
		this.ids = ids;
	}

	public void setKey(EntrezHistoryKey key) {
		this.key = key;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public void setTerm(String term) {
		this.term = term;
	}
}