package solcolator.luwak;

import java.util.Map;

import uk.co.flax.luwak.MonitorQuery;

/**
 * The class represents json serializable LUWAK query
 */
public class LuwakQuery extends MonitorQuery {
	private final String queryName;
	
	public LuwakQuery(String id, String queryName, String query, Map<String, String> queryMetadata) {
		super(id, query, queryMetadata);
		this.queryName = queryName;
	}
	
	public String getQueryName() {
		return queryName;
	}
}
