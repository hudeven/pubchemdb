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
package edu.scripps.fl.pubchem.depo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.dom4j.util.FormControlVisitorSupport;

public class PCDepositionSystemSession extends PCWebSession {

	public boolean DEBUGGING = false;

	private static final Logger log = LoggerFactory.getLogger(PCDepositionSystemSession.class);

	private static Set<String> parameters = new HashSet<String>(Arrays.asList("wcev:src", "wcev:name", "wcev:data", "wc:scrollx", "wc:scrolly"));

	/**
	 * 
	 * Logs into your PubChem Deposition System account 
	 * 
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	public void login(String username, String password) throws Exception {
		Document doc = getDocument("http://" + SITE + "/deposit/deposit.cgi");

		Node formNode = doc.selectSingleNode("//form[@name='deplogform']");
		FormControlVisitorSupport fcvs = new FormControlVisitorSupport();
		formNode.accept(fcvs);

		Set<String> set = new HashSet<String>(parameters);
		set.add("login");
		set.add("password");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : fcvs.getFormParameters().entrySet()) {
			if (!set.contains(entry.getKey()))
				params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		addParameters(params, "wcev:src", "logmgr", "wcev:name", "login", "wcev:data", "true", "wc:scrollx", 0, "wc:scrolly", 0, "login", username,
				"password", password);

		String page = postPage("http://" + SITE + "/deposit/deposit.cgi", new UrlEncodedFormEntity(params, HTTP.UTF_8));
		if (page.contains("Error while Performing an Action"))
			throw new Exception("PubChem Login Failure: Error while Performing an Action");
	}
}