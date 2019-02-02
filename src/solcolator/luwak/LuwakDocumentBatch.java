package solcolator.luwak;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.flax.luwak.DocumentBatch;
import uk.co.flax.luwak.InputDocument;

public class LuwakDocumentBatch extends DocumentBatch {
	private static Logger log = LoggerFactory.getLogger(LuwakDocumentBatch.class);
	private final Directory directory = new RAMDirectory();
	private LeafReader reader = null;
	private String[] docIds = null;
	
	public LuwakDocumentBatch(List<InputDocument> docs, Similarity similarity) {
		super(docs, similarity);
		assert docs.size() > 1;
		IndexWriterConfig iwc = new IndexWriterConfig(docs.get(0).getAnalyzers()).setSimilarity(similarity);
		try(IndexWriter writer = new IndexWriter(directory, iwc)) {
			this.reader = build(writer);
		} catch (IOException e) {
			throw new RuntimeException(e);	//This is a RAMDirectory, so should never happen...
		}
	}
	
	@Override
	public LeafReader getIndexReader() throws IOException {
		return reader;
	}
	
	private LeafReader build(IndexWriter writer) throws IOException {
		for(InputDocument doc : documents) {
			writer.addDocument(doc.getDocument());
		}
		
		writer.forceMerge(1);
		writer.commit();
		LeafReader reader = DirectoryReader.open(directory).leaves().get(0).reader();
		
		assert reader != null;
		
		log.info("Batch size from index is " + reader.maxDoc());
		
		docIds = new String[reader.maxDoc()];
		for (int i = 0; i < docIds.length; i++) {
			docIds[i] = reader.document(i).get(InputDocument.ID_FIELD); //TODO can this be more efficient?
		}
		
		return reader;
	}
	
	@Override
	public String resolveDocId(int docId) {
		return docIds[docId];
	}
	
	@Override
	public void close() throws IOException {
		IOUtils.close(reader, directory);
	}
}
