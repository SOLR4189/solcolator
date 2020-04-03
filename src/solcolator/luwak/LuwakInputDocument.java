package solcolator.luwak;

import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;

import uk.co.flax.luwak.InputDocument;

public class LuwakInputDocument extends InputDocument {
	public LuwakInputDocument(String id, Document luceneDocument, PerFieldAnalyzerWrapper analyzers) {
		super(id, luceneDocument, analyzers);
	}
}
