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

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.dom4j.io.XMLWriter;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.dom4j.util.AppendingVisitorSupport;
import edu.scripps.fl.dom4j.util.FormControlVisitorSupport;

public class PCDepositionSystemSession {

	protected class WaitOnRequestId {
		private Pattern errorPattern = Pattern.compile("<pre>Error: Caught CException: (.+)</pre>");
		private final Logger log = LoggerFactory.getLogger(PCDepositionSystemSession.class);
		private String page;
		private Pattern reqIdPattern = Pattern.compile("([^\"]+reqid=\\d+)");

		public WaitOnRequestId(String page) throws Exception {
			this.page = page;
			while (true) {
				Matcher matcher = reqIdPattern.matcher(this.page);
				if (matcher.find()) {
					String url = matcher.group(1);
					Thread.sleep(2000);
					if (!url.startsWith("http://"))
						url = "http://" + SITE + "/assay/" + url;
					log.info(String.format("found requestId in page. Next uri: %s", url));
					this.page = PCDepositionSystemSession.this.getPage(url);
				} else
					break;
			}

			Matcher matcher = errorPattern.matcher(page);
			if (matcher.find()) {
				File file = File.createTempFile(getClass().getName(), ".html");
				FileUtils.writeStringToFile(file, page);
				String error = String.format("PubChem problem: %s. Please check %s", matcher.group(1), file);
				throw new Exception(error);
			}
		}

		public WaitOnRequestId(URI uri) throws Exception {
			this(getPage(uri));
		}

		public Document asDocument() throws Exception {
			return getDocument(new ByteArrayInputStream(page.getBytes()));
		}

		public String asPage() {
			return page;
		}
	}

	public boolean DEBUGGING = false;

	private static final Logger log = LoggerFactory.getLogger(PCDepositionSystemSession.class);

	private static Set<String> parameters = new HashSet<String>(Arrays.asList("wcev:src", "wcev:name", "wcev:data", "wc:scrollx", "wc:scrolly"));

	static final String SITE = "pubchem.ncbi.nlm.nih.gov";

	protected Document getDocument(InputStream inputStream) throws IOException, ParserConfigurationException {
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode root = cleaner.clean(inputStream);
		DOMReader reader = new DOMReader();
		Document doc = reader.read(new DomSerializer(cleaner.getProperties(), true).createDOM(root));
		if (DEBUGGING) {
			File file = File.createTempFile(PCDepositionSystemSession.class.getName(), ".html");
			XMLWriter writer = new XMLWriter(new FileOutputStream(file));
			writer.write(doc);
			writer.close();
			Desktop.getDesktop().open(file);
		}
		return doc;
	}

	private DefaultHttpClient client;

	public PCDepositionSystemSession() {
		client = new DefaultHttpClient();
	}

	protected void addFormParts(Node formNode, MultipartEntity entity, Set<String> ignore) throws UnsupportedEncodingException {
		FormControlVisitorSupport fcvs = new FormControlVisitorSupport();
		formNode.accept(fcvs);
		for (Map.Entry<String, String> entry : fcvs.getFormParameters().entrySet()) {
			if (!ignore.contains(entry.getKey()))
				entity.addPart(entry.getKey(), new StringBody(entry.getValue()));
		}
	}

	protected List<NameValuePair> addParameters(List<NameValuePair> params, Object... pairs) {
		for (int ii = 0; ii < pairs.length; ii += 2)
			params.add(new BasicNameValuePair(pairs[ii].toString(), pairs[ii + 1].toString()));
		return params;
	}

	protected MultipartEntity addParts(MultipartEntity entity, Object... pairs) throws UnsupportedEncodingException {
		for (int ii = 0; ii < pairs.length; ii += 2)
			entity.addPart(pairs[ii].toString(), new StringBody(pairs[ii + 1].toString()));
		return entity;
	}

	@Override
	public void finalize() {
		client.getConnectionManager().shutdown();
	}

	/**
	 * 
	 * Returns CSV data file for the AID.
	 * 
	 * @param aid
	 * @return
	 * @throws Exception
	 */
	public InputStream getAssayCSV(int aid) throws Exception {
		return getAssayFile("alldata", "csv", aid);
	}

	protected InputStream getAssayFile(String format, String ext, int aid) throws Exception {
		List<NameValuePair> params = addParameters(new ArrayList<NameValuePair>(), "aid", aid, "q", "r", "exptype", format, "zip", "gzip",
				"resultsummary", "detail", "releasehold", "t");
		URI uri = URIUtils.createURI("http", SITE, 80, "/assay/assay.cgi", URLEncodedUtils.format(params, "UTF-8"), null);
		Document document = new WaitOnRequestId(uri).asDocument();
		return getFtpLinkAsStream(document);
	}
	
	/**
	 * 
	 * Returns a description xml file for an aid
	 * 
	 * @param aid
	 * @return
	 * @throws Exception
	 */
	public InputStream getDescrXML(int aid) throws Exception {
		// if we don't provide the version, we get the latest one anyway.
		// http://pubchem.ncbi.nlm.nih.gov/assay/assay.cgi?aid=2551&version=1.1&q=expdesc_xmldisplay
		List<NameValuePair> params = addParameters(new ArrayList<NameValuePair>(), "aid", aid, "q", "expdesc_xmldisplay");
		URI uri = URIUtils.createURI("http", SITE, 80, "/assay/assay.cgi", URLEncodedUtils.format(params, "UTF-8"), null);
		HttpResponse response = client.execute(new HttpGet(uri));
		return response.getEntity().getContent();
	}

	/**
	 * 
	 * Returns an SDF file of substances in an AID.<br>
	 * It only works if on-hold SIDs are from your own center. If you submit AIDs for other centers, you cannot download their SIDs!
	 * 
	 * @param aid
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public InputStream getAssaySDF(int aid) throws Exception {
		String page = getDataTablePage(aid);
		String url = getLinkFromJavaScript(page, "Substance Structure");
		Document document = getDocument(url);
		MultipartEntity entity = new MultipartEntity();
		Node formNode = document.selectSingleNode("//form[1]");
		addFormParts(formNode, entity, new HashSet(Arrays.asList("savejob")));
		document = new WaitOnRequestId(postPage("http://" + SITE + "/pc_fetch/pc_fetch.cgi", entity)).asDocument();
		return getFtpLinkAsStream(document);
	}

	
	/**
	 * 
	 * Returns a list of SIDs that are present in an AID.<br>
	 * This is a convenience method. It first calls getAssayCSV because I can't figure out the link from the DataTable page.
	 * 
	 * @param aid
	 * @return
	 * @throws Exception
	 */
	public List<Long> getAssaySIDs(int aid) throws Exception {
		// really simple, no dependency parsing of the CSV file because I can't get the link from the DataTable page to work :-(
		InputStream is = getAssayCSV(aid);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = br.readLine(); // ignore header line;
		List<Long> list = new ArrayList<Long>();
		while (null != (line = br.readLine())) {
			String items[] = line.split(",");
			list.add(Long.parseLong(items[1])); // SID in 2nd column
		}
		return list;
	}

	/**
	 * 
	 * Returns the full XML file for an AID.
	 * 
	 * @param aid
	 * @return
	 * @throws Exception
	 */
	public InputStream getAssayXML(int aid) throws Exception {
		return getAssayFile("xml", "xml", aid);
	}

	protected String getDataTablePage(int aid) throws Exception {
		List<NameValuePair> params = addParameters(new ArrayList<NameValuePair>(), "aid", aid, "q", "r", "pagefrom", "BioAssaySummary", "acount",
				"0", "releasehold", "t", "activity" + aid, "");
		URI uri = URIUtils.createURI("http", SITE, 80, "/assay/assay.cgi", URLEncodedUtils.format(params, "UTF-8"), null);
		return new WaitOnRequestId(uri).asPage();
	}

	protected Document getDocument(String uri) throws IOException, ParserConfigurationException {
		log.info("Getting uri: " + uri);
		HttpGet method = new HttpGet(uri);
		HttpResponse response = client.execute(method);
		return getDocument(response.getEntity().getContent());
	}

	protected InputStream getFtpLinkAsStream(Document document) throws Exception {
		Node linkNode = document.selectSingleNode("//a[@href=starts-with(.,'ftp://')]");
		if (null != linkNode) {
			String url = linkNode.valueOf("@href");
			log.info("Found ftp link: " + url);
			return new GZIPInputStream(new URL(url).openStream());
		}
		File file = File.createTempFile(getClass().getName(), ".html");
		document.write(new FileWriter(file));
		throw new Exception("Received a PubChem web page without an ftp link. Please check " + file);
	}

	protected String getLinkFromJavaScript(String page, String linkName) throws Exception {
		Pattern pattern = Pattern.compile("\\[\"" + linkName + "\"\\s*,\\s*\"([^\\\"]+)");
		Matcher matcher = pattern.matcher(page);
		if (!matcher.find()) {
			File file = File.createTempFile(getClass().getName(), ".html");
			FileUtils.writeStringToFile(file, page);
			String error = "Could not determine " + linkName + " link on PubChem page. Please check " + file;
			throw new Exception(error);
		}
		String url = matcher.group(1);
		log.info("Found javascript link: " + url);
		return url;
	}

	protected String getPage(HttpRequestBase method) throws IOException {
		HttpResponse response = client.execute(method);
		String page = IOUtils.toString(response.getEntity().getContent());
		if (DEBUGGING) {
			File file = File.createTempFile(getClass().getName(), ".html");
			IOUtils.write(page, new FileOutputStream(file));
			Desktop.getDesktop().open(file);
		}
		return page;
	}

	protected String getPage(String uri) throws IOException {
		log.info("Getting uri: " + uri);
		return getPage(new HttpGet(uri));
	}

	protected String getPage(URI uri) throws IOException {
		log.info("Getting uri: " + uri);
		return getPage(new HttpGet(uri));
	}

	/**
	 * Fetches an SDF file for the given CID or SID.
	 * 
	 * @param type
	 *            "cid" or "sid"
	 * @param id
	 *            the cid or sid
	 * @return sdf file as stream
	 * @throws Exception
	 */
	public InputStream getSDF(String type, int id) throws Exception {
		String href = String.format("http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?%s=%s&disopt=SaveSDF", type, id);
		HttpResponse response = client.execute(new HttpGet(href));
		return response.getEntity().getContent();
	}

	/**
	 * 
	 * Returns counts of active, all, inactive, inconclusive and probe substances in the aid.
	 * 
	 * @param aid
	 * @return OutComeCount 	
	 * @throws Exception
	 */
	public OutcomeCount getSubstanceOutcomeCounts(int aid) throws Exception {
		Document doc = getDocument("http://" + SITE + "/assay/assay.cgi?aid=" + aid);
		Node node = doc.selectSingleNode("//b[. = 'Substances: ']").getParent();
		AppendingVisitorSupport visitor = new AppendingVisitorSupport();
		node.accept(visitor);
		String text = visitor.getText();
		Pattern sectionPattern = Pattern.compile("Substances:(.+)", Pattern.MULTILINE);
		Matcher matcher = sectionPattern.matcher(text);
		matcher.find();
		text = matcher.group(1);
		Pattern countPattern = Pattern.compile("([\\w]+):\\s+(\\d+)");
		matcher = countPattern.matcher(text);
		OutcomeCount oc = new OutcomeCount();
		while (matcher.find()) {
			String outcome = matcher.group(1);
			int count = Integer.parseInt(matcher.group(2));
			if ("All".equalsIgnoreCase(outcome))
				oc.all = count;
			else if ("Active".equalsIgnoreCase(outcome))
				oc.active = count;
			else if ("Inactive".equalsIgnoreCase(outcome))
				oc.inactive = count;
			else if ("Inconclusive".equalsIgnoreCase(outcome))
				oc.inconclusive = count;
			else if ("Probe".equalsIgnoreCase(outcome))
				oc.probe = count;
		}
		return oc;
	}

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

	protected Document postDocument(String uri, HttpEntity entity) throws IOException, ParserConfigurationException {
		log.info("Posting form: " + uri);
		HttpEntityEnclosingRequestBase method = new HttpPost(uri);
		method.setEntity(entity);
		HttpResponse response = client.execute(method);
		return getDocument(response.getEntity().getContent());
	}

	protected String postPage(String uri, HttpEntity entity) throws IOException {
		log.info("Posting form: " + uri);
		HttpPost method = new HttpPost(uri);
		method.setEntity(entity);
		return getPage(method);
	}
}