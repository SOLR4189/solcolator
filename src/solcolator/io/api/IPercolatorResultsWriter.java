package solcolator.io.api;

import java.io.IOException;

import org.apache.solr.common.util.NamedList;

public interface IPercolatorResultsWriter extends AutoCloseable {
	void init(NamedList<?> outputConfig) throws IOException;
	
	void writePercolatorResults(String docContent, String queryId) throws IOException;
	
	void close() throws IOException;
}
