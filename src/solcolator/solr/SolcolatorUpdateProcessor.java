package solcolator.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import solcolator.io.api.ISolcolatorResultsWriter;
import solcolator.luwak.LuwakDocumentBatch;
import solcolator.luwak.LuwakInputDocument;
import solcolator.luwak.LuwakMatcherFactory;
import solcolator.luwak.LuwakQueriesManager;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.flax.luwak.DocumentBatch;
import uk.co.flax.luwak.InputDocument;
import uk.co.flax.luwak.Matches;
import uk.co.flax.luwak.Monitor;
import uk.co.flax.luwak.QueryMatch;
import uk.co.flax.luwak.matchers.HighlightingMatcher;
import uk.co.flax.luwak.matchers.HighlightsMatch;
import uk.co.flax.luwak.matchers.HighlightsMatch.Hit;
import uk.co.flax.luwak.matchers.ParallelMatcher;
import uk.co.flax.luwak.matchers.SimpleMatcher;


public class SolcolatorUpdateProcessor extends UpdateRequestProcessor {
    private static Logger log = LoggerFactory.getLogger(SolcolatorUpdateProcessor.class);
    private final ExecutorService execService;
    private final LuwakQueriesManager manager;
    private final Monitor monitor;
    private final List<ISolcolatorResultsWriter> writers;
    private final LuwakMatcherFactory factory;
    
    private List<InputDocument> luwakDocs = new ArrayList<>();
    private Map<String,SolrInputDocument> solrDocs = new HashMap<>();
    private Similarity similarity;
    private Map<String, Analyzer> fieldToAnalyzer = new HashMap<>();

    public SolcolatorUpdateProcessor(UpdateRequestProcessor next,
    		ExecutorService execService,
    		LuwakQueriesManager manager,
    		LuwakMatcherFactory factory) {
    	super(next);
    	
    	this.manager = manager;
        this.execService = execService;
        this.monitor = manager.getMonitor();
        this.writers = manager.getSolcolatorResultsWriters();
        this.factory = factory;
    }
    
    @Override
    public void finish() throws IOException {
    	execService.execute(() -> {
        	matchDocumentsList(luwakDocs);
        });
    	
    	super.finish();
    }

	private void matchDocumentsList(List<InputDocument> documentsList) {
		log.info("Start to match docs through percolator");
		long start = System.currentTimeMillis();
		
		try {
			DocumentBatch documentBatch = new LuwakDocumentBatch(documentsList, similarity);
			
			matchByFactory(documentsList, documentBatch, factory);	
			
			log.info(String.format("ParallelMatcher matched %d items in %d ms", documentBatch.getBatchSize(), System.currentTimeMillis() - start));	    	
		} catch (Exception e) {
			String errMessage = String.format("Failed to match luwak documents due to %s", e);
			log.error(errMessage);
		} finally {
			log.info("Finish to match docs through percolator");
		}
	}

	private void matchByFactory(List<InputDocument> documentsList, DocumentBatch documentBatch, LuwakMatcherFactory factory) throws IOException {
		switch (factory) {
			case HIGHLIGHTING:
				highlightingMatch(documentsList, documentBatch);
				break;
				
			case SIMPLE:
				simpleMach(documentsList, documentBatch);
				break;
	
			default:
				simpleMach(documentsList, documentBatch);
				break;
		}
	}

	//TODO: To think how to union this function with highlightingMatch
	private void simpleMach(List<InputDocument> documentsList, DocumentBatch documentBatch) throws IOException {
		Matches<QueryMatch> matches = monitor.match(documentBatch, ParallelMatcher.factory(execService, SimpleMatcher.FACTORY));
		Map<String, SolrInputDocument> docsToWrite = new HashMap<>();
		
		for (ISolcolatorResultsWriter writer : writers) {
			for (InputDocument doc : documentsList) {
				String id = doc.getId();
				for (QueryMatch documentMatches : matches.getMatches(doc.getId())) {
					try {
						String queryId = documentMatches.getQueryId();
						SolrInputDocument docWithSpecificFields = getDocWithSpecificFields(queryId, id, null, writer);
						docsToWrite.put(queryId, docWithSpecificFields);
					} catch (Exception e) {
						String errMessage = String.format("Failed to write matched results for doc %s due to %s", doc.getId(), e);
						log.error(errMessage);
					}
				}
			}
	
			writer.writeSolcolatorResults(docsToWrite);
		}
	}

	private void highlightingMatch(List<InputDocument> documentsList, DocumentBatch documentBatch) throws IOException {
		Matches<HighlightsMatch> matches = monitor.match(documentBatch, ParallelMatcher.factory(execService, HighlightingMatcher.FACTORY));
		Map<String, SolrInputDocument> docsToWrite = new HashMap<>();
		
		for (ISolcolatorResultsWriter writer : writers) {
			for (InputDocument doc : documentsList) {
				String id = doc.getId();
				for (HighlightsMatch documentMatches : matches.getMatches(doc.getId()).getMatches()) {
					try {
						String queryId = documentMatches.getQueryId();
						SolrInputDocument docWithSpecificFields = getDocWithSpecificFields(queryId, id, documentMatches.getHits(), writer);
						docsToWrite.put(queryId, docWithSpecificFields);
					} catch (Exception e) {
						String errMessage = String.format("Failed to write matched results for doc %s due to %s", doc.getId(), e);
						log.error(errMessage);
					}
				}
			}
		
			writer.writeSolcolatorResults(docsToWrite);
		}
	}
    
	/**
	 * Return Solr doc with specific (by config) fields only + queryId field, query and hits(optional)
	 * @param queryId	- query id
	 * @param itemId	- id of Solr doc
	 * @param hits		- hits(optional)
	 * @return SolrInputDocument with neccessary fields only
	 */
    private SolrInputDocument getDocWithSpecificFields(String queryId, String itemId, Map<String, Set<Hit>> hits, ISolcolatorResultsWriter writer) {
    	Map<String, SolrInputField> specificFields = new HashMap<>();
    	SolrInputDocument doc = solrDocs.get(itemId);
    	List<String> fl = writer.getFl();	//fl can be different per writer (in the case where we use several writers)
    	
    	if (!fl.contains("*")) {	// if fl = * then we want to get all fields
			for (String fieldName : fl) {
				specificFields.put(fieldName, doc.getField(fieldName));
			}
    	} else {
    		specificFields = doc.entrySet()
    				.stream()
    				.filter(x -> !x.getKey().equals("_version_"))
    				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    	}
    	
    	SolrInputDocument retDoc = new SolrInputDocument(specificFields);
    	
    	// add extra fields
    	retDoc.addField("queryid_s", queryId);
    	retDoc.addField("query_s", manager.getQueryIdToLuwakQuery().get(queryId).getQuery());
    	if (hits != null) {
    		retDoc.addField("hits_s", new SolrInputField(hits.toString()));
		}
    	
    	return retDoc;
	}

	@Override
    public void processAdd(AddUpdateCommand cmd) throws IOException {
    	String itemId = cmd.getIndexedId().utf8ToString();
    	
    	try {    		
	    	IndexSchema schema = cmd.getReq().getSchema();
	    	Document luceneDoc = cmd.getLuceneDocument();
	
	    	setSimilarity(schema.getSimilarity());

	    	InputDocument luwakDoc = createLuwakDoc(itemId, luceneDoc, schema);	
	    	luwakDocs.add(luwakDoc);
	    	solrDocs.put(itemId, cmd.getSolrInputDocument());
	    } catch (Exception e) {
			String errMessage = String.format("Failed to build luwak document for item_id:%s due to %s", itemId, e);
			log.error(errMessage);
		}  
    	
    	super.processAdd(cmd);
    }

	private void setSimilarity(Similarity indexSchemaSimilarity) {
		if (similarity == null) {
			similarity = indexSchemaSimilarity;
		}
	}

	private InputDocument createLuwakDoc(String itemId, Document luceneDoc, IndexSchema schema) {	
    	for( IndexableField f : luceneDoc.getFields() ) {
    		String fname = f.name();
  	      	SchemaField sf = schema.getFieldOrNull(f.name());
  	      	fieldToAnalyzer.put(fname, sf.getType().getIndexAnalyzer());
  	    }	
    	
    	// Luwak internal must field
    	IndexableField f = luceneDoc.getField(schema.getUniqueKeyField().getName());
    	fieldToAnalyzer.put("_luwak_id", schema.getFieldOrNull(f.name()).getType().getIndexAnalyzer());
    	IndexableField _luwak_id = new Field("_luwak_id", itemId, (FieldType) f.fieldType());
    	luceneDoc.add(_luwak_id);
		
    	PerFieldAnalyzerWrapper analyzers = new PerFieldAnalyzerWrapper(fieldToAnalyzer.get(schema.getUniqueKeyField().getName()), fieldToAnalyzer);
		LuwakInputDocument luwakDoc = new LuwakInputDocument(itemId, luceneDoc, analyzers);
				
		return luwakDoc;
	}
}
