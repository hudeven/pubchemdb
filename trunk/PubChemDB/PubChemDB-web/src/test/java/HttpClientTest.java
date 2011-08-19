import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.helpers.DefaultHandler;

public class HttpClientTest {

	public static void main(String[] args) throws Exception {
		new HttpClientTest().runTest();
	}

	public void runTest() throws Exception {
		List<NameValuePair> list = new ArrayList(50000);
		list.add(new BasicNameValuePair("db", "pcassay"));
		list.add(new BasicNameValuePair("dbfrom", "pcassay"));
		list.add(new BasicNameValuePair("linkname", "pcassay_pcassay_neighbor_list"));
		// read in ~50000 ids from file. These are existing uids in the pcassay
		// database.
		BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("ids.txt")));
		String line = null;
		while (null != (line = reader.readLine())) {
			list.add(new BasicNameValuePair("id", line));
		}
		list.add(new BasicNameValuePair("term", "summary[activityoutcomemethod]"));
		list.add(new BasicNameValuePair("version", "2.0"));
		list.add(new BasicNameValuePair("email", "...@scripps.edu"));
		list.add(new BasicNameValuePair("tool", "...@scripps.edu"));

		System.out.println("#NameValuePairs: " + list.size());

		HttpPost post = new HttpPost("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi");
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list);
		post.setEntity(entity);
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(post);

		// dump to local file so we can refer to it.
		File file = File.createTempFile("eutils", ".xml");
		System.out.println("Copying eUtils stream to: " + file);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		response.getEntity().writeTo(bos);
		bos.flush();
		bos.close();
		InputStream is = new BufferedInputStream(new FileInputStream(file));

		// attempt to parse with sax. There will be an error if only a fragment
		// of the xml is returned.
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(is, new DefaultHandler() {
		});
	}
}