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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Node;

import edu.scripps.fl.pubchem.web.ELinkResult;
import edu.scripps.fl.pubchem.web.session.WebSessionBase;
import edu.scripps.fl.util.CollectionUtils;

public class ELinkWebSession extends WebSessionBase {

	private Long id;
	private String dbFrom;
	private String db;
	private String linkName;
	private Collection<Long> ids;

	public String getDbFrom() {
		return dbFrom;
	}

	public String getDb() {
		return db;
	}

	public Long getId() {
		return id;
	}

	public Collection<Long> getIds() {
		return ids;
	}

	public String getLinkName() {
		return linkName;
	}
	
	public Map<Long, List<ELinkResult>> getELinkResultsAsMap() throws Exception {
		Collection<ELinkResult> results = getELinkResults();
		return CollectionUtils.toMap("id", results);
	}
	

	public Collection<ELinkResult> getELinkResults() throws Exception {
		return getELinkResults(new ArrayList<ELinkResult>());
	}

	@SuppressWarnings("unchecked")
	public Collection<ELinkResult> getELinkResults(Collection<ELinkResult> relations) throws Exception {
		List<Object> params = new ArrayList<Object>(6 + 2 * getIds().size());
		params.addAll(Arrays.asList(new String[] { "dbfrom", getDbFrom(), "db", getDb() }));
		if( null != getLinkName() && ! "".equals(getLinkName()) ) {
			params.add("linkname");
			params.add(getLinkName());
		}
		for (Number id : ids) {
			params.add("id");
			params.add(id.longValue());
		}
		Document doc = EUtilsFactory.getInstance().getDocument("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi", params);

		List<Node> linkSets = doc.selectNodes("/eLinkResult/LinkSet");
		if (relations instanceof ArrayList)
			((ArrayList<ELinkResult>) relations).ensureCapacity(linkSets.size());

		for (Node linkSetNode : linkSets) {
			ELinkResult rel = new ELinkResult();
			rel.setDbFrom(linkSetNode.valueOf("./DbFrom/text()"));
			Long id = Long.parseLong(linkSetNode.valueOf("./IdList/Id/text()"));
			rel.setId(id);

			List<Node> linkSetDbs = linkSetNode.selectNodes("./LinkSetDb");
			for (Node linkSetDbNode : linkSetDbs) {
				String dbTo = linkSetDbNode.valueOf("./DbTo/text()");
				String linkName = linkSetDbNode.valueOf("./LinkName/text()");
				List<Node> idNodes = linkSetDbNode.selectNodes("./Link/Id");
				List<Long> relatedIds = new ArrayList<Long>(idNodes.size());
				for (Node idNode : idNodes) {
					long relatedId = Long.parseLong(idNode.getText());
					relatedIds.add(relatedId);
				}
				rel.setIds(dbTo, linkName, relatedIds);
			}
			relations.add(rel);
		}
		return relations;
	}

	public void setDbFrom(String dbFrom) {
		this.dbFrom = dbFrom;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setIds(Collection<Long> ids) {
		this.ids = ids;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

}
