package edu.scripps.fl.pubchem.depo;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.dom4j.io.XMLWriter;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.dom4j.util.FormControlVisitorSupport;

public abstract class PCWebSessionBase {

	private static final Logger log = LoggerFactory.getLogger(PCWebSessionBase.class);
	static final String SITE = "pubchem.ncbi.nlm.nih.gov";
	public boolean DEBUGGING = false;
	
	public abstract HttpClient getHttpClient();
	
	@Override
	public void finalize() {
		getHttpClient().getConnectionManager().shutdown();
	}
	
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
			return getDocument(new ByteArrayInputStream(page.getBytes()));
		}

		public String asPage() {
			return page;
		}
	}
	
	protected Document getDocument(InputStream inputStream) throws IOException, ParserConfigurationException {
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode root = cleaner.clean(inputStream);
		DOMReader reader = new DOMReader();
		Document doc = reader.read(new DomSerializer(cleaner.getProperties(), true).createDOM(root));
		if (DEBUGGING) {
			File file = File.createTempFile(getClass().getName(), ".html");
			XMLWriter writer = new XMLWriter(new FileOutputStream(file));
			writer.write(doc);
			writer.close();
			Desktop.getDesktop().open(file);
		}
		return doc;
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
	
	protected Document getDocument(String uri) throws IOException, ParserConfigurationException {
		log.info("Getting uri: " + uri);
		HttpGet method = new HttpGet(uri);
		HttpResponse response = getHttpClient().execute(method);
		return getDocument(response.getEntity().getContent());
	}
	
	protected String getPage(HttpRequestBase method) throws IOException {
		HttpResponse response = getHttpClient().execute(method);
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
	
	protected Document postDocument(String uri, HttpEntity entity) throws IOException, ParserConfigurationException {
		log.info("Posting form: " + uri);
		HttpEntityEnclosingRequestBase method = new HttpPost(uri);
		method.setEntity(entity);
		HttpResponse response = getHttpClient().execute(method);
		return getDocument(response.getEntity().getContent());
	}

	protected String postPage(String uri, HttpEntity entity) throws IOException {
		log.info("Posting form: " + uri);
		HttpPost method = new HttpPost(uri);
		method.setEntity(entity);
		return getPage(method);
	}
}
