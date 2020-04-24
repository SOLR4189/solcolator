package solcolator.io.api;

import java.util.Map;

public class SolcolatorQuery {
	private final String queryId;
	private final String queryName;
	private final String query;
	private final Map<String, String> queryMetadata;
	
	public SolcolatorQuery(String queryId, String queryName, String query, Map<String, String> queryMetadata) {
		this.queryId = queryId;
		this.queryName = queryName;
		this.query = query;
		this.queryMetadata = queryMetadata;
	}
	
	public String getQueryId() {
		return queryId;
	}
	
	public String getQueryName() {
		return queryName;
	}

	public String getQuery() {
		return query;
	}

	public Map<String, String> getQueryMetadata() {
		return queryMetadata;
	}
}
