package solcolator.solr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import solcolator.luwak.LuwakQueriesManager;
import solcolator.luwak.LuwakQuery;
import solcolator.percolator.common.SolrUtils;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

/**
 * Solr endpoint for add/delete/update/refresh/reread solcolator queries. All request for this endpoint must contain query, queryid and command.
 *
 */
public class SolcolatorQueriesRequestHander extends SolcolatorRequestHandler {
	public final static String QUERY_ID = "queryid";
	public final static String QUERY_NAME = "queryname";
	public final static String COMMAND = "command";
	public final static String NAME = "/update_solcolator_queries";
	
	@Override
	public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {	
		String queryId = null;
		SolrParams reqParams = req.getParams();
		String reqStr = SolrUtils.solrParamsToString(reqParams);
		
		try {
			// Check request command
			String command = reqParams.get(COMMAND);			
			SolcolatorQueriesRequestCommand reqCommand = SolcolatorQueriesRequestCommand.getIfExists(command);
			if (!isReqCommandValid(req, rsp, reqCommand)) { return; }
			
			LuwakQueriesManager manager = LuwakQueriesManager.getQueriesAndCapsManager();
			
			switch (reqCommand) {
				case REFRESH:
					manager.updateAllQueries();
					break;
					
				case REREAD:
					manager.loadQueriesToSolcolator(getRequestHandlerMetadata(req.getCore()));
					break;
					
				case UPDATE:
					// Check query id
					queryId = reqParams.get(QUERY_ID);			
					if (!isQueryIdValid(req, rsp, queryId)) { return; }
					
					// Check query name
					String queryName = reqParams.get(QUERY_NAME);			
					if (!isQueryNameValid(req, rsp, queryName)) { return; }
					
					LuwakQuery query = manager.getQueryReader().readByQueryId(queryId, queryName, getRequestHandlerMetadata(req.getCore()));
					
					manager.updateQueryInMonitor(query);
					break;
					
				case DELETE:
					// Check query id
					queryId = reqParams.get(QUERY_ID);			
					if (!isQueryIdValid(req, rsp, queryId)) { return; }
					
					manager.deleteQueryFromMonitor(queryId);
					break;
		
				default:
					throw new UnsupportedOperationException("Unsupported query command: " + reqCommand);
			}
			
			writeOkToRspAndLog(req, rsp, String.format("%s request %s was handled successfully", command, reqStr));
		} catch (Exception e) {
			writeErrorToRspAndLog(req, rsp, String.format("Request %s was failed due to %s", reqStr, e));
		}
	}

	/**
	 * Check if query contains queryname and it isn't empty
	 * @param req - solr request
	 * @param rsp - solr response
	 * @param queryName - query name
	 * @return if queryname is valid or not
	 */
	private boolean isQueryNameValid(SolrQueryRequest req, SolrQueryResponse rsp, String queryName) {
		if (queryName == null || queryName.isEmpty()) {
			writeErrorToRspAndLog(req, rsp, "Query will not be processed due to queryName is absent or empty");		
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check if query contains queryid and it isn't empty
	 * @param req - solr request
	 * @param rsp - solr response
	 * @param queryId - query id
	 * @return if queryid is valid or not
	 */
	private boolean isQueryIdValid(SolrQueryRequest req, SolrQueryResponse rsp, String queryId) {
		if (queryId == null) {
			writeErrorToRspAndLog(req, rsp, "Query will not be processed due to queryid is absent");		
			
			return false;
		}
		
		if (queryId.isEmpty()) {
			writeErrorToRspAndLog(req, rsp, "Query will not be processed due to queryid is empty");		
			
			return false;
		}
		
		if (!isInteger(queryId)) {
			writeErrorToRspAndLog(req, rsp, "Query will not be processed due to queryid isn't number");		
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check if request command isn't NONE
	 * @param req - solr request
	 * @param rsp - solr response
	 * @param reqCommand - request command (ADD/DELETE/UPDATE)
	 * @return if request command is valid
	 */
	private boolean isReqCommandValid(SolrQueryRequest req, SolrQueryResponse rsp, SolcolatorQueriesRequestCommand reqCommand) {
		if (reqCommand == null) {
			String errMsg = String.format("Request will not be processed due to request command isn't provided. Valid commands: %s", Arrays.asList(SolcolatorQueriesRequestCommand.values()));
			writeErrorToRspAndLog(req, rsp, errMsg);

			return false;
		}
		
		return true;
	}
	
	/**
	 * Get request handler metadata (default)
	 * @param core
	 * @return map between name of arg name and arg value
	 */
	public static Map<String, String> getRequestHandlerMetadata(SolrCore core) {
		Map<String, String> metadata = new HashMap<>();
		SolcolatorQueriesRequestHander reqHandler = ((SolcolatorQueriesRequestHander)core.getRequestHandlers().get(NAME));
		
		if (reqHandler == null) {
			String errMsg = String.format("%s request handler can't be find in solrconfig.xml", NAME);

			throw new ExceptionInInitializerError(errMsg);
		}
		
		NamedList<?> defaults = (NamedList<?>) reqHandler.getInitArgs().get("defaults");
		if (defaults == null || defaults.size() == 0) {
			return metadata;
		}
		
		for (Entry<String, ?> arg : defaults) {
			metadata.put(arg.getKey(), arg.getValue().toString());
		}
		
		return metadata;
	}
	
	private static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}
		
		return true;
	}
}
