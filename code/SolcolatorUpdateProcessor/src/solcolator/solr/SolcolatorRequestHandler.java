package solcolator.solr;

import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolcolatorRequestHandler extends SearchHandler {
	private final static Logger log = LoggerFactory.getLogger(SolcolatorQueriesRequestHander.class);
	
	public final static String SOLCOLATOR_OK_RESPONSE_HEADER = "solcolatorOkResponse";
	public final static String SOLCOLATOR_ERROR_RESPONSE_HEADER = "solcolatorErrorResponse";

	public void writeOkToRspAndLog(SolrQueryRequest req, SolrQueryResponse rsp, String msg) {
		rsp.add(SOLCOLATOR_OK_RESPONSE_HEADER, msg);
		log.info(msg);
	}
	
	public void writeErrorToRspAndLog(SolrQueryRequest req, SolrQueryResponse rsp, String errMsg) {
		rsp.add(SOLCOLATOR_ERROR_RESPONSE_HEADER, errMsg);
		log.error(errMsg);
	}
	
	@Override
	public String getDescription() {
		return "Enpoint for update solcolator queries";
	}

	@Override
	public String getSource() {
		return "SOLR4189 production";
	}
}
