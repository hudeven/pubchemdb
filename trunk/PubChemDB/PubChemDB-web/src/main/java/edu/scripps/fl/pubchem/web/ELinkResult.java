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
package edu.scripps.fl.pubchem.web;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.Factory;

public class ELinkResult {

	private Long id;
	private String dbFrom;
	private Map<String, Map<String,List<Long>>> linkSet = LazyMap.decorate(new HashMap(), new Factory() {
		public Object create() {
			return new HashMap();
		}
	});
	
	public Set<String> getDatabases() {
		return linkSet.keySet();
	}

	public Set<String> getLinks(String database) {
		return linkSet.get(database).keySet();
	}
	
	public String getDbFrom() {
		return dbFrom;
	}

	public Long getId() {
		return id;
	}

	public List<Long> getIds(String database, String link) {
		return linkSet.get(database).get(link);
	}
	
	public void setIds(String database, String link, List<Long> ids) {
		this.linkSet.get(database).put(link, ids);
	}

	public void setDbFrom(String dbFrom) {
		this.dbFrom = dbFrom;
	}

	public void setId(Long id) {
		this.id = id;
	}
}