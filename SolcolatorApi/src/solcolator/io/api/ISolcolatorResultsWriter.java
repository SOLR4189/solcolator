package solcolator.io.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

/**
 * Each solcolator results writer have to implement this interface (see FileWriter for example)
 */
public interface ISolcolatorResultsWriter extends AutoCloseable {
	void init(NamedList<?> outputConfig) throws IOException;

	void writeSolcolatorResults(Map<String, List<SolrInputDocument>> docs) throws IOException;
	
	List<String> getFl();
	
	void close() throws IOException;
}
