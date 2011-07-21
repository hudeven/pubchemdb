package edu.scripps.fl.pubchem.web.session;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpClientBase {

	private HttpClient client = new DefaultHttpClient();

	public HttpClient getHttpClient() {
		return client;
	}

	public void setHttpClient(HttpClient client) {
		this.client = client;
	}

	@Override
	public void finalize() {
		getHttpClient().getConnectionManager().shutdown();
	}
}
