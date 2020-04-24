package solcolator.io.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.util.NamedList;

/**
 * Each queries reader have to implement this interface (see FileReader for example)
 */
public interface IQueryReader extends AutoCloseable{
	void init(NamedList<?> inputConfig) throws IOException;
	
	List<SolcolatorQuery> readAllQueries(Map<String, String> reqHandlerMetadata) throws IOException;
	
	SolcolatorQuery readByQueryId(String queryId, String queryName, Map<String, String> reqHandlerMetadata) throws IOException;
}
