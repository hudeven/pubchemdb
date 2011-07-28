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

public class EntrezHistoryKey {

	private String database;
	private String webEnv;
	private String queryKey;

	public EntrezHistoryKey(String database, String webEnv, String queryKey) {
		setDatabase(database);
		setWebEnv(webEnv);
		setQueryKey(queryKey);
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getWebEnv() {
		return webEnv;
	}

	public void setWebEnv(String webEnv) {
		this.webEnv = webEnv;
	}

	public String getQueryKey() {
		return queryKey;
	}

	public void setQueryKey(String queryKey) {
		this.queryKey = queryKey;
	}

}
