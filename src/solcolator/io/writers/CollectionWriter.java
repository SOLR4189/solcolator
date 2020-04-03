package solcolator.io.writers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solcolator.io.api.ISolcolatorResultsWriter;

/**
 * This writer is designed to write percolator results to another SOLR collection
 * Parameters which must be provided: zookeepers, collection name and fields list
 * 
 * Config for example:
    <str name="zookeepers">zootest01:2181,zootest03:2181,zootest04:2181</str>
	<str name="collectionName">PercolatorTmp</str>
	<str name="collectionFl">*</str>
 *
 */
public class CollectionWriter implements ISolcolatorResultsWriter, AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(CollectionWriter.class);
	private static final String ZOOKEEPERS = "zookeepers";
	private static final String COLLECTION_NAME = "collectionName";
	private static final String COLLECTION_FL = "collectionFl";
	
	private List<String> fl;
	private CloudSolrClient solrClient;

	@Override
	public void init(NamedList<?> outputConfig) throws IOException {
		String zookeepers = (String) outputConfig.get(ZOOKEEPERS);
		String collection = (String) outputConfig.get(COLLECTION_NAME);
		this.fl = Arrays.asList(((String) outputConfig.get(COLLECTION_FL)).split(","));
		this.solrClient = new CloudSolrClient.Builder().withZkHost(Arrays.asList(zookeepers.split(","))).build();
		this.solrClient.setDefaultCollection(collection);
	}

	@Override
	public void writeSolcolatorResults(Map<String, SolrInputDocument> docs) throws IOException {
		try {
			solrClient.add(docs.values());
		} catch (SolrServerException e) {
			log.error(String.format("Bulk of %d docs failed to index to collection %s@%s due to %s",
					docs.size(),
					solrClient.getDefaultCollection(),
					solrClient.getZkHost(),
					e));
		}
	}

	@Override
	public List<String> getFl() {
		return fl;
	}
	
	@Override
	public void close() throws IOException {
		solrClient.close();
	}
}
