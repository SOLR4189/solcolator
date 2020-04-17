package solcolator.io.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.util.NamedList;

import solcolator.luwak.LuwakQuery;

public interface IQueryReader extends AutoCloseable{
	void init(NamedList<?> inputConfig) throws IOException;;
	
	List<LuwakQuery> readAllQueries(Map<String, String> reqHandlerMetadata) throws IOException;
	
	LuwakQuery readByQueryId(String queryId, String queryName, Map<String, String> reqHandlerMetadata) throws IOException;
}
