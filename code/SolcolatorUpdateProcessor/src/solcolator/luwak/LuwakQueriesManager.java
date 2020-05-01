package solcolator.luwak;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solcolator.io.api.ISolcolatorResultsWriter;
import solcolator.io.api.SolcolatorQuery;
import solcolator.io.api.IQueryReader;
import uk.co.flax.luwak.Monitor;
import uk.co.flax.luwak.MonitorQueryParser;
import uk.co.flax.luwak.Presearcher;
import uk.co.flax.luwak.UpdateException;
import uk.co.flax.luwak.presearcher.MatchAllPresearcher;

/**
 * The class is responsible for managing (add/update/delete) queries
 */
public class LuwakQueriesManager implements AutoCloseable {
	private final static Object updatedLock = new Object();
	private final static LuwakQueriesManager manager = new LuwakQueriesManager();
	
	private final Logger log = LoggerFactory.getLogger(LuwakQueriesManager.class);
		
	private Monitor monitor;
	private Map<String,LuwakQuery> queryIdToLuwakQuery;
	private IQueryReader reader;
	private List<ISolcolatorResultsWriter> writers;
	private List<String> componentsToParser;
		
	public static LuwakQueriesManager getQueriesManager() {
		return manager;
	}
	
	// ======================================= INIT & CLOSE ======================================= //
	/**
	 * Initializing mappings (queryIdToSolrQuery)
	 */
 	public void init(IQueryReader reader, List<ISolcolatorResultsWriter> writers, List<String> componentsToParser) {
		queryIdToLuwakQuery = new HashMap<>();
		
		this.reader = reader;
		this.writers = writers;
		this.componentsToParser = componentsToParser;
		
		log.info("LuwakQueriesManager was initialized successfully");
	}
	
	/**
	 * Create Luwak monitor for storing solcolator queries
	 * @param core - solr core
	 * @throws ExceptionInInitializerError
	 */
	public void createMonitor(SolrCore core) throws ExceptionInInitializerError {
		MonitorQueryParser parser = new LuwakParser(core, componentsToParser);
		Presearcher presearcher = new MatchAllPresearcher();
		
		try {
			monitor = new Monitor(parser, presearcher);
		} catch (IOException e) {
			log.error("Failed to create Monitor", e);
			
			try {
				if (monitor != null) {
					monitor.close();
				}
			} catch (IOException ex) {
				log.error("Failed to close Monitor", ex);
			}
			
			throw new ExceptionInInitializerError(e);
		}
		
		log.info("LuwakQueriesManager monitor was created successfully");
	}
	
	/**
	 * Load solcolator queries to Luwak monitor
	 * @param reqHandlerMetadata - Request handler metadata (default args)
	 */
	public void loadQueriesToSolcolator(Map<String, String> reqHandlerMetadata) {	
		try {
			long start = System.currentTimeMillis();
			List<SolcolatorQuery> solcolatorQueries = reader.readAllQueries(reqHandlerMetadata);
			
			for (SolcolatorQuery solcolatorQuery : solcolatorQueries) {
				try {
					LuwakQuery luwakQuery = new LuwakQuery(solcolatorQuery.getQueryId(),
							solcolatorQuery.getQueryName(),
							solcolatorQuery.getQuery(),
							solcolatorQuery.getQueryMetadata());
					updateQueryInMonitor(luwakQuery);
					queryIdToLuwakQuery.put(luwakQuery.getId(), luwakQuery);
				} catch(Exception ex) {
					// Nothing to do. Solcolator will continue to load queries to monitor
				}			
			}
			
			log.info(String.format("Solcolator finished to load %d queries in %s miliseconds", monitor.getQueryCount(), System.currentTimeMillis() - start));
		} catch (Exception e) {
			String errMessage = "Failed to load queries to monitor";
			log.error(errMessage, e);
			
			throw new ExceptionInInitializerError(e);
		}
	}
	
	/**
	 * Closing all used resources
	 */
	public void close() {
		try {
			for (ISolcolatorResultsWriter writer : writers) {
				writer.close();
			}
			
			reader.close();
			log.info("Manager is closed successfully");
		} catch (Exception e) {
			log.error("Closing writer/reader is failed", e);
		}
	}
	// ============================================================================================ //
	
	// ======================================= GETTERS ============================================ //
	public List<ISolcolatorResultsWriter> getSolcolatorResultsWriters() {
		return writers;
	}
	
	public Monitor getMonitor() {
		return monitor;
	}
	
	public  Map<String,LuwakQuery> getQueryIdToLuwakQuery() {
		return queryIdToLuwakQuery;
	}
	
	public IQueryReader getQueryReader() {
		return reader;
	}
	
	// ============================================================================================ //	
	/**
	 * Update all queries in solcolator
	 * In the case query contains dynamic values like NOW, it will be update
	 */
	public void updateAllQueries() {
		synchronized (updatedLock) {
			log.info("Solcolator is started to update all its queries");
			long startTime = System.currentTimeMillis();
			queryIdToLuwakQuery.forEach((queryId, query) -> {
				try {
					monitor.update(query);												// add/update query in monitor
				} catch (Exception e) {
					log.error(String.format("Query %s is failed to update", queryId), e);
				}
			});
			log.info(String.format("Solcolator is finished to update all its queries in %d miliseconds", System.currentTimeMillis() - startTime));
		}
	}
		
	/**
	 * Update solcolator query (add query if it doesn't exist)
	 * @param monitorQuery
	 * @throws Exception
	 */
	public void updateQueryInMonitor(LuwakQuery monitorQuery) throws Exception {
		synchronized (updatedLock) {
			String queryId = monitorQuery.getId();
			
			try {
				monitor.update(monitorQuery);												// add/update query in monitor
			} catch (UpdateException e) {
				String errMessage = String.format("Failed to load query with id %s due to %s", monitorQuery.getId(), LuwakUpdateException.getPrintableErrorString(e.errors));
				log.error(errMessage);
				
				throw new Exception(errMessage);
			} catch (Exception e) {
				String errMessage = String.format("Failed to load query with id %s", monitorQuery.getId());
				log.error(errMessage, e);
				
				throw new Exception(errMessage, e);
			}
		
			
			queryIdToLuwakQuery.put(queryId, monitorQuery);								// add/update query in queryId to query mapping
			
			log.info(String.format("The query %s was updated successfully", monitorQuery.getId()));
		}
	}
	
	/**
	 * Delete solcolator query bu query id
	 * @param queryId
	 * @throws Exception
	 */
	public void deleteQueryFromMonitor(String queryId) throws Exception {
		synchronized (updatedLock) {

			try {
				monitor.deleteById(queryId);
			} catch (Exception ex) {
				String errMessage = String.format("Failed to delete query with id %s", queryId);
				log.error(errMessage, ex);
				
				throw new Exception(errMessage, ex);
			}
			
			LuwakQuery query = queryIdToLuwakQuery.remove(queryId);
			
			if (query == null) {
				log.error(String.format("Illegal case: Query with id %s doesn't exist in solcolator", queryId));
				return;
			}
			
			log.info(String.format("The query %s was deleted successfully", queryId));
		}
	}
}
