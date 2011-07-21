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
