package solcolator.luwak;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solcolator.io.api.ISolcolatorResultsWriter;
import solcolator.io.api.IQueryReader;
import uk.co.flax.luwak.Monitor;
import uk.co.flax.luwak.MonitorQueryParser;
import uk.co.flax.luwak.Presearcher;
import uk.co.flax.luwak.UpdateException;
import uk.co.flax.luwak.presearcher.MatchAllPresearcher;

/**
 * Class is responsible for add/update/delete querie
 *
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
		
	public static LuwakQueriesManager getQueriesAndCapsManager() {
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
	 * Create Luwak monitor for storing percolator queries
	 * @param core - solr core
	 * @throws ExceptionInInitializerError
	 */
	public void createMonitor(SolrCore core) throws ExceptionInInitializerError {
		MonitorQueryParser parser = new LuwakParser(core, componentsToParser);
		Presearcher presearcher = new MatchAllPresearcher();
		
		try {
			monitor = new Monitor(parser, presearcher);
		} catch (IOException e) {
			String errMessage = String.format("Failed to create Monitor due to %s", e);
			log.error(errMessage);
			
			try {
				if (monitor != null) {
					monitor.close();
				}
			} catch (IOException ex) {
				errMessage = String.format("Failed to close Monitor due to %s", ex);
				log.error(errMessage);
			}
			
			throw new ExceptionInInitializerError(errMessage);
		}
		
		log.info("Creating monitor in LuwakQueriesManager was created successfully");
	}
	
	/**
	 * Load solcolator queries to Luwak monitor
	 * @param reqHandlerMetadata - Request handler metadata (default args)
	 */
	public void loadQueriesToSolcolator(Map<String, String> reqHandlerMetadata) {	
		try {
			long start = System.currentTimeMillis();
			List<LuwakQuery> solcolatorQueries = reader.readAllQueries(reqHandlerMetadata);
			
			for (LuwakQuery solcolatorQuery : solcolatorQueries) {
				try {
					updateQueryInMonitor(solcolatorQuery);
					queryIdToLuwakQuery.put(solcolatorQuery.getId(), solcolatorQuery);
				} catch(Exception ex) {
					// Nothing to do. Solcolator will continue to load queries to monitor
				}			
			}
			
			log.info(String.format("Solcolator finished to load %d queries in %s miliseconds", monitor.getQueryCount(), System.currentTimeMillis() - start));
		} catch (Exception e) {
			String errMessage = String.format("Failed to load queries to monitor due to %s", e);
			log.error(errMessage);
			
			throw new ExceptionInInitializerError(errMessage);
		}
	}
	
	/**
	 * Closing all used resources: SolcolatorResultsWriter
	 */
	public void close() {
		try {
			for (ISolcolatorResultsWriter writer : writers) {
				writer.close();
			}
			
			reader.close();
			log.info("Manager is closed successfully");
		} catch (Exception e) {
			log.error(String.format("Closing writer/reader is failed due to ", e));
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
	 * Update all queries in percolator
	 */
	public void updateAllQueries() {
		synchronized (updatedLock) {
			log.info("Solcolator is started to update all its queries");
			long startTime = System.currentTimeMillis();
			queryIdToLuwakQuery.forEach((queryId, query) -> {
				try {
					monitor.update(query);												// add/update query in monitor
				} catch (Exception e) {
					log.error(String.format("Query %s is failed to update due to % s", queryId, e));
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
				String errMessage = String.format("Failed to load query with id %s due to %s", monitorQuery.getId(), e);
				log.error(errMessage);
				throw new Exception(errMessage);
			}
		
			
			queryIdToLuwakQuery.put(queryId, monitorQuery);								// add/update query in queryId to query mapping
			
			log.info(String.format("The query %s was updated successfully", monitorQuery.getId()));
		}
	}
	
	/**
	 * Delete solcolator query
	 * @param queryId
	 * @throws Exception
	 */
	public void deleteQueryFromMonitor(String queryId) throws Exception {
		synchronized (updatedLock) {

			try {
				monitor.deleteById(queryId);
			} catch (Exception ex) {
				String errMessage = String.format("Failed to delete query with id %s due to %s", queryId, ex);
				log.error(errMessage);
				throw new Exception(errMessage);
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
