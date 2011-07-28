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
package edu.scripps.fl.pubchem.web.session;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;

import edu.scripps.fl.dom4j.util.AppendingVisitorSupport;
import edu.scripps.fl.pubchem.web.PCBioActivityAssaySummary;
import edu.scripps.fl.pubchem.web.PCBioActivityCompoundSummary;
import edu.scripps.fl.pubchem.web.PCOutcomeCounts;
import edu.scripps.fl.util.CollectionUtils;

public class PCWebSession extends WebSessionBase {

	private static final Logger log = LoggerFactory.getLogger(PCWebSession.class);
	public static final String SITE = "pubchem.ncbi.nlm.nih.gov";

	protected class WaitOnRequestId {
		private Pattern errorPattern = Pattern.compile("<pre>Error: Caught CException: (.+)</pre>");
		private final Logger log = LoggerFactory.getLogger(PCDepositionSystemSession.class);
		private String page;
		private Pattern reqIdPattern = Pattern.compile("\\W(reqid=\\d+)");
//		private Pattern reqIdPattern = Pattern.compile("([^\"\']+reqid=\\d+)");

		public WaitOnRequestId(String page) throws Exception {
			this.page = page;
			while (true) {
				Matcher matcher = reqIdPattern.matcher(this.page);
				if (matcher.find()) {
					String reqid = matcher.group(1);
					Thread.sleep(2000);
					String url = "http://" + SITE + "/assay/assay.cgi?" + reqid;
					log.info(String.format("found requestId in page. Next uri: %s", url));
					this.page = getPage(url);
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
			return getDocumentFromHtml(new ByteArrayInputStream(page.getBytes()));
		}

		public String asPage() {
			return page;
		}
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
		List<NameValuePair> params = addParameters(new ArrayList<NameValuePair>(), "aid", aid, "q", "r", "exptype", format, "zip", "gzip", "resultsummary",
				"detail", "releasehold", "t");
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
		HttpResponse response = getHttpClient().execute(new HttpGet(uri));
		return response.getEntity().getContent();
	}

	/**
	 * 
	 * Returns an SDF file of substances in an AID.<br>
	 * It only works if on-hold SIDs are from your own center. If you submit
	 * AIDs for other centers, you cannot download their SIDs!
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
	 * This is a convenience method. It first calls getAssayCSV because I can't
	 * figure out the link from the DataTable page.
	 * 
	 * @param aid
	 * @return
	 * @throws Exception
	 */
	public List<Long> getAssaySIDs(int aid) throws Exception {
		// really simple, no dependency parsing of the CSV file because I can't
		// get the link from the DataTable page to work :-(
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
		List<NameValuePair> params = addParameters(new ArrayList<NameValuePair>(), "aid", aid, "q", "r", "pagefrom", "BioAssaySummary", "acount", "0",
				"releasehold", "t", "activity" + aid, "");
		URI uri = URIUtils.createURI("http", SITE, 80, "/assay/assay.cgi", URLEncodedUtils.format(params, "UTF-8"), null);
		return new WaitOnRequestId(uri).asPage();
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
		HttpResponse response = getHttpClient().execute(new HttpGet(href));
		return response.getEntity().getContent();
	}

	/**
	 * 
	 * Returns counts of active, all, inactive, inconclusive and probe
	 * substances in the aid.
	 * 
	 * @param aid
	 * @return OutComeCount
	 * @throws Exception
	 */
	public PCOutcomeCounts getSubstanceOutcomeCounts(int aid) throws Exception {
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
		PCOutcomeCounts oc = new PCOutcomeCounts();
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
	
	protected InputStream getBioActivityCompoundSummaryAsStream(List<Long> cids) throws Exception {
		List<NameValuePair> params = addParameters(new ArrayList<NameValuePair>(), "cid", StringUtils.join(cids, ","), "q", "cmp", "exptype", "csv");
		URI uri = URIUtils.createURI("http", SITE, 80, "/assay/assaytool.cgi", URLEncodedUtils.format(params, "UTF-8"), null);
		Document doc = new WaitOnRequestId(uri).asDocument();
		return getFtpLinkAsStream(doc);
	}
	
	public Map<Long, PCBioActivityCompoundSummary> getBioActivityCompoundSummaryAsMap(List<Long> cids) throws Exception {
		List<PCBioActivityCompoundSummary> list = getBioActivityCompoundSummary(cids);
		return CollectionUtils.toMap("CID", list);
	}
	
	public List<PCBioActivityCompoundSummary> getBioActivityCompoundSummary(List<Long> cids) throws Exception {
		List<PCBioActivityCompoundSummary> list = new ArrayList<PCBioActivityCompoundSummary>(cids.size());
		CsvReader reader = new CsvReader(new InputStreamReader(getBioActivityCompoundSummaryAsStream(cids)), ',');
		reader.readHeaders();
		while( reader.readRecord() ) {
			String line[] = reader.getValues();
			PCBioActivityCompoundSummary sum = new PCBioActivityCompoundSummary();
			BeanUtils.setProperty(sum, "CID", line[0]);
			BeanUtils.setProperty(sum, "IUPAC", line[1]);
			BeanUtils.setProperty(sum, "synonyms", line[2]);
			BeanUtils.setProperty(sum, "bioAssayProbes", line[3]);
			BeanUtils.setProperty(sum, "bioAssayActives", line[4]);
			BeanUtils.setProperty(sum, "bioAssayTested", line[5]);
			BeanUtils.setProperty(sum, "activeContentrationBelow1uM", line[6]);
			BeanUtils.setProperty(sum, "activeContentrationBelow1nM", line[7]);
			BeanUtils.setProperty(sum, "activeProteins", line[8]);
			BeanUtils.setProperty(sum, "testedProteins", line[9]);
			BeanUtils.setProperty(sum, "activeContentrationLowerBound", line[10]);
			BeanUtils.setProperty(sum, "activeContentrationUpperBound", line[11]);
			list.add(sum);
		}
		return list;
	}

	protected InputStream getBioActivityAssaySummaryAsStream(List<Long> cids) throws Exception {
		List<NameValuePair> params = addParameters(new ArrayList<NameValuePair>(), "cid", StringUtils.join(cids, ","), "q", "cids", "exptype", "bioactivitycsv");
		URI uri = URIUtils.createURI("http", SITE, 80, "/assay/assay.cgi", URLEncodedUtils.format(params, "UTF-8"), null);
		Document doc = new WaitOnRequestId(uri).asDocument();
		return getFtpLinkAsStream(doc);
	}
	
	public Map<Long, PCBioActivityAssaySummary> getBioActivityAssaySummaryAsMap(List<Long> cids) throws Exception {
		List<PCBioActivityAssaySummary> list = getBioActivityAssaySummary(cids);
		return CollectionUtils.toMap("AID", list);
	}

	public List<PCBioActivityAssaySummary> getBioActivityAssaySummary(List<Long> cids) throws Exception {
		List<PCBioActivityAssaySummary> list = new ArrayList<PCBioActivityAssaySummary>(cids.size());
		CsvReader reader = new CsvReader(new InputStreamReader(getBioActivityAssaySummaryAsStream(cids)), ',');
		reader.readHeaders();
		reader.readRecord();
		String line[] = reader.getValues();
		// csv parsing written this way b/c of pubchem pub (1 row csv, 1000's columns!)
		for (int ii = 0; ii < line.length; ii += 10) {
			if( line.length - ii < 10 )
				break;
			PCBioActivityAssaySummary act = new PCBioActivityAssaySummary();
			BeanUtils.setProperty(act, "AID", line[ii]);
			BeanUtils.setProperty(act, "probeCount", line[ii + 1]);
			BeanUtils.setProperty(act, "activeCount", line[ii + 2]);
			BeanUtils.setProperty(act, "inactiveCount", line[ii + 3]);
			BeanUtils.setProperty(act, "testedCount", line[ii + 4]);
			BeanUtils.setProperty(act, "activesLessThan1uM", line[ii + 5]);
			BeanUtils.setProperty(act, "activesLessThan1nM", line[ii + 6]);
			if( ! "".equals(line[ii+7]) ) {
				String[] actRange = line[ii + 7].split(" - ");
				BeanUtils.setProperty(act, "activityConcentrationMin", actRange[0]);
				BeanUtils.setProperty(act, "activityConcentrationMax", actRange[1]);
			}
			BeanUtils.setProperty(act, "bioAssayName", line[ii + 8]);
			BeanUtils.setProperty(act, "proteinTargetName", line[ii + 9]);

			list.add(act);
		}
		return list;
	}
}
